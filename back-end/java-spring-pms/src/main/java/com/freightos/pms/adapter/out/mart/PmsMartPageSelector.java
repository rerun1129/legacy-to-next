package com.freightos.pms.adapter.out.mart;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PmsMartQueryAdapter의 page 선택 로직 전담 컴포넌트.
 *
 * 다음 세 경로를 포함한다.
 *   (1) keyset / skip 기반 dense 경로 — selectDensePage
 *   (2) 희소 경로·깊은 점프 공용 사이드카 경로 — selectViaSidecarFreight / selectViaSidecarDocument
 *   (3) 적응형 라우팅 진입점 — selectFreightPageDocs / selectDocumentPageDocs
 *
 * 활성 조건: pms.mart.enabled=true
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class PmsMartPageSelector {

    private final MongoTemplate mongoTemplate;
    private final PmsMartProperties props;

    private final Optional<PmsMartDateDimQueryPlanner> planner;
    private final Optional<PmsPerformanceQueryCache>   queryCache;
    private final Optional<PmsMartKeysetSupport>       keysetSupport;

    // B/L 단위 정렬: blId DESC(최신순) + blType ASC(HOUSE < MASTER — 사전순 tie-break)
    static final Sort BL_SORT = Sort.by(Sort.Direction.DESC, "blId")
        .and(Sort.by(Sort.Direction.ASC, "blType"));

    // ── 적응형 진입점 ────────────────────────────────────────────────────────

    /**
     * freight basis page 문서 목록을 적응형으로 선택한다.
     *
     * <ul>
     *   <li>희소(total &le; earlyTermThreshold) 또는 line-grain 상관 불필요 시 깊은 점프:
     *       사이드카 경로 우회</li>
     *   <li>그 외 밀집: keyset / skip dense 경로</li>
     * </ul>
     *
     * needsLineGrainCorrelation(TAX/SLIP + 서류타입 필터) 조합은 사이드카가 과대집계하므로
     * 깊은 점프라도 dense 경로를 유지한다.
     */
    List<PmsBlMartDocument> selectFreightPageDocs(
            SearchPmsPerformanceCommand command,
            String flagField,
            Criteria pageCriteria,
            long total,
            Pageable pageable,
            String cacheKey) {

        if (total > props.getLineAccel().getEarlyTermThreshold()
                || PmsMartCountResolver.needsLineGrainCorrelation(command)) {
            // 밀집 경로: 깊은 점프만 사이드카로 우회(needsLineGrainCorrelation이면 항상 dense)
            if (!PmsMartCountResolver.needsLineGrainCorrelation(command)
                    && isDeepJump(pageable, cacheKey)) {
                return selectViaSidecarFreight(command, flagField, pageable, cacheKey);
            }
            Document hint = new Document(flagField, 1).append("blId", -1).append("blType", 1);
            return selectDensePage(pageCriteria, pageable, cacheKey, hint);
        }

        // 희소 경로: sidecar 위임
        return selectViaSidecarFreight(command, flagField, pageable, cacheKey);
    }

    /**
     * document basis page 문서 목록을 적응형으로 선택한다.
     *
     * document basis는 needsLineGrainCorrelation 비대상이므로
     * 밀집 + 깊은 점프이면 조건 없이 사이드카로 우회한다.
     */
    List<PmsBlMartDocument> selectDocumentPageDocs(
            SearchPmsPerformanceCommand command,
            Criteria pageCriteria,
            long total,
            Pageable pageable,
            String cacheKey) {

        if (total > props.getLineAccel().getEarlyTermThreshold()) {
            // 밀집 경로: 깊은 점프만 사이드카로 우회
            if (isDeepJump(pageable, cacheKey)) {
                return selectViaSidecarDocument(command, pageable, cacheKey);
            }
            Document hint = new Document("hasDocumentCreated", 1).append("blId", -1).append("blType", 1);
            return selectDensePage(pageCriteria, pageable, cacheKey, hint);
        }

        // 희소 경로: sidecar 위임
        return selectViaSidecarDocument(command, pageable, cacheKey);
    }

    // ── 깊은 점프 판정 ────────────────────────────────────────────────────────

    /**
     * 현재 페이지 이동이 "깊은 점프"인지 판정한다.
     *
     * <ul>
     *   <li>0페이지: false — early-term skip이 빠름</li>
     *   <li>queryCache 없음: false — keyset 인프라 없음</li>
     *   <li>직전 페이지 경계 캐시 hit: false — 순차 이동이므로 keyset가 더 빠름</li>
     *   <li>그 외: pageable.getOffset() &gt; deepJumpOffsetThreshold 반환</li>
     * </ul>
     */
    boolean isDeepJump(Pageable pageable, String cacheKey) {
        if (pageable.getPageNumber() == 0) return false;
        if (queryCache.isEmpty()) return false;
        if (queryCache.get().getBoundary(cacheKey, pageable.getPageNumber() - 1).isPresent()) return false;
        return pageable.getOffset() > props.getLineAccel().getDeepJumpOffsetThreshold();
    }

    // ── 사이드카 경로 (희소 + 깊은 점프 공용) ────────────────────────────────

    /**
     * freight basis 사이드카 경로.
     * pms_perfdt_entry sidecar 집계로 blKey 목록을 선택하고 본체 컬렉션에서 순서 보존 조회한다.
     * 조회 후 마지막 경계를 저장하여 이후 순차 이동에서 keyset 이점을 확보한다.
     */
    List<PmsBlMartDocument> selectViaSidecarFreight(
            SearchPmsPerformanceCommand command,
            String flagField,
            Pageable pageable,
            String cacheKey) {

        List<String> blKeys = planner.get().pageBlKeysFreight(command, flagField, pageable);
        List<PmsBlMartDocument> docs = findByBlKeysOrdered(blKeys);
        storeLastBoundary(docs, pageable.getPageNumber(), cacheKey);
        return docs;
    }

    /**
     * document basis 사이드카 경로.
     * pms_docdt_entry sidecar 집계로 blKey 목록을 선택하고 본체 컬렉션에서 순서 보존 조회한다.
     * 조회 후 마지막 경계를 저장하여 이후 순차 이동에서 keyset 이점을 확보한다.
     */
    List<PmsBlMartDocument> selectViaSidecarDocument(
            SearchPmsPerformanceCommand command,
            Pageable pageable,
            String cacheKey) {

        List<String> blKeys = planner.get().pageBlKeysDocument(command, pageable);
        List<PmsBlMartDocument> docs = findByBlKeysOrdered(blKeys);
        storeLastBoundary(docs, pageable.getPageNumber(), cacheKey);
        return docs;
    }

    // ── Dense 경로 ─────────────────────────────────────────────────────────

    /**
     * 밀집 경로 page 선택. keysetSupport가 있으면 keyset 우선, 없으면 skip 폴백.
     *
     * @param hint 정렬커버 인덱스 hint Document. keysetSupport·폴백 skip 쿼리 양쪽에 전달된다.
     */
    List<PmsBlMartDocument> selectDensePage(Criteria pageCriteria, Pageable pageable, String cacheKey, Document hint) {
        if (keysetSupport.isPresent() && queryCache.isPresent()) {
            return keysetSupport.get().selectPage(pageCriteria, pageable, cacheKey, queryCache.get(), hint);
        }
        // keysetSupport/queryCache 미등록 시 skip 폴백(이 분기는 정상 환경에서 도달하지 않음)
        Query q = Query.query(pageCriteria)
            .with(BL_SORT)
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize());
        if (hint != null) q.withHint(hint);
        return mongoTemplate.find(q, PmsBlMartDocument.class);
    }

    // ── 경계 저장 ───────────────────────────────────────────────────────────

    /** 희소·사이드카 경로에서도 마지막 문서 경계를 저장하여 다음 페이지 연속 keyset 가능성을 확보한다. */
    void storeLastBoundary(List<PmsBlMartDocument> docs, int pageIndex, String cacheKey) {
        if (docs.isEmpty() || queryCache.isEmpty()) return;
        PmsBlMartDocument last = docs.get(docs.size() - 1);
        queryCache.get().putBoundary(cacheKey, pageIndex, last.getBlId(), last.getBlType());
    }

    // ── blKeys 순서 보존 조회 ────────────────────────────────────────────────

    /**
     * blKeys 순서를 보존하여 PmsBlMartDocument 일괄 조회한다.
     * sidecar sort(blId DESC, blType ASC) 기준 페이지 순서 보존이 목적.
     */
    List<PmsBlMartDocument> findByBlKeysOrdered(List<String> blKeys) {
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
}
