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

        // 2-tier 경로: 실적일자 필터 존재 OR 정형 서류조건 존재 + line-accel ON
        if (planner.isPresent() && reaggregator.isPresent()
                && (hasFreightDate(command) || PmsMartFilterSupport.hasDocLineFilter(command))) {
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
        return executeQuery(criteria, pageable, doc -> rowMapper.toFreightRow(doc, basisKey), command);
    }

    @Override
    public Page<PmsRawBlRow> searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable) {
        // 2-tier 경로: 날짜 필터(실적·서류 중 하나라도) OR 정형 서류조건 존재 + line-accel ON
        if (planner.isPresent() && reaggregator.isPresent()
                && (hasDocumentDate(command) || PmsMartFilterSupport.hasDocLineFilter(command))) {
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
        return executeQuery(criteria, pageable, doc -> rowMapper.toDocumentRow(doc), command);
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

    /**
     * 라인-그레인 정확 count: pageCriteria($elemMatch) 매칭 B/L 문서 수 = 정확 distinct B/L 수.
     * needsLineGrainCorrelation 또는 hasBlNoFilter 시 pms_bl_mart count 사용.
     * houseBlNo/masterBlNo 인덱스가 pageCriteria에 포함되어 있어 prefix bounded scan으로 처리된다.
     */
    private long exactFreightCount(SearchPmsPerformanceCommand command, String flagField, Criteria pageCriteria) {
        if (needsLineGrainCorrelation(command) || hasBlNoFilter(command) || PmsMartFilterSupport.hasDocLineFilter(command)) {
            return mongoTemplate.count(Query.query(pageCriteria), PmsBlMartDocument.class);
        }
        return planner.get().countFreight(command, flagField);
    }

    /**
     * freight basis 총건수 결정.
     * exactCount=true → 정확 count(exactFreightCount가 hasDocLineFilter면 pageCriteria로 라우팅).
     * hasBlNoFilter → $sample 희소오판 방지로 항상 정확 count.
     * 서류조건(issued/grouped/status/documentTypes)은 저카디라 정확 count가 18~63초 → 근사 기본, 정확은 opt-in.
     * 희소 폴백(정확)은 서류조건에선 적용하지 않고 근사 유지.
     * 그 외 → 근사 추정.
     */
    private long resolveFreightTotal(
            SearchPmsPerformanceCommand command,
            String flagField,
            Criteria pageCriteria) {

        // hasBlNoFilter: $sample 근사가 희소오판을 유발하므로 바로 pms_bl_mart count로 확정.
        // 서류조건(issued/grouped/status/documentTypes)은 저카디라 정확 count가 7~63초 → 근사 기본.
        // 정확은 exactCount opt-in 시에만(exactFreightCount가 hasDocLineFilter면 pageCriteria로 라우팅).
        if (Boolean.TRUE.equals(command.exactCount()) || hasBlNoFilter(command)) {
            return exactFreightCount(command, flagField, pageCriteria);
        }
        long approx = approxEstimator.get().estimate(pageCriteria);
        // 희소 폴백(정확)은 서류조건에선 저카디라 비싸므로 적용하지 않고 근사 유지.
        if (!PmsMartFilterSupport.hasDocLineFilter(command)
                && approx < props.getLineAccel().getEarlyTermThreshold()) {
            return exactFreightCount(command, flagField, pageCriteria);
        }
        return approx;
    }

    /**
     * document basis 총건수 결정.
     * hasBlNoFilter → $sample 희소오판 방지로 항상 pageCriteria 정확 count.
     * 서류조건(grouped/status/documentTypes)은 저카디라 정확 count가 느림 → 근사 기본, 정확은 opt-in.
     *   exactCount=true + 서류조건: pageCriteria count(sidecar는 etd 미보유·저카디라 동일하게 느림).
     *   exactCount=true + 그 외: sidecar covered count.
     * 희소 폴백(정확)은 서류조건에선 적용하지 않고 근사 유지.
     * 그 외 → 근사 추정.
     */
    private long resolveDocumentTotal(
            SearchPmsPerformanceCommand command,
            Criteria pageCriteria) {

        // hasBlNoFilter: $sample 근사가 희소오판을 유발하므로 항상 pageCriteria 정확 count.
        if (hasBlNoFilter(command)) {
            return mongoTemplate.count(Query.query(pageCriteria), PmsBlMartDocument.class);
        }
        // 서류조건(grouped/status/documentTypes)은 저카디라 정확 count가 느림 → 근사 기본, 정확은 opt-in.
        boolean docLine = PmsMartFilterSupport.hasDocLineFilter(command);
        if (Boolean.TRUE.equals(command.exactCount())) {
            // 정확 요청: 서류조건은 pageCriteria(sidecar는 etd 미보유·저카디라 동일하게 느림), 그 외는 sidecar covered count.
            return docLine
                ? mongoTemplate.count(Query.query(pageCriteria), PmsBlMartDocument.class)
                : planner.get().countDocument(command);
        }
        long approx = approxEstimator.get().estimate(pageCriteria);
        // 희소 폴백(정확)은 서류조건에선 비싸므로 적용하지 않고 근사 유지.
        if (!docLine && approx < props.getLineAccel().getEarlyTermThreshold()) {
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

        if (total > props.getLineAccel().getEarlyTermThreshold() || needsLineGrainCorrelation(command) || hasBlNoFilter(command) || PmsMartFilterSupport.hasDocLineFilter(command)) {
            // 밀집 / 라인-그레인 상관 / blNo 필터 / 서류조건 경로: pageCriteria + blId DESC 조기종료 find
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

        if (total > props.getLineAccel().getEarlyTermThreshold() || hasBlNoFilter(command) || PmsMartFilterSupport.hasDocLineFilter(command)) {
            // 밀집 / blNo 필터 / 서류조건 경로: pms_bl_mart houseBlNo 인덱스 활용
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

    // ── 날짜 존재 헬퍼 / blNo 존재 헬퍼 ─────────────────────────────────────

    /**
     * houseBlNo/masterBlNo 필터 존재 여부.
     * 이 조건이 있으면 sidecar 인덱스(flag, pd)에 해당 필드가 없어 residual 필터가 되어
     * $sample 근사가 희소오판·530만 풀스캔을 유발한다.
     * pms_bl_mart houseBlNo 인덱스(prefix bounded scan)로 우회한다.
     */
    private static boolean hasBlNoFilter(SearchPmsPerformanceCommand c) {
        return StringUtils.hasText(c.hblNo()) || StringUtils.hasText(c.mblNo());
    }

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
     * fast-path criteria에 대한 총건수를 결정한다.
     * 2-tier 경로의 resolveFreightTotal과 동형 적응형 분기를 적용한다:
     *   - line-accel OFF(approxEstimator 없음): 무조건 정확 count(기존 동작 무회귀).
     *   - exactCount=true 또는 hasBlNoFilter: 정확 count.
     *   - 근사 < earlyTermThreshold(희소): 정확 count(근사 오차 큰 구간).
     *   - 그 외: 근사 추정.
     */
    private long resolveFastPathTotal(Criteria criteria, SearchPmsPerformanceCommand command) {
        if (approxEstimator.isEmpty()) {
            return mongoTemplate.count(Query.query(criteria), PmsBlMartDocument.class);
        }
        if (Boolean.TRUE.equals(command.exactCount()) || hasBlNoFilter(command)) {
            return mongoTemplate.count(Query.query(criteria), PmsBlMartDocument.class);
        }
        long approx = approxEstimator.get().estimate(criteria);
        if (approx < props.getLineAccel().getEarlyTermThreshold()) {
            return mongoTemplate.count(Query.query(criteria), PmsBlMartDocument.class);
        }
        return approx;
    }

    /**
     * Criteria로 skip/limit 페이지 조회 + 적응형 count를 수행한다.
     * count는 skip/limit/sort 없는 별도 Query로 실행한다.
     */
    private Page<PmsRawBlRow> executeQuery(
            Criteria criteria, Pageable pageable, DocMapper mapper,
            SearchPmsPerformanceCommand command) {

        Query findQuery = Query.query(criteria)
            .with(BL_SORT)
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize());

        List<PmsBlMartDocument> docs = mongoTemplate.find(findQuery, PmsBlMartDocument.class);

        long total = resolveFastPathTotal(criteria, command);

        List<PmsRawBlRow> content = docs.stream().map(mapper::map).toList();
        return new PageImpl<>(content, pageable, total);
    }

    @FunctionalInterface
    private interface DocMapper {
        PmsRawBlRow map(PmsBlMartDocument doc);
    }
}
