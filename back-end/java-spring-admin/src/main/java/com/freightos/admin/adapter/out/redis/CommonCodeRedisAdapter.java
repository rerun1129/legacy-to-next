package com.freightos.admin.adapter.out.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.application.commoncode.port.out.CommonCodeCachePort;
import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 공통코드 Redis write-through 어댑터.
 * 키: cc:{groupCode}, 페이로드: [{code, label, labelKo}] JSON 배열.
 * TTL 24h — 소비자(FMS/BMS/PMS)가 폴백 체인 보유이므로 짧게 유지.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonCodeRedisAdapter implements CommonCodeCachePort {

    private static final String KEY_PREFIX = "cc:";
    private static final Duration TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void putGroupCodes(String groupCode, List<CommonCodeSummary> activeCodes) {
        String key = KEY_PREFIX + groupCode;
        try {
            List<CommonCodeCacheEntry> payload = activeCodes.stream()
                    .map(s -> new CommonCodeCacheEntry(s.code(), s.label(), s.labelKo()))
                    .toList();
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.opsForValue().set(key, json, TTL);
        } catch (JsonProcessingException e) {
            // JSON 직렬화 버그 — 운영 영향 없지만 즉시 인지해야 함
            log.warn("공통코드 Redis 직렬화 실패 — DB 폴백으로 처리됩니다. groupCode={}, error={}",
                    groupCode, e.getMessage());
        } catch (Exception e) {
            // Redis 연결 실패 — 소비자가 DB 폴백 처리하므로 삼킨다(가용성 우선)
            log.warn("공통코드 Redis 저장 실패 — DB 폴백으로 처리됩니다. groupCode={}, error={}",
                    groupCode, e.getMessage());
        }
    }

    /** Redis 페이로드 항목 — FMS EnumOption 필드명과 동일하게(code/label/labelKo). */
    private record CommonCodeCacheEntry(String code, String label, String labelKo) {}
}
