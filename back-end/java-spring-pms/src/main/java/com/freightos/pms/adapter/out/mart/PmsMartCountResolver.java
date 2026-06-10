package com.freightos.pms.adapter.out.mart;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.cancel.PmsExactCountRegistry;
import com.freightos.pms.adapter.out.mart.cancel.PmsQueryCancelledException;
import com.freightos.pms.adapter.out.mart.cancel.RunningOp;
import com.freightos.pms.adapter.out.mart.countindex.PmsRedisExactCountProvider;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 2-tier 경로 및 fast-path의 총건수(total) 결정 로직을 캡슐화한 컴포넌트.
 *
 * count 결정 규칙은 PmsMartQueryAdapter(back)의 인라인 메서드와 동일하며,
 * 결과를 인메모리 캐시에 저장하고 Mongo op-kill(서버사이드 취소)로 감싼다.
 *
 * 활성 조건: pms.mart.enabled=true
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
class PmsMartCountResolver {

    private final MongoTemplate mongoTemplate;
    private final PmsMartProperties props;
    private final Optional<PmsMartDateDimQueryPlanner> planner;
    private final Optional<PmsMartApproxCountEstimator> approxEstimator;
    private final Optional<PmsPerformanceQueryCache> queryCache;
    private final Optional<PmsExactCountRegistry> exactCountRegistry;
    /** count-index OFF면 빈 Optional — @ConditionalOnProperty로 빈 미등록. */
    private final Optional<PmsRedisExactCountProvider> redisCount;

