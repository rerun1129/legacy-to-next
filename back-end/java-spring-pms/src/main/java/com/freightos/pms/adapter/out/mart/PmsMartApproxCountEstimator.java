package com.freightos.pms.adapter.out.mart;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

/**
 * pms_bl_mart 컬렉션의 총건수 근사 추정기.
 *
 * $sample(k) → $match(pageCriteria) → $count 파이프라인으로
 * sub-second 내에 근사 총건수를 반환한다.
 *
 * 추정 공식: (샘플 중 매칭 수 m / 샘플 크기 k) × 컬렉션 전체 B/L 수 n
 *
 * $sample(k)는 k가 컬렉션의 5% 미만일 때 MongoDB가 random-cursor 경로를 사용하므로
 * 빠르다. pageCriteria에 차원 필터($elemMatch 포함)가 반영되어 있으므로
 * 날짜·차원 조건이 있는 조회에서도 근사 비율이 적절히 반영된다.
 *
 * 활성 조건: master(pms.mart.enabled)와 하위 플래그 모두 true일 때만 활성 — mart off 시 계열 전체 off
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = {"enabled", "line-accel.enabled"}, havingValue = "true")
@RequiredArgsConstructor
public class PmsMartApproxCountEstimator {

    private final MongoTemplate mongoTemplate;
    private final PmsMartProperties props;

    /**
     * pageCriteria에 해당하는 B/L 수의 근사값을 반환한다.
     *
     * @param pageCriteria {@link PmsMartPageCriteriaBuilder}가 생성한 밀집 경로용 Criteria.
     *                     차원 필터($elemMatch 포함)가 이미 합성되어 있어야 한다.
     * @return 추정 총건수. 샘플 크기 k가 0 이하이면 샘플 내 매칭 수(m)를 그대로 반환한다.
     */
    public long estimate(Criteria pageCriteria) {
        // 컬렉션 총 B/L 수: estimatedDocumentCount는 메타데이터 기반으로 즉시 반환
        long n = mongoTemplate.estimatedCount(PmsBlMartDocument.class);
        long k = props.getLineAccel().getApproxSampleSize();

        Aggregation agg = Aggregation.newAggregation(
            Aggregation.sample(k),
            Aggregation.match(pageCriteria),
            Aggregation.count().as("m")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
            agg, PmsBlMartDocument.class, Document.class);

        Document mapped = results.getUniqueMappedResult();
        long m = mapped == null ? 0L : ((Number) mapped.get("m")).longValue();

        // k <= 0이면 비율 추정 불가 — 샘플 매칭 수를 그대로 반환
        if (k <= 0) return m;
        return Math.round((double) m / (double) k * (double) n);
    }
}
