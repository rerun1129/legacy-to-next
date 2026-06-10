package com.freightos.pms.adapter.out.mart;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.cancel.PmsExactCountRegistry;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.port.out.PmsPerformanceQueryPort;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoDB Mart 기반 PmsPerformanceQueryPort 구현체.
 * pms.mart.enabled=true일 때만 등록된다.
 * 라우팅 결정은 PmsPerformanceQueryRouter가 담당하며 이 빈은 순수 Mart 조회만 수행한다.
 *
 * 날짜 필터 존재 + line-accel ON 시 2-tier 경로를 사용하며,
 * count 값으로 page 경로를 적응형 분기한다.
 *   - 밀집(count > earlyTermThreshold): blId DESC + $elemMatch 조기종료 find
 *   - 희소(count <= earlyTermThreshold): 기존 sidecar pageBlKeys + _id 조회
 * 그 외에는 fast path(Criteria 단일 쿼리)로 처리한다.
 *
 * count 결정은 PmsMartCountResolver에 위임한다(캐시 + op-kill 포함).
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsMartQueryAdapter implements PmsPerformanceQueryPort {

    private final MongoTemplate mongoTemplate;
    private final PmsMartCriteriaBuilder criteriaBuilder;
    private final PmsMartPageCriteriaBuilder pageCriteriaBuilder;
    private final PmsMartRowMapper rowMapper;
    private final PmsMartProperties props;
    private final PmsMartCountResolver countResolver;

    /**
     * line-accel OFF이면 Optional.empty() — @RequiredArgsConstructor가 Optional<T> 파라미터를
     * 있는 경우 주입, 빈 없는 경우 Optional.empty로 자동 처리한다.
     */
    private final Optional<PmsMartDateDimQueryPlanner>   planner;
    private final Optional<PmsMartLineReaggregator>      reaggregator;
    private final Optional<PmsExactCountRegistry>        exactCountRegistry;

    // B/L 단위 정렬: blId DESC(최신순) + blType ASC(HOUSE < MASTER — 사전순 tie-break)
    private static final Sort BL_SORT = Sort.by(Sort.Direction.DESC, "blId")
        .and(Sort.by(Sort.Direction.ASC, "blType"));

    @Override
    public Page<PmsRawBlRow> searchByFreightLine(SearchPmsPerformanceCommand command, Pageable pageable) {
        AggregationBasis basis = command.effectiveBasis();
        String basisKey = switch (basis) {
            case FREIGHT_INPUT -> "freightInput";
            case TAX_ISSUED    -> "taxIssued";
            case SLIP_ISSUED   -> "slipIssued";
            // DOCUMENT_CREATED는 searchByDocument 경로 — 방어적 처리
            default -> throw new IllegalStateException("searchByFreightLine은 DOCUMENT_CREATED를 지원하지 않습니다: " + basis);
        };
        String flagField = switch (basis) {
            case FREIGHT_INPUT -> "hasFreightInput";
            case TAX_ISSUED    -> "hasTaxIssued";
            case SLIP_ISSUED   -> "hasSlipIssued";
            default -> throw new IllegalStateException("지원하지 않는 basis: " + basis);
        };

        // 2-tier 경로: 실적일자 필터 존재 OR 정형 서류조건 존재 + line-accel ON
        if (planner.isPresent() && reaggregator.isPresent()
                && (hasFreightDate(command) || PmsMartFilterSupport.hasDocLineFilter(command))) {
            String userKey = currentUserKey();
            String signature = PmsPerformanceFilterSignature.of(command);
            exactCountRegistry.ifPresent(r -> r.onNewSearch(userKey, signature));
            String cacheKey = userKey + "|" + signature;

            Criteria pageCriteria = pageCriteriaBuilder.buildFreightPageCriteria(command, basisKey, flagField);
            long total = countResolver.resolveFreightTotal(command, flagField, pageCriteria, cacheKey, userKey, signature);
            // count=0이면 매칭 결과가 없으므로 페이지 find를 생략한다.
            if (total == 0L) return new PageImpl<>(List.of(), pageable, 0L);
            List<PmsBlMartDocument> pageDocs = selectFreightPageDocsWithCriteria(command, flagField, pageCriteria, total, pageable);
            List<PmsRawBlRow> content = pageDocs.stream()
                .map(doc -> reaggregator.get().reaggregateFreight(doc, command, basisKey))
                .toList();
            return new PageImpl<>(content, pageable, total);
        }

        // fast path
        String userKey = currentUserKey();
        String signature = PmsPerformanceFilterSignature.of(command);
        String cacheKey = userKey + "|" + signature;
        Criteria criteria = criteriaBuilder.buildFreight(command, flagField);
        return executeQuery(criteria, pageable, doc -> rowMapper.toFreightRow(doc, basisKey), command, cacheKey);
    }

    @Override
    public Page<PmsRawBlRow> searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable) {
        // 2-tier 경로: 날짜 필터(실적·서류 중 하나라도) OR 정형 서류조건 존재 + line-accel ON
        if (planner.isPresent() && reaggregator.isPresent()
                && (hasDocumentDate(command) || PmsMartFilterSupport.hasDocLineFilter(command))) {
            String userKey = currentUserKey();
            String signature = PmsPerformanceFilterSignature.of(command);
            exactCountRegistry.ifPresent(r -> r.onNewSearch(userKey, signature));
            String cacheKey = userKey + "|" + signature;

            Criteria docPageCriteria = pageCriteriaBuilder.buildDocumentPageCriteria(command);
            long total = countResolver.resolveDocumentTotal(command, docPageCriteria, cacheKey, userKey, signature);
            // count=0이면 매칭 결과가 없으므로 페이지 find를 생략한다.
            if (total == 0L) return new PageImpl<>(List.of(), pageable, 0L);
            List<PmsBlMartDocument> pageDocs = selectDocumentPageDocsWithCriteria(command, docPageCriteria, total, pageable);
            List<PmsRawBlRow> content = pageDocs.stream()
                .map(doc -> reaggregator.get().reaggregateDocument(doc, command))
                .toList();
            return new PageImpl<>(content, pageable, total);
        }

        // fast path
        String userKey = currentUserKey();
        String signature = PmsPerformanceFilterSignature.of(command);
        String cacheKey = userKey + "|" + signature;
        Criteria criteria = criteriaBuilder.buildDocument(command);
        return executeQuery(criteria, pageable, doc -> rowMapper.toDocumentRow(doc), command, cacheKey);
    }

    // ── 적응형 page 선택 ─────────────────────────────────────────────────────

    /**
     * count 임계를 기준으로 freight page 문서 목록을 선택한다.
     * 이미 구성된 pageCriteria를 재사용하여 밀집 경로에서 중복 빌드를 피한다.
     *
     * 밀집(total > threshold): pageCriteria + blId DESC 조기종료 find.
     * 희소(total <= threshold): sidecar pageBlKeys + _id 조회 (기존 경로).
     */
    private List<PmsBlMartDocument> selectFreightPageDocsWithCriteria(
            SearchPmsPerformanceCommand command,
            String flagField,
            Criteria pageCriteria,
            long total,
            Pageable pageable) {

        if (total > props.getLineAccel().getEarlyTermThreshold()
                || PmsMartFilterSupport.needsLineGrainCorrelation(command)
                || PmsMartFilterSupport.hasDocLineFilter(command)) {
            // 밀집 / 라인-그레인 상관 / 서류조건 경로: pageCriteria + blId DESC 조기종료 find
            Query q = Query.query(pageCriteria)
                .with(BL_SORT)
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize());
            return mongoTemplate.find(q, PmsBlMartDocument.class);
        }

        // 희소 경로: sidecar sort 기준 blKeys 선택 후 _id 조회(순서 보존)
        List<String> blKeys = planner.get().pageBlKeysFreight(command, flagField, pageable);
        return findByBlKeysOrdered(blKeys);
    }

    /**
     * count 임계를 기준으로 document page 문서 목록을 선택한다.
     * 이미 구성된 pageCriteria를 재사용하여 밀집 경로에서 중복 빌드를 피한다.
     *
     * 밀집(total > threshold): pageCriteria + blId DESC 조기종료 find.
     * 희소(total <= threshold): sidecar pageBlKeys + _id 조회 (기존 경로).
     */
    private List<PmsBlMartDocument> selectDocumentPageDocsWithCriteria(
            SearchPmsPerformanceCommand command,
            Criteria pageCriteria,
            long total,
            Pageable pageable) {

        if (total > props.getLineAccel().getEarlyTermThreshold()
                || PmsMartFilterSupport.hasDocLineFilter(command)) {
            // 밀집 / 서류조건 경로: pageCriteria + blId DESC 조기종료 find
            Query q = Query.query(pageCriteria)
                .with(BL_SORT)
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize());
            return mongoTemplate.find(q, PmsBlMartDocument.class);
        }

        // 희소 경로
        List<String> blKeys = planner.get().pageBlKeysDocument(command, pageable);
        return findByBlKeysOrdered(blKeys);
    }

    // ── 날짜 존재 헬퍼 ────────────────────────────────────────────────────────

    /** freight basis: 실적일자 필터가 하나라도 있으면 2-tier 대상. */
    private static boolean hasFreightDate(SearchPmsPerformanceCommand c) {
        return StringUtils.hasText(c.performanceDtFrom()) || StringUtils.hasText(c.performanceDtTo());
    }

    /** document basis: 실적일자 또는 서류일자 필터가 하나라도 있으면 2-tier 대상. */
    private static boolean hasDocumentDate(SearchPmsPerformanceCommand c) {
        return hasFreightDate(c)
            || StringUtils.hasText(c.documentDtFrom())
            || StringUtils.hasText(c.documentDtTo());
    }

    // ── blKeys 순서 보존 조회 ─────────────────────────────────────────────────

    /**
     * blKeys 순서를 보존하여 PmsBlMartDocument 일괄 조회한다.
     * sidecar sort(blId DESC, blType ASC) 기준 페이지 순서 보존이 목적.
     */
    private List<PmsBlMartDocument> findByBlKeysOrdered(List<String> blKeys) {
        if (blKeys.isEmpty()) return List.of();

        List<PmsBlMartDocument> docs = mongoTemplate.find(
            Query.query(Criteria.where("_id").in(blKeys)),
            PmsBlMartDocument.class);

        Map<String, PmsBlMartDocument> byId = docs.stream()
            .collect(Collectors.toMap(PmsBlMartDocument::getId, d -> d));

        return blKeys.stream()
            .map(byId::get)
            .filter(Objects::nonNull)
            .toList();
    }

    // ── fast-path 공통 실행 템플릿 ────────────────────────────────────────────

    /**
     * Criteria로 skip/limit 페이지 조회 + 적응형 count를 수행한다.
     * count는 skip/limit/sort 없는 별도 Query로 실행한다.
     * count 결정은 countResolver.resolveFastPathTotal에 위임한다.
     */
    private Page<PmsRawBlRow> executeQuery(
            Criteria criteria, Pageable pageable, DocMapper mapper,
            SearchPmsPerformanceCommand command, String cacheKey) {

        // count를 find보다 먼저 결정한다.
        // total=0이면 페이지 find를 생략(3M 풀스캔 방지).
        long total = countResolver.resolveFastPathTotal(criteria, command, cacheKey);
        if (total == 0L) return new PageImpl<>(List.of(), pageable, 0L);

        Query findQuery = Query.query(criteria)
            .with(BL_SORT)
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize());

        List<PmsBlMartDocument> docs = mongoTemplate.find(findQuery, PmsBlMartDocument.class);
        List<PmsRawBlRow> content = docs.stream().map(mapper::map).toList();
        return new PageImpl<>(content, pageable, total);
    }

    // ── 인증 헬퍼 ─────────────────────────────────────────────────────────────

    /**
     * SecurityContext에서 사용자 식별 키를 추출한다.
     * 인증 정보가 없으면 "anonymous"를 반환한다(op-kill 레지스트리 키로 사용).
     */
    private static String currentUserKey() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return "anonymous";
        }
        return auth.getName();
    }

    @FunctionalInterface
    private interface DocMapper {
        PmsRawBlRow map(PmsBlMartDocument doc);
    }
}
