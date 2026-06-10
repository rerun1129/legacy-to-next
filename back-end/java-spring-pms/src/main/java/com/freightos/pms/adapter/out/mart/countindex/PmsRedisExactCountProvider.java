package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.PmsMartFilterSupport;
import com.freightos.pms.adapter.out.mart.document.PmsMartSyncState;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis 역색인 기반 정확 distinct-count Provider.
 *
 * isReady()가 true일 때만 exactCount()를 호출해야 한다.
 * 지원 불가 형태(line/doc grain, B/L 번호, PERFORMANCE dateKind)는 null 반환 → Mongo 폴백.
 *
 * 게이팅: pms.mart.count-index.enabled=true
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "pms.mart.count-index", name = "enabled", havingValue = "true")
public class PmsRedisExactCountProvider {

    static final String CIRCUIT_BREAKER_NAME = "pmsCountIndex";

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final MongoTemplate mongoTemplate;
    private final PmsMartProperties props;
    private final CircuitBreaker circuitBreaker;
    /** Phase B: freight/tax/slip basis perfdt 일버킷 계산 협력자. */
    private final PmsCountIndexFreightPath freightPath;
    /** Phase C: document basis fdId-grain 계산 협력자. */
    private final PmsCountIndexDocumentPath documentPath;

    PmsRedisExactCountProvider(
            RedisTemplate<String, byte[]> redisTemplate,
            MongoTemplate mongoTemplate,
            PmsMartProperties props,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.redisTemplate  = redisTemplate;
        this.mongoTemplate  = mongoTemplate;
        this.props          = props;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        this.freightPath    = new PmsCountIndexFreightPath(redisTemplate, props);
        this.documentPath   = new PmsCountIndexDocumentPath(redisTemplate, props);
    }

    // ── 공개 API ──────────────────────────────────────────────────────────────

    /**
     * Redis 역색인이 사용 가능한 상태인지 확인한다.
     *
     * 조건:
     * 1. Redis ping 성공
     * 2. meta.complete == "1"
     * 3. meta.syncAt 이 Mart lastSyncAt 대비 staleToleranceSeconds 이내
     * 4. bl:overflow / dc:overflow 플래그 없음
     */
    public boolean isReady() {
        try {
            return circuitBreaker.executeSupplier(this::checkReady);
        } catch (Exception e) {
            log.debug("Count Index isReady 확인 실패: {}", e.toString());
            return false;
        }
    }

    /**
     * B/L 속성 필터 조합에 대한 정확 distinct-count를 반환한다.
     *
     * Phase B freight 경로(perfdt 일버킷)를 먼저 시도한다.
     * Phase C document 경로(fdId-grain)를 그 다음 시도한다.
     * 두 경로 모두 null이면 Phase A B/L 속성 경로를 시도한다.
     * Redis 오류 또는 차단기 OPEN이면 null 반환 → Mongo 폴백.
     */
    public Long exactCount(SearchPmsPerformanceCommand cmd) {
        String prefix = props.getCountIndex().getKeyPrefix();

        // Phase B: freight 경로 우선 시도
        try {
            Long freightResult = circuitBreaker.executeSupplier(
                () -> freightPath.computeFreightCount(cmd, prefix));
            if (freightResult != null) {
                return freightResult;
            }
        } catch (Exception e) {
            log.warn("Count Index freight 경로 실패 — document 경로 시도: {}", e.toString());
        }

        // Phase C: document 경로 시도
        try {
            Long docResult = circuitBreaker.executeSupplier(
                () -> documentPath.computeDocumentCount(cmd, prefix));
            if (docResult != null) {
                return docResult;
            }
        } catch (Exception e) {
            log.warn("Count Index document 경로 실패 — Phase A 폴백 시도: {}", e.toString());
        }

        // Phase A: B/L 속성 경로
        if (!isSupportedShape(cmd)) {
            return null;
        }
        try {
            return circuitBreaker.executeSupplier(() -> doExactCount(cmd));
        } catch (Exception e) {
            log.warn("Count Index exactCount 실패 — Mongo 폴백: {}", e.toString());
            return null;
        }
    }

    // ── 지원 형태 판정 ────────────────────────────────────────────────────────

