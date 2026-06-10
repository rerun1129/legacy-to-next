package com.freightos.pms.adapter.out.mart.countindex;

import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.document.PmsMartSyncState;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.roaringbitmap.RoaringBitmap;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * meta 게이팅 규칙(markComplete/markStale/isReady) 단위 테스트.
 *
 * isReady 조건:
 * 1. Redis ping 성공
 * 2. meta.complete == "1"
 * 3. syncAt이 Mart lastSyncAt 대비 staleToleranceSeconds 이내
 * 4. bl:overflow / dc:overflow 플래그 없음
 */
class PmsCountIndexMetaGatingTest {

    private static final String PREFIX     = "pms:ix";
    private static final String META_KEY   = PREFIX + ":meta";
    private static final long   TOLERANCE  = 120L; // seconds

    private PmsRedisExactCountProvider provider;
    private PmsMartProperties props;
    private MongoTemplate mongoTemplate;
    /** 인메모리 저장소 — meta 해시 + 플래그 키. */
    private final Map<String, byte[]>                  valueStore = new HashMap<>();
    private final Map<String, Map<String, byte[]>>     hashStore  = new HashMap<>();

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        props = new PmsMartProperties();
        props.getCountIndex().setKeyPrefix(PREFIX);
        props.getCountIndex().setStaleToleranceSeconds(TOLERANCE);
        props.getLineAccel().setEnabled(true);

        RedisTemplate<String, byte[]> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, byte[]> valueOps    = mock(ValueOperations.class);
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        RedisConnectionFactory connFactory          = mock(RedisConnectionFactory.class);
        RedisConnection conn                        = mock(RedisConnection.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redisTemplate.opsForHash()).thenReturn(hashOps);
        when(redisTemplate.getConnectionFactory()).thenReturn(connFactory);
        when(connFactory.getConnection()).thenReturn(conn);
        // ping 성공 stub
        when(conn.ping()).thenReturn("PONG");

        // opsForHash().get stub: hashStore에서 조회
        when(hashOps.get(anyString(), any())).thenAnswer(inv -> {
            String key   = inv.getArgument(0);
            String field = inv.getArgument(1).toString();
            Map<String, byte[]> h = hashStore.get(key);
            return h != null ? h.get(field) : null;
        });

        // hasKey stub: valueStore에서 조회
        when(redisTemplate.hasKey(anyString())).thenAnswer(inv -> {
            String key = inv.getArgument(0);
            return valueStore.containsKey(key);
        });

        mongoTemplate = mock(MongoTemplate.class);

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        provider = new PmsRedisExactCountProvider(redisTemplate, mongoTemplate, props, registry);
    }

    // ── complete 필드 검사 ────────────────────────────────────────────────────

    @Test
    void complete필드없으면_isReady_false이다() {
        // meta 해시에 complete 없음
        stubMartSyncAt(Instant.now().toEpochMilli() - 10_000);
        assertThat(provider.isReady()).isFalse();
    }

    @Test
    void complete가_1이아니면_isReady_false이다() {
        setMetaField(PmsCountIndexKeys.META_COMPLETE, "0");
        setMetaField(PmsCountIndexKeys.META_SYNC_AT, String.valueOf(Instant.now().toEpochMilli() - 10_000));
        stubMartSyncAt(Instant.now().toEpochMilli() - 10_000);
        assertThat(provider.isReady()).isFalse();
    }

    @Test
    void complete가_1이고_stale이내면_isReady_true이다() {
        long now = Instant.now().toEpochMilli();
        setMetaField(PmsCountIndexKeys.META_COMPLETE, "1");
        setMetaField(PmsCountIndexKeys.META_SYNC_AT, String.valueOf(now));
        stubMartSyncAt(now - 5_000); // 5초 전 martSyncAt
        assertThat(provider.isReady()).isTrue();
    }

    // ── overflow 플래그 검사 ──────────────────────────────────────────────────

    @Test
    void bl_overflow플래그존재하면_isReady_false이다() {
        long now = Instant.now().toEpochMilli();
        setMetaField(PmsCountIndexKeys.META_COMPLETE, "1");
        setMetaField(PmsCountIndexKeys.META_SYNC_AT, String.valueOf(now));
        stubMartSyncAt(now - 5_000);
        // bl:overflow 플래그 설정
        valueStore.put(PREFIX + ":bl:overflow", "1".getBytes(StandardCharsets.UTF_8));
        assertThat(provider.isReady()).isFalse();
    }

    @Test
    void dc_overflow플래그존재하면_isReady_false이다() {
        long now = Instant.now().toEpochMilli();
        setMetaField(PmsCountIndexKeys.META_COMPLETE, "1");
        setMetaField(PmsCountIndexKeys.META_SYNC_AT, String.valueOf(now));
        stubMartSyncAt(now - 5_000);
        // dc:overflow 플래그 설정
        valueStore.put(PREFIX + ":dc:overflow", "1".getBytes(StandardCharsets.UTF_8));
        assertThat(provider.isReady()).isFalse();
    }

    // ── stale 검사 ───────────────────────────────────────────────────────────

    @Test
    void syncAt이_허용범위초과_stale이면_isReady_false이다() {
        long now      = Instant.now().toEpochMilli();
        long martSync = now;                              // martSyncAt = now
        long syncAt   = now - (TOLERANCE * 1000 + 1000); // syncAt = tolerance+1초 초과
        setMetaField(PmsCountIndexKeys.META_COMPLETE, "1");
        setMetaField(PmsCountIndexKeys.META_SYNC_AT, String.valueOf(syncAt));
        stubMartSyncAt(martSync);
        assertThat(provider.isReady()).isFalse();
    }

    @Test
    void syncAt이_허용범위내이면_isReady_true이다() {
        long now      = Instant.now().toEpochMilli();
        long martSync = now;
        long syncAt   = now - (TOLERANCE * 1000 - 1000); // tolerance-1초 이내
        setMetaField(PmsCountIndexKeys.META_COMPLETE, "1");
        setMetaField(PmsCountIndexKeys.META_SYNC_AT, String.valueOf(syncAt));
        stubMartSyncAt(martSync);
        assertThat(provider.isReady()).isTrue();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private void setMetaField(String field, String value) {
        hashStore.computeIfAbsent(META_KEY, k -> new HashMap<>())
                 .put(field, value.getBytes(StandardCharsets.UTF_8));
    }

    private void stubMartSyncAt(long epochMs) {
        PmsMartSyncState state = PmsMartSyncState.builder()
            .id("pms_bl_mart")
            .lastSyncAt(Instant.ofEpochMilli(epochMs))
            .lastFullRebuildAt(Instant.ofEpochMilli(epochMs))
            .build();
        when(mongoTemplate.findOne(any(Query.class), eq(PmsMartSyncState.class))).thenReturn(state);
    }
}
