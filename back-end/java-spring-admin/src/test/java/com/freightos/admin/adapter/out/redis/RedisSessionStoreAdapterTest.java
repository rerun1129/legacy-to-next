package com.freightos.admin.adapter.out.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.auth.SessionBundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class RedisSessionStoreAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private RedisSessionStoreAdapter adapter;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        adapter = new RedisSessionStoreAdapter(redisTemplate, objectMapper);
    }

    // ── 정상 저장: 키 형식·TTL 검증 ──────────────────────────────────────────

    @Test
    void saveSession_writesCorrectKeyAndTtl() {
        given(redisTemplate.opsForValue()).willReturn(valueOps);

        SessionBundle bundle = new SessionBundle(
                "admin",
                "ROLE_ADMIN,MENU_ADMIN_CODE_LIST",
                Map.of("role", List.of("ADMIN"))
        );

        adapter.saveSession("abc123hash", bundle, 14L);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);

        then(valueOps).should().set(keyCaptor.capture(), jsonCaptor.capture(), ttlCaptor.capture());

        assertThat(keyCaptor.getValue()).isEqualTo("auth:session:abc123hash");
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofDays(14L));
        assertThat(jsonCaptor.getValue()).contains("\"username\":\"admin\"");
        assertThat(jsonCaptor.getValue()).contains("\"authorities\":\"ROLE_ADMIN,MENU_ADMIN_CODE_LIST\"");
    }

    // ── Redis 장애 시 예외를 삼키고 정상 반환 ────────────────────────────────

    @Test
    void saveSession_redisException_silentlyIgnored() {
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        willThrow(new RuntimeException("Connection refused")).given(valueOps)
                .set(anyString(), anyString(), any(Duration.class));

        SessionBundle bundle = new SessionBundle("admin", "ROLE_ADMIN", Map.of());

        // 예외가 전파되지 않고 정상 반환됨
        adapter.saveSession("somehash", bundle, 14L);
    }
}