    /**
     * freight basis 총건수를 결정한다.
     *
     * 결정 규칙 (back 원본과 동일):
     * - exactCount=true → 캐시(exact) 적중 시 반환; Redis Count Index 시도(성공 시 반환);
     *   아니면 exactFreightCount(op-kill 래핑) + putExact.
     * - hasBlNoFilter → $sample 희소오판 방지로 exactFreightCount(op-kill 래핑) + putExact.
     * - 그 외 → 캐시(total) 적중 시 반환; Redis Count Index 시도(성공 시 반환);
     *   approx 추정 후 !hasDocLineFilter && approx < threshold 이면 exact 폴백 + putExact, 아니면 approx + putApprox.
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

            // Redis Count Index 우선 시도
            Long redisExact = tryRedisExact(command, cacheKey);
            if (redisExact != null) return redisExact;

            long exact = exactFreightCountWithCancel(command, flagField, pageCriteria, userKey, signature);
            queryCache.ifPresent(c -> c.putExact(cacheKey, exact));
            return exact;
        }

        // hasBlNoFilter: $sample 근사가 희소오판을 유발하므로 바로 정확 count로 확정.
        if (PmsMartFilterSupport.hasBlNoFilter(command)) {
            long exact = exactFreightCountWithCancel(command, flagField, pageCriteria, userKey, signature);
            queryCache.ifPresent(c -> c.putExact(cacheKey, exact));
            return exact;
        }

        Long cached = getCachedTotal(cacheKey);
        if (cached != null) return cached;

        // Redis Count Index 우선 시도 (approx 전)
        Long redisExact = tryRedisExact(command, cacheKey);
        if (redisExact != null) return redisExact;

        long approx = approxEstimator.get().estimate(pageCriteria);
        // 희소 폴백(정확)은 서류조건에선 저카디라 비싸므로 적용하지 않고 근사 유지.
        if (!PmsMartFilterSupport.hasDocLineFilter(command)
                && approx < props.getLineAccel().getEarlyTermThreshold()) {
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
     * 결정 규칙 (back 원본과 동일):
     * - hasBlNoFilter → pageCriteria 직접 count(op-kill 래핑) + putExact.
     * - exactCount=true → 캐시(exact) 적중 시 반환;
     *   docLine ? mongoTemplate.count(pageCriteria) : planner.countDocument (op-kill 래핑) + putExact.
     * - 그 외 → 캐시(total) 적중 시 반환; approx 추정 후
     *   !docLine && approx < threshold 이면 countDocument 폴백(op-kill 래핑) + putExact, 아니면 approx.
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

        // hasBlNoFilter: $sample 근사가 희소오판을 유발하므로 항상 pageCriteria 정확 count.
        if (PmsMartFilterSupport.hasBlNoFilter(command)) {
            long exact = exactDocumentBlNoCaseWithCancel(pageCriteria, userKey, signature);
            queryCache.ifPresent(c -> c.putExact(cacheKey, exact));
            return exact;
        }

        boolean docLine = PmsMartFilterSupport.hasDocLineFilter(command);

        if (Boolean.TRUE.equals(command.exactCount())) {
            Long cachedExact = queryCache.isPresent() ? queryCache.get().getExactTotal(cacheKey) : null;
            if (cachedExact != null) return cachedExact;

            // Redis Count Index 우선 시도
            Long redisExact = tryRedisExact(command, cacheKey);
            if (redisExact != null) return redisExact;

            // 서류조건은 pageCriteria(sidecar는 etd 미보유·저카디라 동일하게 느림), 그 외는 sidecar covered count.
            long exact = exactDocumentCountWithCancel(command, pageCriteria, docLine, userKey, signature);
            queryCache.ifPresent(c -> c.putExact(cacheKey, exact));
            return exact;
        }

        Long cached = getCachedTotal(cacheKey);
        if (cached != null) return cached;

        // Redis Count Index 우선 시도 (approx 전)
        Long redisExact = tryRedisExact(command, cacheKey);
        if (redisExact != null) return redisExact;

        long approx = approxEstimator.get().estimate(pageCriteria);
        // 희소 폴백(정확)은 서류조건에선 비싸므로 적용하지 않고 근사 유지.
        if (!docLine && approx < props.getLineAccel().getEarlyTermThreshold()) {
            long exact = exactDocumentNonDocLineWithCancel(command, userKey, signature);
            queryCache.ifPresent(c -> c.putExact(cacheKey, exact));
            return exact;
        }
        queryCache.ifPresent(c -> c.putApprox(cacheKey, approx));
        return approx;
    }

    /**
     * fast-path(ETD/ETA 전용 경로) 총건수를 결정한다.
     *
     * 결정 규칙 (back 원본과 동일):
     * - 캐시(total) 적중 시 반환.
     * - line-accel OFF(approxEstimator 없음) → mongoTemplate.count + putExact(무회귀).
     * - exactCount=true 또는 hasBlNoFilter → mongoTemplate.count + putExact.
     * - approx 추정 후 approx < earlyTermThreshold(희소) → mongoTemplate.count + putExact.
     * - 그 외 → approx + putApprox.
     *
     * @param criteria criteria (flagField 필터 포함)
     * @param command  조회 커맨드(exactCount·hasBlNoFilter 판단용)
     * @param cacheKey 사용자 키 + 필터 서명 조합
     */
    long resolveFastPathTotal(Criteria criteria, SearchPmsPerformanceCommand command, String cacheKey) {
        Long cached = getCachedTotal(cacheKey);
        if (cached != null) return cached;

        // Redis Count Index 우선 조회 — tryRedisExact 헬퍼로 통합
        Long redisExact = tryRedisExact(command, cacheKey);
        if (redisExact != null) return redisExact;

        if (approxEstimator.isEmpty()) {
            long total = mongoTemplate.count(Query.query(criteria), PmsBlMartDocument.class);
            queryCache.ifPresent(c -> c.putExact(cacheKey, total));
            return total;
        }

        if (Boolean.TRUE.equals(command.exactCount()) || PmsMartFilterSupport.hasBlNoFilter(command)) {
            long total = mongoTemplate.count(Query.query(criteria), PmsBlMartDocument.class);
            queryCache.ifPresent(c -> c.putExact(cacheKey, total));
            return total;
        }

        long approx = approxEstimator.get().estimate(criteria);
        if (approx < props.getLineAccel().getEarlyTermThreshold()) {
            long total = mongoTemplate.count(Query.query(criteria), PmsBlMartDocument.class);
            queryCache.ifPresent(c -> c.putExact(cacheKey, total));
            return total;
        }
        queryCache.ifPresent(c -> c.putApprox(cacheKey, approx));
        return approx;
    }

    // ── op-kill 래핑 헬퍼 ────────────────────────────────────────────────────

