package com.freightos.pms.adapter.out.mart;

import com.freightos.common.config.PmsMartProperties;
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

    /**
     * line-accel OFF이면 Optional.empty() — @RequiredArgsConstructor가 Optional<T> 파라미터를
     * 있는 경우 주입, 빈 없는 경우 Optional.empty로 자동 처리한다.
     */
    private final Optional<PmsMartDateDimQueryPlanner>   planner;
    private final Optional<PmsMartLineReaggregator>      reaggregator;
    private final Optional<PmsMartApproxCountEstimator>  approxEstimator;

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

        // 2-tier 경로: 실적일자 필터 존재 + line-accel ON
        if (planner.isPresent() && reaggregator.isPresent() && hasFreightDate(command)) {
            Criteria pageCriteria = pageCriteriaBuilder.buildFreightPageCriteria(command, basisKey, flagField);
            long total = resolveFreightTotal(command, flagField, pageCriteria);
            List<PmsBlMartDocument> pageDocs = selectFreightPageDocsWithCriteria(command, flagField, pageCriteria, total, pageable);
            List<PmsRawBlRow> content = pageDocs.stream()
                .map(doc -> reaggregator.get().reaggregateFreight(doc, command, basisKey))
                .toList();
            return new PageImpl<>(content, pageable, total);
        }

        // fast path
        Criteria criteria = criteriaBuilder.buildFreight(command, flagField);
        return executeQuery(criteria, pageable, doc -> rowMapper.toFreightRow(doc, basisKey));
    }

    @Override
    public Page<PmsRawBlRow> searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable) {
        // 2-tier 경로: 날짜 필터(실적·서류 중 하나라도) 존재 + line-accel ON
        if (planner.isPresent() && reaggregator.isPresent() && hasDocumentDate(command)) {
            Criteria docPageCriteria = pageCriteriaBuilder.buildDocumentPageCriteria(command);
            long total = resolveDocumentTotal(command, docPageCriteria);
            List<PmsBlMartDocument> pageDocs = selectDocumentPageDocsWithCriteria(command, docPageCriteria, total, pageable);
            List<PmsRawBlRow> content = pageDocs.stream()
                .map(doc -> reaggregator.get().reaggregateDocument(doc, command))
                .toList();
            return new PageImpl<>(content, pageable, total);
        }

        // fast path
        Criteria criteria = criteriaBuilder.buildDocument(command);
        return executeQuery(criteria, pageable, doc -> rowMapper.toDocumentRow(doc));
    }

    // ── count 분기 (근사 기본 / exactCount 토글 / 희소는 exact) ─────────────

    /**
     * TAX/SLIP basis + 서류타입 필터 동시 적용 시 sidecar covered count는
     * (발급 플래그, 서류타입)이 같은 라인임을 보장하지 못해 과대 집계된다.
     * 이 조합에서는 라인-그레인 pageCriteria($elemMatch)로 count·page를 일관 처리한다.
     */
    private static boolean needsLineGrainCorrelation(SearchPmsPerformanceCommand c) {
        AggregationBasis basis = c.effectiveBasis();
        boolean taxOrSlip = basis == AggregationBasis.TAX_ISSUED || basis == AggregationBasis.SLIP_ISSUED;
        boolean hasDocTypeFilter =
            (c.documentTypes() != null && !c.documentTypes().isEmpty())
            || StringUtils.hasText(c.financialDocType());
        return taxOrSlip && hasDocTypeFilter;
    }

    /** 라인-그레인 정확 count: pageCriteria($elemMatch) 매칭 B/L 문서 수 = 정확 distinct B/L 수. */
    private long exactFreightCount(SearchPmsPerformanceCommand command, String flagField, Criteria pageCriteria) {
        if (needsLineGrainCorrelation(command)) {
            return mongoTemplate.count(Query.query(pageCriteria), PmsBlMartDocument.class);
        }
        return planner.get().countFreight(command, flagField);
    }

    /**
     * freight basis 총건수 결정.
     * exactCount=true → 라인-그레인 상관 조합이면 pageCriteria count, 그 외 sidecar 정확 count.
     * 근사 total이 earlyTermThreshold 미만 → 희소 판정 → 정확 count(근사 오차 큰 구간).
     * 그 외 → 근사 추정.
     */
    private long resolveFreightTotal(
            SearchPmsPerformanceCommand command,
            String flagField,
            Criteria pageCriteria) {

        if (Boolean.TRUE.equals(command.exactCount())) {
            return exactFreightCount(command, flagField, pageCriteria);
        }
        long approx = approxEstimator.get().estimate(pageCriteria);
        // 희소: 근사 오차가 커지는 구간이라 정확 count가 오히려 저렴
        if (approx < props.getLineAccel().getEarlyTermThreshold()) {
            return exactFreightCount(command, flagField, pageCriteria);
        }
        return approx;
    }

    /**
     * document basis 총건수 결정.
     * exactCount=true → sidecar 정확 count.
     * 근사 total이 earlyTermThreshold 미만 → 희소 판정 → 정확 count.
     * 그 외 → 근사 추정.
     */
    private long resolveDocumentTotal(
            SearchPmsPerformanceCommand command,
            Criteria pageCriteria) {

        if (Boolean.TRUE.equals(command.exactCount())) {
            return planner.get().countDocument(command);
        }
        long approx = approxEstimator.get().estimate(pageCriteria);
        if (approx < props.getLineAccel().getEarlyTermThreshold()) {
            return planner.get().countDocument(command);
        }
        return approx;
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

        if (total > props.getLineAccel().getEarlyTermThreshold() || needsLineGrainCorrelation(command)) {
            // 밀집 / 라인-그레인 상관 경로: pageCriteria + blId DESC 조기종료 find
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

        if (total > props.getLineAccel().getEarlyTermThreshold()) {
            // 밀집 경로
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
     * Criteria로 skip/limit 페이지 조회 + 별도 count 쿼리를 수행한다.
     * count는 skip/limit/sort 없는 별도 Query로 실행한다.
     */
    private Page<PmsRawBlRow> executeQuery(
            Criteria criteria, Pageable pageable, DocMapper mapper) {

        Query findQuery = Query.query(criteria)
            .with(BL_SORT)
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize());

        List<PmsBlMartDocument> docs = mongoTemplate.find(findQuery, PmsBlMartDocument.class);

        long total = mongoTemplate.count(Query.query(criteria), PmsBlMartDocument.class);

        List<PmsRawBlRow> content = docs.stream().map(mapper::map).toList();
        return new PageImpl<>(content, pageable, total);
    }

    @FunctionalInterface
    private interface DocMapper {
        PmsRawBlRow map(PmsBlMartDocument doc);
    }
}
