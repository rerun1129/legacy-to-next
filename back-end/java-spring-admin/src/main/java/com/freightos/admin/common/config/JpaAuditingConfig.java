package com.freightos.admin.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(a -> a.isAuthenticated() && !"anonymousUser".equals(a.getPrincipal()))
                .map(Authentication::getName)
                .or(() -> Optional.of("SYSTEM"));
    }

    /** 시간 의존성 추상화 (ARCH5). 테스트에서 Clock.fixed(...)로 오버라이드 가능. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
