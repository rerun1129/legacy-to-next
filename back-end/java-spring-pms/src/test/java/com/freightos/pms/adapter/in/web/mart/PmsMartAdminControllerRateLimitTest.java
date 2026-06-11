package com.freightos.pms.adapter.in.web.mart;

import com.freightos.pms.adapter.out.mart.document.PmsMartSyncState;
import com.freightos.pms.application.mart.port.in.PmsMartMaintenanceUseCase;
import com.freightos.pms.application.mart.result.MartSyncResult;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 JUnit5 — RateLimiter 동작 단위 테스트.
 *
 * 웹 슬라이스(@WebMvcTest) 없음: in-memory RateLimiterRegistry + 수기 스텁 UseCase로 컨트롤러 직접 생성.
 * limitRefreshPeriod=1h로 설정해 60s 경계 race를 완전 차단한다.
 * sleep·시간·랜덤 없는 결정적 실행.
 */
class PmsMartAdminControllerRateLimitTest {

    private PmsMartAdminController controller;
    private AtomicInteger rebuildCallCount;
    private AtomicInteger statusCallCount;

    @BeforeEach
    void setUp() {
        rebuildCallCount = new AtomicInteger(0);
        statusCallCount = new AtomicInteger(0);

        MartSyncResult stubResult = new MartSyncResult("full", 0L, 0L, Instant.now());

        PmsMartMaintenanceUseCase stubUseCase = new PmsMartMaintenanceUseCase() {
            @Override
            public MartSyncResult rebuildFull() {
                rebuildCallCount.incrementAndGet();
                return stubResult;
            }

            @Override
            public MartSyncResult rebuildIncremental() {
                rebuildCallCount.incrementAndGet();
                return stubResult;
            }

            @Override
            public PmsMartSyncState status() {
                statusCallCount.incrementAndGet();
                return new PmsMartSyncState();
            }
        };

        // limitRefreshPeriod=1h: 60s 경계 race를 완전 차단
        RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(1)
                .limitRefreshPeriod(Duration.ofHours(1))
                .timeoutDuration(Duration.ZERO)
                .build();
        RateLimiterRegistry registry = RateLimiterRegistry.of(rateLimiterConfig);
        registry.rateLimiter("pmsMartRebuild", rateLimiterConfig);

        controller = new PmsMartAdminController(stubUseCase, registry);
    }

    @Test
    @DisplayName("1차 rebuild 호출 정상 반환")
    void firstRebuild_succeeds() {
        controller.rebuild("full");
        assertThat(rebuildCallCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("2차 즉시 rebuild → RequestNotPermitted throw")
    void secondImmediateRebuild_throwsRequestNotPermitted() {
        controller.rebuild("full");

        assertThatThrownBy(() -> controller.rebuild("full"))
                .isInstanceOf(RequestNotPermitted.class);
    }

    @Test
    @DisplayName("status는 2회 연속 정상 — RateLimiter 비대상")
    void statusEndpoint_notRateLimited_succeedsTwice() {
        controller.status();
        controller.status();
        assertThat(statusCallCount.get()).isEqualTo(2);
    }
}