    /**
     * freight exact count를 op-kill 래핑과 함께 실행한다.
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
     * document hasBlNoFilter 경로 — pageCriteria 직접 count를 op-kill 래핑으로 실행한다.
     * comment가 있으면 maxTime + comment를 Query에 주입한다.
     */
    private long exactDocumentBlNoCaseWithCancel(
            Criteria pageCriteria,
            String userKey,
            String signature) {

        if (exactCountRegistry.isEmpty()) {
            return mongoTemplate.count(Query.query(pageCriteria), PmsBlMartDocument.class);
        }
        PmsExactCountRegistry registry = exactCountRegistry.get();
        RunningOp op = registry.begin(userKey, signature);
        try {
            long timeout = props.getLineAccel().getExactCountTimeoutMs();
            Query q = Query.query(pageCriteria).comment(op.getComment()).maxTimeMsec(timeout);
            return mongoTemplate.count(q, PmsBlMartDocument.class);
        } catch (RuntimeException ex) {
            if (op.isKilled()) throw new PmsQueryCancelledException();
            throw ex;
        } finally {
            registry.complete(userKey, op);
        }
    }

    /**
     * document exactCount 요청 경로 — docLine 여부에 따라 pageCriteria count 또는 sidecar count를
     * op-kill 래핑으로 실행한다.
     */
    private long exactDocumentCountWithCancel(
            SearchPmsPerformanceCommand command,
            Criteria pageCriteria,
            boolean docLine,
            String userKey,
            String signature) {

        if (exactCountRegistry.isEmpty()) {
            return docLine
                ? mongoTemplate.count(Query.query(pageCriteria), PmsBlMartDocument.class)
                : planner.get().countDocument(command);
        }
        PmsExactCountRegistry registry = exactCountRegistry.get();
        RunningOp op = registry.begin(userKey, signature);
        try {
            if (docLine) {
                long timeout = props.getLineAccel().getExactCountTimeoutMs();
                Query q = Query.query(pageCriteria).comment(op.getComment()).maxTimeMsec(timeout);
                return mongoTemplate.count(q, PmsBlMartDocument.class);
            }
            return planner.get().countDocument(command, op.getComment());
        } catch (RuntimeException ex) {
            if (op.isKilled()) throw new PmsQueryCancelledException();
            throw ex;
        } finally {
            registry.complete(userKey, op);
        }
    }

    /**
     * document 희소 폴백 경로(비docLine) — sidecar covered count를 op-kill 래핑으로 실행한다.
     */
    private long exactDocumentNonDocLineWithCancel(
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

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────

    /**
     * freight exact count. back 어댑터의 인라인 메서드와 동일한 규칙.
     *
     * needsLineGrainCorrelation || hasBlNoFilter || hasDocLineFilter이면 pageCriteria count,
     * 아니면 planner.countFreight.
     * comment가 null이 아니면 maxTime + comment를 Query/Aggregation에 주입한다.
     */
    private long exactFreightCount(
            SearchPmsPerformanceCommand command,
            String flagField,
            Criteria pageCriteria,
            String comment) {

        if (PmsMartFilterSupport.needsLineGrainCorrelation(command)
                || PmsMartFilterSupport.hasBlNoFilter(command)
                || PmsMartFilterSupport.hasDocLineFilter(command)) {
            if (comment != null) {
                long timeout = props.getLineAccel().getExactCountTimeoutMs();
                Query q = Query.query(pageCriteria).comment(comment).maxTimeMsec(timeout);
                return mongoTemplate.count(q, PmsBlMartDocument.class);
            }
            return mongoTemplate.count(Query.query(pageCriteria), PmsBlMartDocument.class);
        }
        return comment != null
            ? planner.get().countFreight(command, flagField, comment)
            : planner.get().countFreight(command, flagField);
    }

    private Long getCachedTotal(String cacheKey) {
        if (queryCache.isEmpty()) return null;
        return queryCache.get().getTotal(cacheKey);
    }

    /**
     * Redis Count Index가 준비된 경우 exactCount를 시도한다.
     * 성공(비-null) 시 캐시에 exact로 저장 후 반환한다.
     * 미준비·미지원·RuntimeException은 null 반환(기존 Mongo 경로로 폴백).
     */
    private Long tryRedisExact(SearchPmsPerformanceCommand command, String cacheKey) {
        if (redisCount.isEmpty() || !redisCount.get().isReady()) {
            return null;
        }
        try {
            Long count = redisCount.get().exactCount(command);
            if (count != null) {
                queryCache.ifPresent(c -> c.putExact(cacheKey, count));
                return count;
            }
        } catch (RuntimeException ex) {
            log.warn("Count Index tryRedisExact 실패 — Mongo 폴백: {}", ex.toString());
        }
        return null;
    }
}
