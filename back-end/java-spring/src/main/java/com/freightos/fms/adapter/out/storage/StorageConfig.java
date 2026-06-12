package com.freightos.fms.adapter.out.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/**
 * 파일 스토리지 + 스케줄러 설정.
 * Clock 빈: 테스트에서 Clock.fixed()로 교체해 시간 결정성 확보 (ARCH5 준수).
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties({LocalStorageProperties.class, AttachmentCleanupProperties.class})
public class StorageConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
