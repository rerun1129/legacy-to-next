package com.freightos.fms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class JpaAuditingConfig {

    /**
     * JWT 도입 전 임시: 시스템 계정 고정.
     * JWT 인증 구현 후 SecurityContextHolder에서 사용자 ID 추출로 교체.
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of("SYSTEM");
    }
}
