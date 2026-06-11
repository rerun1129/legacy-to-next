package com.freightos.admin.adapter.out.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.auth.SessionBundle;
import com.freightos.admin.application.auth.port.out.SessionStorePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSessionStoreAdapter implements SessionStorePort {

    private static final String KEY_PREFIX = "auth:session:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void saveSession(String refreshTokenHash, SessionBundle bundle, long ttlDays) {
        String key = KEY_PREFIX + refreshTokenHash;
        try {
            String json = objectMapper.writeValueAsString(bundle);
            redisTemplate.opsForValue().set(key, json, Duration.ofDays(ttlDays));
        } catch (JsonProcessingException e) {
            // JSON 직렬화 실패는 코드 버그이므로 RuntimeException으로 전파
            throw new IllegalStateException("SessionBundle 직렬화 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            // Redis 연결 실패 — 로그인은 계속 성공해야 하므로 삼킨다(가용성 우선).
            // Redis 없이 15분 단명 세션으로 degrade된다.
            log.warn("Redis 세션 저장 실패 — 로그인은 계속 진행(Redis 단명 세션 degrade). key={}, error={}", key, e.getMessage());
        }
    }
}
