package com.freightos.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.TimeUnit;

/**
 * PMS Mart 빈군 활성화 설정.
 * pms.mart.enabled=true 일 때만 이 설정 클래스가 로드되며,
 * Mart 라우터 · 어댑터 · 스케줄러가 함께 등록된다.
 * 기본값(enabled 미설정 또는 false)에서는 OLTP 어댑터가 그대로 동작한다.
 */
@Configuration
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(PmsMartProperties.class)
@EnableScheduling
public class PmsMartConfig {

    /**
     * Mongo 클라이언트 server-selection·connect 타임아웃을 하향 조정한다.
     * socket read 타임아웃은 변경하지 않는다 — 긴 exact-count 집계·full rebuild가 죽으면 안 됨.
     * 타임아웃을 줄이면 Mongo 다운 시 회로차단기가 빠르게 열려 OLTP 폴백으로 전환된다.
     */
    @Bean
    public MongoClientSettingsBuilderCustomizer pmsMartMongoTimeouts(PmsMartProperties props) {
        long serverSelection = props.getMongo().getServerSelectionTimeoutMs();
        int connect = props.getMongo().getConnectTimeoutMs();
        return builder -> builder
                .applyToClusterSettings(c -> c.serverSelectionTimeout(serverSelection, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(s -> s.connectTimeout(connect, TimeUnit.MILLISECONDS));
    }
}
