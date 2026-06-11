package com.freightos.fms.adapter.out.commoncode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.fms.application.enums.port.out.CommonCodeCachePort;
import com.freightos.fms.application.enums.projection.EnumOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Redis 기반 공통코드 캐시 어댑터.
 * 캐시 키: cc:{groupCode}, TTL 24h.
 * Redis 예외/타임아웃은 삼키고 빈 Optional을 반환한다.
 */
@Slf4j
@Component
public class CommonCodeRedisAdapter implements CommonCodeCachePort {

    private static final String KEY_PREFIX = "cc:";
    private static final Duration TTL = Duration.ofHours(24);
    private static final TypeReference<List<EnumOption>> LIST_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CommonCodeRedisAdapter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper  = objectMapper;
    }

    @Override
    public Optional<List<EnumOption>> get(String groupCode) {
        try {
            String json = redisTemplate.opsForValue().get(KEY_PREFIX + groupCode);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, LIST_TYPE));
        } catch (JsonProcessingException e) {
            log.warn("CommonCode Redis deserialization failed for group '{}': {}", groupCode, e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            // Redis 연결 실패, 타임아웃 등 인프라 예외는 삼킨다 — DB 폴백이 뒤따른다
            log.warn("CommonCode Redis GET failed for group '{}': {}", groupCode, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String groupCode, List<EnumOption> options) {
        try {
            String json = objectMapper.writeValueAsString(options);
            redisTemplate.opsForValue().set(KEY_PREFIX + groupCode, json, TTL);
        } catch (JsonProcessingException e) {
            log.warn("CommonCode Redis serialization failed for group '{}': {}", groupCode, e.getMessage());
        } catch (Exception e) {
            // Redis write 실패는 서비스 불가 사유가 아니다 — 로그만 남기고 계속
            log.warn("CommonCode Redis PUT failed for group '{}': {}", groupCode, e.getMessage());
        }
    }
}
