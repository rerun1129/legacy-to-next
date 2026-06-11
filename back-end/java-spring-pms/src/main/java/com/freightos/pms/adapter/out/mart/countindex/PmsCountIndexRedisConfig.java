package com.freightos.pms.adapter.out.mart.countindex;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Count Index 전용 RedisTemplate 빈 설정.
 *
 * key: String, value: byte[] (RoaringBitmap 직렬화 결과 저장).
 * 게이팅: master(pms.mart.enabled)와 하위 플래그 모두 true일 때만 활성 — mart off 시 계열 전체 off
 */
@Configuration
@ConditionalOnProperty(prefix = "pms.mart", name = {"enabled", "count-index.enabled"}, havingValue = "true")
class PmsCountIndexRedisConfig {

    @Bean
    RedisTemplate<String, byte[]> pmsCountIndexRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, byte[]> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(RedisSerializer.byteArray());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(RedisSerializer.byteArray());
        return template;
    }
}