    /**
     * Phase A 지원 형태를 판정한다.
     *
     * null 반환(Mongo 폴백) 조건:
     * - line/doc grain 필터 존재 (hasDocLineFilter, performanceDt, documentDt)
     * - B/L 번호 필터 존재 (hasBlNoFilter)
     * - dateKind == "PERFORMANCE" (line-level 실적일자)
     * - financialDocType, taxType, documentNoLike, groupFinancialNo (비정형)
     * - operator 존재: CriteriaBuilder는 DOCUMENT_CREATED basis의 documentCreated.operator 서브문서로
     *   처리하므로 B/L-level 역색인으로는 커버 불가
     * - teamCode 존재 && basis==DOCUMENT_CREATED: documentCreated.teamCode(max 의미)로 처리 — Phase C에서 모델링
     */
    static boolean isSupportedShape(SearchPmsPerformanceCommand cmd) {
        if (PmsMartFilterSupport.hasBlNoFilter(cmd)) return false;
        if (PmsMartFilterSupport.hasDocLineFilter(cmd)) return false;

        if (StringUtils.hasText(cmd.performanceDtFrom()) || StringUtils.hasText(cmd.performanceDtTo())) return false;
        if (StringUtils.hasText(cmd.documentDtFrom()) || StringUtils.hasText(cmd.documentDtTo())) return false;

        if ("PERFORMANCE".equals(cmd.dateKind())
                && (StringUtils.hasText(cmd.dateFrom()) || StringUtils.hasText(cmd.dateTo()))) return false;

        if (StringUtils.hasText(cmd.financialDocType())) return false;
        if (StringUtils.hasText(cmd.taxType())) return false;
        if (StringUtils.hasText(cmd.documentNoLike())) return false;
        if (StringUtils.hasText(cmd.groupFinancialNo())) return false;

        // operator: CriteriaBuilder가 DOCUMENT_CREATED basis의 documentCreated.operator 서브문서로 처리
        // B/L-level 역색인으로 커버 불가 → null
        if (StringUtils.hasText(cmd.operator())) return false;

        // teamCode + DOCUMENT_CREATED: documentCreated.teamCode(max 의미)로 처리 — Phase C documentPath 소관.
        // Phase A B/L-level 경로에서는 bl:docteam 차원이 있지만 isSupportedShape는 여전히 false 유지
        // (기존 Phase B 테스트 호환성 보존, documentPath가 먼저 시도됨).
        if (StringUtils.hasText(cmd.teamCode()) && cmd.effectiveBasis() == AggregationBasis.DOCUMENT_CREATED) {
            return false;
        }

        return true;
    }

    // ── 내부 구현 ─────────────────────────────────────────────────────────────

    private boolean checkReady() {
        String p = props.getCountIndex().getKeyPrefix();

        // ping — try-with-resources로 연결 반납 보장
        try (var conn = redisTemplate.getConnectionFactory().getConnection()) {
            conn.ping();
        }

        // meta.complete == "1" 확인
        Object completeObj = redisTemplate.opsForHash().get(PmsCountIndexKeys.meta(p), PmsCountIndexKeys.META_COMPLETE);
        if (!"1".equals(parseString(completeObj))) {
            return false;
        }

        // overflow 플래그 검사 — bl:overflow 또는 dc:overflow 존재 시 not-ready
        if (redisTemplate.hasKey(PmsCountIndexKeys.blOverflowFlag(p))) return false;
        if (redisTemplate.hasKey(PmsCountIndexKeys.docOverflowFlag(p))) return false;

        // meta.syncAt 조회
        Object syncAtObj = redisTemplate.opsForHash().get(PmsCountIndexKeys.meta(p), PmsCountIndexKeys.META_SYNC_AT);
        if (syncAtObj == null) {
            return false;
        }

        long syncAtMs = Long.parseLong(parseString(syncAtObj));

        // Mart lastSyncAt 조회
        PmsMartSyncState state = mongoTemplate.findOne(
            Query.query(Criteria.where("_id").is("pms_bl_mart")), PmsMartSyncState.class);
        if (state == null || state.getLastSyncAt() == null) {
            return false;
        }

        Instant martSyncAt = state.getLastSyncAt();
        long toleranceMs   = props.getCountIndex().getStaleToleranceSeconds() * 1000L;
        return syncAtMs >= (martSyncAt.toEpochMilli() - toleranceMs);
    }

    private static String parseString(Object val) {
        if (val == null) return null;
        if (val instanceof byte[] b) return new String(b, StandardCharsets.UTF_8);
        return val.toString();
    }

    private Long doExactCount(SearchPmsPerformanceCommand cmd) {
        String p        = props.getCountIndex().getKeyPrefix();
        String dateKind = cmd.dateKind();
        String dateFrom = cmd.dateFrom();
        String dateTo   = cmd.dateTo();

        // null: maxDistinctScan 초과 → Mongo 폴백
        List<String> bitmapKeys = collectBitmapKeys(cmd, p, dateKind, dateFrom, dateTo);
        if (bitmapKeys == null) {
            return null;
        }

        if (bitmapKeys.isEmpty()) {
            // 필터 없음 → Mongo 폴백(전체 count는 Mart 집계가 더 정확)
            return null;
        }

        // 날짜 범위 키(OR 집합)와 dim 키(AND 집합) 분리
        List<String> dateKeys = new ArrayList<>();
        List<String> dimKeys  = new ArrayList<>();
        for (String k : bitmapKeys) {
            if (k.contains(":bl:etd:") || k.contains(":bl:eta:")) {
                dateKeys.add(k);
            } else {
                dimKeys.add(k);
            }
        }

        // pipelined MGET
        List<byte[]> allBytes = mgetAll(bitmapKeys);

        // 날짜 범위 OR → dim AND → cardinality
        RoaringBitmap result = computeCardinality(bitmapKeys, dateKeys, dimKeys, allBytes);
        return (long) result.getCardinality();
    }

