package com.freightos.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

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
}
