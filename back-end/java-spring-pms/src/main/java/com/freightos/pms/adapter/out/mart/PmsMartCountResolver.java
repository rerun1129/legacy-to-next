package com.freightos.pms.adapter.out.mart;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.cancel.PmsExactCountRegistry;
import com.freightos.pms.adapter.out.mart.cancel.PmsQueryCancelledException;
import com.freightos.pms.adapter.out.mart.cancel.RunningOp;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 2-tier 경로의 총건수(total) 결정 로직을 캡슐화한 컴포넌트.
 *
 * 결정 흐름:
 * 1. exactCount=true → 정확 count 계산 후 cache.putExact
 * 2. cache 적중 → 재계산 없이 캐시 값 반환
 * 3. 근사 추정 → 희소이면 정확 count로 폴백(cache.putExact), 밀집이면 근사(cache.putApprox)
 *
 * 활성 조건: pms.mart.enabled=true
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class PmsMartCountResolver {

    private final MongoTemplate mongoTemplate;
    private final PmsMartProperties props;
    private final Optional<PmsMartDateDimQueryPlanner> planner;
    private final Optional<PmsMartApproxCountEstimator> approxEstimator;
    private final Optional<PmsPerformanceQueryCache> queryCache;
    private final Optional<PmsExactCountRegistry> exactCountRegistry;

    /**
     * freight basis 총건수를 결정한다.
     *
     * @param command      조회 커맨드
     * @param flagField    basis 존재 플래그 필드명
     * @param pageCriteria 밀집 경로용 pageCriteria($elemMatch 포함)
     * @param cacheKey     캐시 키
     * @param userKey      인증 사용자 키(취소 레지스트리 식별용)
     * @param signature    필터 서명(취소 레지스트리 식별용)
     */
    long resolveFreightTotal(
            SearchPmsPerformanceCommand command,
            String flagField,
            Criteria pageCriteria,
            String cacheKey,
            String userKey,
            String signature) {

        if (Boolean.TRUE.equals(command.exactCount())) {
            Long cachedExact = queryCache.isPresent() ? queryCache.get().getExactTotal(cacheKey) : null;
            if (cachedExact != null) return cachedExact;
            long exact = exactFreightCountWithCancel(command, flagField, pageCriteria, userKey, signature);
            queryCache.ifPresent(c -> c.putExact(cacheKey, exact));
            return exact;
        }

        Long cached = getCachedTotal(cacheKey);
        if (cached != null) return cached;

        long approx = approxEstimator.get().estimate(pageCriteria);
        if (approx < props.getLineAccel().getEarlyTermThreshold()) {
            long exact = exactFreightCountWithCancel(command, flagField, pageCriteria, userKey, signature);
            queryCache.ifPresent(c -> c.putExact(cacheKey, exact));
            return exact;
        }
        queryCache.ifPresent(c -> c.putApprox(cacheKey, approx));
        return approx;
    }

    /**
     * document basis 총건수를 결정한다.
     *
     * @param command      조회 커맨드
     * @param pageCriteria 밀집 경로용 pageCriteria($elemMatch 포함)
     * @param cacheKey     캐시 키
     * @param userKey      인증 사용자 키(취소 레지스트리 식별용)
     * @param signature    필터 서명(취소 레지스트리 식별용)
     */
    long resolveDocumentTotal(
            SearchPmsPerformanceCommand command,
            Criteria pageCriteria,
            String cacheKey,
            String userKey,
            String signature) {

        if (Boolean.TRUE.equals(command.exactCount())) {
            Long cachedExact = queryCache.isPresent() ? queryCache.get().getExactTotal(cacheKey) : null;
            if (cachedExact != null) return cachedExact;
            long exact = exactDocumentCountWithCancel(command, userKey, signature);
            queryCache.ifPresent(c -> c.putExact(cacheKey, exact));
            return exact;
        }

        Long cached = getCachedTotal(cacheKey);
        if (cached != null) return cached;

        long approx = approxEstimator.get().estimate(pageCriteria);
        if (approx < props.getLineAccel().getEarlyTermThreshold()) {
            long exact = exactDocumentCountWithCancel(command, userKey, signature);
            queryCache.ifPresent(c -> c.putExact(cacheKey, exact));
            return exact;
        }
        queryCache.ifPresent(c -> c.putApprox(cacheKey, approx));
        return approx;
    }

    /**
     * fast-path(ETD/ETA 전용 경로) 총건수를 결정한다.
     *
     * exact count를 캐시 키 단위로 1회만 계산한다.
     * queryCache 없음(line-accel OFF)이면 매번 mongoTemplate.count를 실행한다(무회귀).
     *
     * @param criteria Criteria (flagField 필터 포함)
     * @param cacheKey 사용자 키 + 필터 서명 조합
     */
    long resolveFastPathTotal(Criteria criteria, String cacheKey) {
        Long cached = getCachedTotal(cacheKey);
        if (cached != null) return cached;

        long total = mongoTemplate.count(Query.query(criteria), PmsBlMartDocument.class);
        queryCache.ifPresent(c -> c.putExact(cacheKey, total));
        return total;
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────

    /**
     * TAX/SLIP basis + 서류타입 필터 동시 적용 시 sidecar covered count는
     * (발급 플래그, 서류타입)이 같은 라인임을 보장하지 못해 과대 집계된다.
     */
    static boolean needsLineGrainCorrelation(SearchPmsPerformanceCommand c) {
        AggregationBasis basis = c.effectiveBasis();
        boolean taxOrSlip = basis == AggregationBasis.TAX_ISSUED || basis == AggregationBasis.SLIP_ISSUED;
        boolean hasDocTypeFilter =
            (c.documentTypes() != null && !c.documentTypes().isEmpty())
            || StringUtils.hasText(c.financialDocType());
        return taxOrSlip && hasDocTypeFilter;
    }

    /**
     * freight exact count를 취소 래핑과 함께 실행한다.
     * registry 없음(line-accel OFF)이면 래핑 없이 실행한다.
     */
    private long exactFreightCountWithCancel(
            SearchPmsPerformanceCommand command,
            String flagField,
            Criteria pageCriteria,
            String userKey,
            String signature) {

        if (exactCountRegistry.isEmpty()) {
            return exactFreightCount(command, flagField, pageCriteria, null);
        }
        PmsExactCountRegistry registry = exactCountRegistry.get();
        RunningOp op = registry.begin(userKey, signature);
        try {
            return exactFreightCount(command, flagField, pageCriteria, op.getComment());
        } catch (RuntimeException ex) {
            if (op.isKilled()) throw new PmsQueryCancelledException();
            throw ex;
        } finally {
            registry.complete(userKey, op);
        }
    }

    /**
     * document exact count를 취소 래핑과 함께 실행한다.
     * registry 없음(line-accel OFF)이면 래핑 없이 실행한다.
     */
    private long exactDocumentCountWithCancel(
            SearchPmsPerformanceCommand command,
            String userKey,
            String signature) {

        if (exactCountRegistry.isEmpty()) {
            return planner.get().countDocument(command);
        }
        PmsExactCountRegistry registry = exactCountRegistry.get();
        RunningOp op = registry.begin(userKey, signature);
        try {
            return planner.get().countDocument(command, op.getComment());
        } catch (RuntimeException ex) {
            if (op.isKilled()) throw new PmsQueryCancelledException();
            throw ex;
        } finally {
            registry.complete(userKey, op);
        }
    }

    private long exactFreightCount(SearchPmsPerformanceCommand command, String flagField, Criteria pageCriteria, String comment) {
        if (needsLineGrainCorrelation(command)) {
            long timeout = props.getLineAccel().getExactCountTimeoutMs();
            Query q = comment != null
                    ? Query.query(pageCriteria).comment(comment).maxTimeMsec(timeout)
                    : Query.query(pageCriteria);
            return mongoTemplate.count(q, PmsBlMartDocument.class);
        }
        return comment != null
                ? planner.get().countFreight(command, flagField, comment)
                : planner.get().countFreight(command, flagField);
    }

    private Long getCachedTotal(String cacheKey) {
        if (queryCache.isEmpty()) return null;
        return queryCache.get().getTotal(cacheKey);
    }
}
