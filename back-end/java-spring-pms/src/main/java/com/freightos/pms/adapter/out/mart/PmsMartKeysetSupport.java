package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * dense 경로 키셋 페이지 선택 헬퍼.
 *
 * blId DESC, blType ASC 정렬 기준에서 이전 페이지 마지막 경계(Boundary)가 캐시에 있으면
 * skip 없이 seek 조건만으로 pageSize 문서를 fetch한다(keyset pagination).
 *
 * 경계 없는 경우(첫 페이지 또는 캐시 miss)는 기존 skip 폴백을 사용한다.
 *
 * 활성 조건: pms.mart.line-accel.enabled=true
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart.line-accel", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class PmsMartKeysetSupport {

    private final MongoTemplate mongoTemplate;

    // BL_SORT 는 PmsMartQueryAdapter와 동일한 정렬: blId DESC + blType ASC
    private static final Sort BL_SORT = Sort.by(Sort.Direction.DESC, "blId")
        .and(Sort.by(Sort.Direction.ASC, "blType"));

    /**
     * dense 경로 page 문서를 선택한다.
     *
     * <p>이전 페이지 경계가 캐시에 있으면 keyset find(skip 없음)를 수행한다.
     * 없으면 기존 skip find 폴백을 사용한다.
     *
     * <p>page 문서 fetch 후 비어있지 않으면 마지막 문서의 (blId, blType)을 cache에 저장한다.
     * 이를 통해 희소→밀집 전환 이후에도 연속 페이지 이동 시 keyset 이점을 유지한다.
     *
     * @param pageCriteria  밀집 경로용 B/L 레벨 + $elemMatch Criteria
     * @param pageable      페이지 정보(pageNumber, pageSize, offset)
     * @param cacheKey      캐시 키(userKey + "|" + signature)
     * @param cache         QueryCache — boundary 읽기·쓰기
     * @param hint          정렬커버 인덱스 hint Document(키패턴). $or 키셋 쿼리의 blocking sort를 차단한다.
     * @return 현재 페이지에 해당하는 PmsBlMartDocument 목록
     */
    List<PmsBlMartDocument> selectPage(
            Criteria pageCriteria,
            Pageable pageable,
            String cacheKey,
            PmsPerformanceQueryCache cache,
            Document hint) {

        int pageIndex = pageable.getPageNumber();
        Optional<PmsPerformanceQueryCache.Boundary> prevBoundary = cache.getBoundary(cacheKey, pageIndex - 1);

        List<PmsBlMartDocument> docs;
        if (pageIndex > 0 && prevBoundary.isPresent()) {
            docs = fetchByKeyset(pageCriteria, prevBoundary.get(), pageable.getPageSize(), hint);
        } else {
            docs = fetchBySkip(pageCriteria, pageable, hint);
        }

        if (!docs.isEmpty()) {
            PmsBlMartDocument last = docs.get(docs.size() - 1);
            cache.putBoundary(cacheKey, pageIndex, last.getBlId(), last.getBlType());
        }

        return docs;
    }

    /**
     * 이전 페이지 경계(boundary) 이후 문서를 keyset 방식으로 fetch한다.
     *
     * <p>MongoDB가 $or 키셋 술어를 인덱스 bound로 변환(explode-for-sort)하지 못해
     * 잔차필터(residual)로 처리하면 매 페이지마다 blId=MAX부터 경계까지 O(offset)
     * 스캔이 발생한다. 이를 방지하기 위해 $or 대신 단일 필드 직접범위 2쿼리로 분리한다.
     * 각 쿼리는 인덱스 bound seek가 보장되어 깊이 무관 O(pageSize)를 유지한다.
     *
     * <ul>
     *   <li>main: blId &lt; b — 단일 필드 직접 술어, 인덱스 seek 보장</li>
     *   <li>tie:  blId = b AND blType &gt; t — 점+범위, 인덱스 bound seek 보장</li>
     * </ul>
     *
     * <p>blId DESC 정렬에서 tie(blId=b)가 main(blId&lt;b)보다 앞이므로
     * 결합 순서는 tie + main, 앞에서 pageSize개로 자른다.
     * main·tie는 blId 범위가 disjoint(=b vs &lt;b)이므로 중복 없음.
     */
    private List<PmsBlMartDocument> fetchByKeyset(
            Criteria pageCriteria,
            PmsPerformanceQueryCache.Boundary boundary,
            int pageSize,
            Document hint) {

        long b = boundary.blId();
        String t = boundary.blType();

        // main: blId < b (단일 필드 직접 술어 → 인덱스 seek)
        Criteria mainCriteria = new Criteria().andOperator(pageCriteria, Criteria.where("blId").lt(b));
        Query mainQuery = Query.query(mainCriteria).with(BL_SORT).limit(pageSize);
        if (hint != null) mainQuery.withHint(hint);
        List<PmsBlMartDocument> main = mongoTemplate.find(mainQuery, PmsBlMartDocument.class);

        // tie: blId = b AND blType > t (점+범위 → 인덱스 bound seek, 보통 0~1행)
        Criteria tieCriteria = new Criteria().andOperator(pageCriteria, Criteria.where("blId").is(b).and("blType").gt(t));
        Query tieQuery = Query.query(tieCriteria).with(BL_SORT).limit(pageSize);
        if (hint != null) tieQuery.withHint(hint);
        List<PmsBlMartDocument> tie = mongoTemplate.find(tieQuery, PmsBlMartDocument.class);

        // blId DESC 순서: tie(blId=b) 앞 + main(blId<b) 뒤, pageSize개로 자름
        List<PmsBlMartDocument> combined = new ArrayList<>(tie.size() + main.size());
        combined.addAll(tie);
        combined.addAll(main);
        return combined.size() > pageSize ? combined.subList(0, pageSize) : combined;
    }

    /** 경계 없는 경우(첫 페이지, 점프) skip 폴백 */
    private List<PmsBlMartDocument> fetchBySkip(Criteria pageCriteria, Pageable pageable, Document hint) {
        // 정렬커버 인덱스 hint 강제: blocking sort 방지(런타임 회귀 수정)
        Query q = Query.query(pageCriteria)
            .with(BL_SORT)
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize());
        if (hint != null) q.withHint(hint);

        return mongoTemplate.find(q, PmsBlMartDocument.class);
    }
}
