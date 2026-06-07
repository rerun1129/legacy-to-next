package com.freightos.pms.adapter.out.mart;

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

import java.util.List;

/**
 * MongoDB Mart 기반 PmsPerformanceQueryPort 구현체.
 * pms.mart.enabled=true일 때만 등록된다.
 * 라우팅 결정은 PmsPerformanceQueryRouter가 담당하며 이 빈은 순수 Mart 조회만 수행한다.
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsMartQueryAdapter implements PmsPerformanceQueryPort {

    private final MongoTemplate mongoTemplate;
    private final PmsMartCriteriaBuilder criteriaBuilder;
    private final PmsMartRowMapper rowMapper;

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
        String existFlagField = switch (basis) {
            case FREIGHT_INPUT -> "hasFreightInput";
            case TAX_ISSUED    -> "hasTaxIssued";
            case SLIP_ISSUED   -> "hasSlipIssued";
            default -> throw new IllegalStateException("지원하지 않는 basis: " + basis);
        };

        Criteria criteria = criteriaBuilder.buildFreight(command, existFlagField);
        return executeQuery(criteria, pageable, doc -> rowMapper.toFreightRow(doc, basisKey));
    }

    @Override
    public Page<PmsRawBlRow> searchByDocument(SearchPmsPerformanceCommand command, Pageable pageable) {
        Criteria criteria = criteriaBuilder.buildDocument(command);
        return executeQuery(criteria, pageable, doc -> rowMapper.toDocumentRow(doc));
    }

    // ── 공통 실행 템플릿 ──────────────────────────────────────────────────────

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
