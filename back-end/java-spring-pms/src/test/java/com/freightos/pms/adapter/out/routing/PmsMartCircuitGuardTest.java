package com.freightos.pms.adapter.out.routing;

import com.freightos.common.config.PmsMartConfig;
import com.freightos.pms.adapter.out.mart.cancel.PmsQueryCancelledException;
import com.mongodb.MongoException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 JUnit5 — Spring 컨텍스트 없음, in-memory CircuitBreakerRegistry.
 *
 * 상태 전이는 수동(automaticTransitionFromOpenToHalfOpenEnabled=false,
 * transitionToOpenState())으로 제어해 결정적 실행을 보장한다.
 */
class PmsMartCircuitGuardTest {

    private CircuitBreakerRegistry registry;
    private PmsMartCircuitGuard guard;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                // 자동 전이 OFF — 상태 전이를 테스트에서 수동 제어해 결정성 보장
                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                .recordException(ex ->
                    ex instanceof DataAccessResourceFailureException
                    || ex instanceof MongoException)
                .ignoreException(ex -> ex instanceof PmsQueryCancelledException)
                .build();

        registry = CircuitBreakerRegistry.of(config);

        // in-memory RetryRegistry: waitDuration ZERO(결정적 실행), 운영과 동일 술어
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ZERO)
                .retryOnException(PmsMartConfig::retryableMartFault)
                .build();
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        retryRegistry.retry(PmsMartCircuitGuard.INSTANCE, retryConfig);

        // in-memory TimeLimiterRegistry: 여유 타임아웃(이 파일 케이스에서 절대 발화 안 함)
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .build();
        TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.of(timeLimiterConfig);

        executor = Executors.newSingleThreadExecutor();

        guard = new PmsMartCircuitGuard(registry, retryRegistry, timeLimiterRegistry, executor);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    @DisplayName("mart supplier 성공 시 mart 값 반환, oltp 미호출")
    void martSucceeds_returnsMartValue() {
        AtomicInteger oltpCallCount = new AtomicInteger(0);

        String result = guard.martOrOltp(
            () -> "MART",
            () -> { oltpCallCount.incrementAndGet(); return "OLTP"; });

        assertThat(result).isEqualTo("MART");
        assertThat(oltpCallCount.get()).isZero();
    }

    @Test
    @DisplayName("DataAccessException 발생 시 OLTP 폴백 값 반환")
    void dataAccessException_fallsBackToOltp() {
        String result = guard.martOrOltp(
            () -> { throw new DataAccessResourceFailureException("down"); },
            () -> "OLTP");

        assertThat(result).isEqualTo("OLTP");
    }

    @Test
    @DisplayName("차단기 OPEN 상태이면 mart supplier 미호출 후 OLTP 폴백")
    void circuitOpen_shortCircuitsToOltp() {
        registry.circuitBreaker(PmsMartCircuitGuard.INSTANCE).transitionToOpenState();
        AtomicInteger martCallCount = new AtomicInteger(0);

        String result = guard.martOrOltp(
            () -> { martCallCount.incrementAndGet(); return "MART"; },
            () -> "OLTP");

        assertThat(result).isEqualTo("OLTP");
        assertThat(martCallCount.get()).isZero();
    }

    @Test
    @DisplayName("PmsQueryCancelledException은 폴백 없이 그대로 전파")
    void cancelledException_propagatesWithoutFallback() {
        AtomicInteger oltpCallCount = new AtomicInteger(0);

        assertThatThrownBy(() -> guard.martOrOltp(
            () -> { throw new PmsQueryCancelledException(); },
            () -> { oltpCallCount.incrementAndGet(); return "OLTP"; }))
            .isInstanceOf(PmsQueryCancelledException.class);

        assertThat(oltpCallCount.get()).isZero();
    }

    @Test
    @DisplayName("raw MongoException 발생 시 OLTP 폴백 — broad-catch 회귀 가드")
    void mongoException_fallsBackToOltp() {
        String result = guard.martOrOltp(
            () -> { throw new MongoException("down"); },
            () -> "OLTP");

        assertThat(result).isEqualTo("OLTP");
    }
}
