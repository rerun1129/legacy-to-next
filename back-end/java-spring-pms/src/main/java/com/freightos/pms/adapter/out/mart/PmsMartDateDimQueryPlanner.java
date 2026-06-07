package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.adapter.out.mart.document.PmsDocDtEntryDocument;
import com.freightos.pms.adapter.out.mart.document.PmsPerfDtEntryDocument;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * line-grain sidecar를 이용한 covered-count + page-select 플래너.
 *
 * sidecar 날짜 인덱스로 B/L 집합을 좁히고(covered),
 * 나머지 필터를 residual 적용한다. count·page-select는 동일 match를
 * 공유하므로 totalElements·페이지 슬라이스가 정합을 유지한다.
 *
 * 활성 조건: pms.mart.line-accel.enabled=true
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart.line-accel", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsMartDateDimQueryPlanner {

    private final MongoTemplate mongoTemplate;
    private final PmsMartDateDimMatchBuilder matchBuilder;

    // ── 공개 API ─────────────────────────────────────────────────────────────

    /**
     * freight basis(pms_perfdt_entry) distinct blKey 수를 반환한다.
     *
     * @param flagField "hasFreightInput" / "hasTaxIssued" / "hasSlipIssued"
     */
    public long countFreight(SearchPmsPerformanceCommand c, String flagField) {
        Criteria criteria = matchBuilder.buildFreightMatch(c, flagField);
        return count(criteria, PmsPerfDtEntryDocument.class);
    }

    /**
     * freight basis(pms_perfdt_entry) 페이지에 해당하는 blKey 목록을 반환한다.
     * 정렬: blId DESC, blType ASC (B/L 최신순 + tie-break).
     *
     * @param flagField "hasFreightInput" / "hasTaxIssued" / "hasSlipIssued"
     */
    public List<String> pageBlKeysFreight(SearchPmsPerformanceCommand c, String flagField, Pageable pageable) {
        Criteria criteria = matchBuilder.buildFreightMatch(c, flagField);
        return pageBlKeys(criteria, pageable, PmsPerfDtEntryDocument.class);
    }

    /**
     * document basis(pms_docdt_entry) distinct blKey 수를 반환한다.
     */
    public long countDocument(SearchPmsPerformanceCommand c) {
        Criteria criteria = matchBuilder.buildDocumentMatch(c);
        return count(criteria, PmsDocDtEntryDocument.class);
    }

    /**
     * document basis(pms_docdt_entry) 페이지에 해당하는 blKey 목록을 반환한다.
     * 정렬: blId DESC, blType ASC (B/L 최신순 + tie-break).
     */
    public List<String> pageBlKeysDocument(SearchPmsPerformanceCommand c, Pageable pageable) {
        Criteria criteria = matchBuilder.buildDocumentMatch(c);
        return pageBlKeys(criteria, pageable, PmsDocDtEntryDocument.class);
    }

    // ── 실행 헬퍼 ─────────────────────────────────────────────────────────────

    /**
     * match → group("blKey") → count 파이프라인.
     * 결과 Document의 "n" 필드가 distinct blKey 수.
     * 결과 없으면 0 반환.
     */
    private long count(Criteria criteria, Class<?> entityClass) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("blKey"),
                Aggregation.count().as("n")
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, entityClass, Document.class);
        Document mapped = results.getUniqueMappedResult();
        if (mapped == null) return 0L;
        return ((Number) mapped.get("n")).longValue();
    }

    /**
     * match → group("blKey", first blId/blType) → sort → skip/limit 파이프라인.
     * group의 _id = blKey 문자열 → 결과 Document의 "_id" 필드.
     */
    private List<String> pageBlKeys(Criteria criteria, Pageable pageable, Class<?> entityClass) {
        Sort sort = Sort.by(Sort.Direction.DESC, "blId").and(Sort.by(Sort.Direction.ASC, "blType"));

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("blKey")
                        .first("blId").as("blId")
                        .first("blType").as("blType"),
                Aggregation.sort(sort),
                Aggregation.skip(pageable.getOffset()),
                Aggregation.limit(pageable.getPageSize())
        ).withOptions(AggregationOptions.builder().allowDiskUse(true).build());

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, entityClass, Document.class);
        return results.getMappedResults().stream()
                .map(d -> d.getString("_id"))
                .toList();
    }
}