    /**
     * 필터 조건에 해당하는 비트맵 키 목록을 수집한다.
     * maxDistinctScan 초과 시 null을 반환(Mongo 폴백 신호).
     *
     * basis has-flag 키 추가 규칙:
     * - 차원·날짜 키가 하나라도 있을 때만 basis has-flag 키를 AND 묶음에 추가한다.
     * - 차원·날짜가 모두 없으면(무필터 전체 조회) 기존 동작 그대로 빈 목록 반환
     *   (fast-path 전체 count는 Mongo가 이미 빠름).
     * - 이렇게 하면 Mongo fast-path criteria(항상 flagField=true 포함)와 의미상 동치가 유지된다.
     */
    List<String> collectBitmapKeys(
            SearchPmsPerformanceCommand cmd,
            String p,
            String dateKind,
            String dateFrom,
            String dateTo) {

        List<String> keys = new ArrayList<>();

        // 차원 필터
        PmsCountIndexBitmapKeyCollector.collectDimKeys(cmd, p, keys);

        // 날짜 범위 버킷 키
        boolean isEtd = "ETD".equals(dateKind) || !StringUtils.hasText(dateKind);
        boolean isEta = "ETA".equals(dateKind);
        if ((isEtd || isEta) && (StringUtils.hasText(dateFrom) || StringUtils.hasText(dateTo))) {
            String from = StringUtils.hasText(dateFrom) ? dateFrom : "00000000";
            String to   = StringUtils.hasText(dateTo)   ? dateTo   : "99999999";
            List<String> dayKeys = isEtd
                ? PmsCountIndexBitmapKeyCollector.etdDayKeys(p, from, to)
                : PmsCountIndexBitmapKeyCollector.etaDayKeys(p, from, to);

            // maxDistinctScan 상한 검사 — 초과 시 null(Mongo 폴백)
            if (keys.size() + dayKeys.size() > props.getCountIndex().getMaxDistinctScan()) {
                log.debug("Count Index: 날짜 범위 키 수({}) maxDistinctScan 초과 → Mongo 폴백", dayKeys.size());
                return null;
            }
            keys.addAll(dayKeys);
        }

        // basis has-flag 키 추가: 차원/날짜 필터가 있을 때만 AND에 포함한다.
        // 무필터 전체 조회(keys 빈 상태)면 null 반환(아래 isEmpty 가드)이 기존과 동일하게 동작한다.
        if (!keys.isEmpty()) {
            String flagKey = switch (cmd.effectiveBasis()) {
                case FREIGHT_INPUT    -> PmsCountIndexKeys.hasFlagBitmap(p, PmsCountIndexKeys.FLAG_FREIGHT);
                case TAX_ISSUED       -> PmsCountIndexKeys.hasFlagBitmap(p, PmsCountIndexKeys.FLAG_TAX);
                case SLIP_ISSUED      -> PmsCountIndexKeys.hasFlagBitmap(p, PmsCountIndexKeys.FLAG_SLIP);
                case DOCUMENT_CREATED -> PmsCountIndexKeys.hasFlagBitmap(p, PmsCountIndexKeys.FLAG_DOC);
            };
            keys.add(flagKey);
        }

        return keys;
    }

    private List<byte[]> mgetAll(List<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    private RoaringBitmap computeCardinality(
            List<String> allKeys,
            List<String> dateKeys,
            List<String> dimKeys,
            List<byte[]> allBytes) {

        // 키 → bytes 인덱스 맵 구성
        Map<String, byte[]> keyToBytes = new HashMap<>();
        for (int i = 0; i < allKeys.size(); i++) {
            keyToBytes.put(allKeys.get(i), allBytes.get(i));
        }

        // 날짜 범위 OR
        RoaringBitmap dateBitmap = null;
        if (!dateKeys.isEmpty()) {
            dateBitmap = new RoaringBitmap();
            for (String k : dateKeys) {
                RoaringBitmap day = PmsCountIndexMaintainer.deserialize(keyToBytes.get(k));
                dateBitmap.or(day);
            }
        }

        // dim AND (시작은 dateBitmap 또는 전체)
        RoaringBitmap result = dateBitmap != null ? dateBitmap.clone() : null;

        for (String k : dimKeys) {
            RoaringBitmap dim = PmsCountIndexMaintainer.deserialize(keyToBytes.get(k));
            if (result == null) {
                result = dim.clone();
            } else {
                result.and(dim);
            }
        }

        return result != null ? result : new RoaringBitmap();
    }
}
