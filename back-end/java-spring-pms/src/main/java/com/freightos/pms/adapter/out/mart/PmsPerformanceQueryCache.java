package com.freightos.pms.adapter.out.mart;

import com.freightos.common.config.PmsMartProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PMS 실적 조회 결과(카운트 + 키셋 경계) 인-메모리 캐시.
 *
 * 키 구조: userKey + "|" + filterSignature
 * 캐시 항목: 총건수(total), exact 여부, page → Boundary 맵, 생성 시각(ms).
 *
 * TTL(cacheTtlSeconds)을 초과한 항목은 getXxx 시 lazy 제거된다.
 * 캐시 항목 수가 cacheMaxSize를 초과하면 가장 오래된 항목부터 정리한다.
 *
 * 외부 의존성 추가 없이 ConcurrentHashMap + timestamp 로 단순 구현한다.
 * 활성 조건: pms.mart.line-accel.enabled=true
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart.line-accel", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsPerformanceQueryCache {

    /**
     * 키셋 페이지 경계점.
     * blId DESC, blType ASC 정렬 기준으로 pageIndex번째 페이지의 마지막 문서 위치를 나타낸다.
     */
    public record Boundary(long blId, String blType) {}

    private static final class Entry {
        final long createdAtMillis = System.currentTimeMillis();
        volatile Long total;
        volatile boolean exact;
        final Map<Integer, Boundary> boundaries = new ConcurrentHashMap<>();
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private final PmsMartProperties props;

    // ── 총건수 캐시 ─────────────────────────────────────────────────────────────

    /**
     * 캐시에 저장된 총건수를 반환한다. TTL 초과이면 null 반환(miss).
     * exact/approx 구분 없이 반환한다.
     */
    public Long getTotal(String cacheKey) {
        Entry e = fetchLive(cacheKey);
        return e != null ? e.total : null;
    }

    /**
     * 캐시에 저장된 총건수를 ResolvedTotal(total + approximate 여부)로 반환한다.
     * TTL 초과이거나 total이 null이면 null 반환(miss).
     * exact 저장이면 approximate=false, approx 저장이면 approximate=true.
     */
    public ResolvedTotal getResolvedTotal(String cacheKey) {
        Entry e = fetchLive(cacheKey);
        if (e == null || e.total == null) return null;
        return e.exact ? ResolvedTotal.exact(e.total) : ResolvedTotal.approx(e.total);
    }

    /**
     * exact count로 저장된 총건수만 반환한다.
     * approx로 저장된 항목이거나 TTL 초과이면 null 반환(miss).
     * exactCount=true 분기에서 캐시 재사용 시 사용한다.
     */
    public Long getExactTotal(String cacheKey) {
        Entry e = fetchLive(cacheKey);
        return (e != null && e.exact) ? e.total : null;
    }

    /**
     * 근사 총건수를 캐시에 저장한다.
     * 이미 exact count가 저장되어 있으면 덮어쓰지 않는다.
     */
    public void putApprox(String cacheKey, long total) {
        Entry e = getOrCreate(cacheKey);
        if (!e.exact) {
            e.total = total;
        }
    }

    /**
     * 정확 총건수를 캐시에 저장한다. 항상 덮어쓴다.
     */
    public void putExact(String cacheKey, long total) {
        Entry e = getOrCreate(cacheKey);
        e.total = total;
        e.exact = true;
    }

    // ── 키셋 경계 캐시 ────────────────────────────────────────────────────────

    /**
     * pageIndex번째 페이지 마지막 문서 경계를 반환한다. TTL 초과이면 empty(miss).
     */
    public Optional<Boundary> getBoundary(String cacheKey, int pageIndex) {
        Entry e = fetchLive(cacheKey);
        if (e == null) return Optional.empty();
        return Optional.ofNullable(e.boundaries.get(pageIndex));
    }

    /**
     * pageIndex번째 페이지 마지막 문서 경계를 저장한다.
     */
    public void putBoundary(String cacheKey, int pageIndex, long blId, String blType) {
        getOrCreate(cacheKey).boundaries.put(pageIndex, new Boundary(blId, blType));
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────────────────

    /** 살아있는(non-expired) Entry를 반환하고, 만료됐으면 제거 후 null을 반환한다. */
    private Entry fetchLive(String key) {
        Entry e = store.get(key);
        if (e == null) return null;
        if (isExpired(e)) {
            store.remove(key, e);
            return null;
        }
        return e;
    }

    /** 존재하면 반환, 없으면 생성 후 크기 검사. */
    private Entry getOrCreate(String key) {
        return store.computeIfAbsent(key, k -> {
            evictIfOverLimit();
            return new Entry();
        });
    }

    private boolean isExpired(Entry e) {
        long ttlMs = props.getLineAccel().getCacheTtlSeconds() * 1000L;
        return System.currentTimeMillis() - e.createdAtMillis > ttlMs;
    }

    /**
     * 캐시 크기가 cacheMaxSize를 초과하면 가장 오래된 항목부터 제거한다.
     * computeIfAbsent 내에서 호출되므로 store 크기가 maxSize인 상태에서 실행된다.
     */
    private void evictIfOverLimit() {
        int max = props.getLineAccel().getCacheMaxSize();
        if (store.size() < max) return;

        store.entrySet().stream()
            .min(Comparator.comparingLong(en -> en.getValue().createdAtMillis))
            .ifPresent(en -> store.remove(en.getKey(), en.getValue()));
    }
}
