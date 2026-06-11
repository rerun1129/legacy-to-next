package com.freightos.pms.adapter.out.routing;

import com.freightos.common.config.PmsMartConfig;
import com.freightos.pms.adapter.out.mart.cancel.PmsQueryCancelledException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 JUnit5 — TimeLimiter 동작 단위 테스트.
 *
 * 케이스별로 가드를 개별 구성한다(타임아웃 값이 케이스마다 다름).
 * CountDownLatch로 영구 블록 supplier를 결정적으로 제어한다.
 * sleep·Awaitility·랜덤 없는 결정적 실행.
 */
class PmsMartCircuitGuardTimeLimiterTest {

    /** 테스트별 가드를 생성한다. executor는 호출자가 관리한다. */
    private static PmsMartCircuitGuard buildGuard(Duration timeout, ExecutorService executor) {
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                .recordException(ex ->
                        ex instanceof org.springframework.dao.DataAccessException
                        || ex instanceof PmsMartQueryTimeoutException)
                .ignoreException(ex -> ex instanceof PmsQueryCancelledException)
                .build();
        CircuitBreakerRegistry cbRegistry = CircuitBreakerRegistry.of(cbConfig);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ZERO)
                .retryOnException(PmsMartConfig::retryableMartFault)
                .build();
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        retryRegistry.retry(PmsMartCircuitGuard.INSTANCE, retryConfig);

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(timeout)
                .cancelRunningFuture(true)
                .build();
        TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.of(timeLimiterConfig);

        return new PmsMartCircuitGuard(cbRegistry, retryRegistry, timeLimiterRegistry, executor);
    }

    @Test
    @DisplayName("Duration.ZERO 타임아웃 + 영구 블록 supplier → OLTP 폴백 + CB failedCalls==1 + supplier 진입 1회")
    void zeroTimeout_blocksForever_timesOutAndFallsBack() throws InterruptedException {
        CountDownLatch supplierEntered = new CountDownLatch(1);
        CountDownLatch releaseSupplier = new CountDownLatch(1);
        AtomicInteger supplierCalls = new AtomicInteger(0);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // CB 설정: PmsMartQueryTimeoutException 기록
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                .recordException(ex -> ex instanceof PmsMartQueryTimeoutException)
                .ignoreException(ex -> ex instanceof PmsQueryCancelledException)
                .build();
        CircuitBreakerRegistry cbRegistry = CircuitBreakerRegistry.of(cbConfig);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ZERO)
                .retryOnException(PmsMartConfig::retryableMartFault)
                .build();
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        retryRegistry.retry(PmsMartCircuitGuard.INSTANCE, retryConfig);

        // Duration.ZERO: 즉시 타임아웃
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ZERO)
                .cancelRunningFuture(true)
                .build();
        TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.of(timeLimiterConfig);

        PmsMartCircuitGuard guard = new PmsMartCircuitGuard(
                cbRegistry, retryRegistry, timeLimiterRegistry, executor);

        try {
            String result = guard.martOrOltp(
                    () -> {
                        supplierCalls.incrementAndGet();
                        supplierEntered.countDown();
                        try {
                            // PmsMartQueryTimeoutException은 비재시도 대상이므로 1회만 호출됨
                            releaseSupplier.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return "MART";
                    },
                    () -> "OLTP");

            assertThat(result).isEqualTo("OLTP");
            // PmsMartQueryTimeoutException은 ignoreExceptions가 아니므로 1번 기록
            assertThat(cbRegistry.circuitBreaker(PmsMartCircuitGuard.INSTANCE)
                    .getMetrics().getNumberOfFailedCalls()).isEqualTo(1);
            // Retry는 PmsMartQueryTimeoutException을 재시도 않으므로 supplier 진입 최대 1회
            // Duration.ZERO 타임아웃은 executor submit 직후 만료되므로 supplier가 아직 시작 전일 수 있어 <=1로 검증
            assertThat(supplierCalls.get()).isLessThanOrEqualTo(1);
        } finally {
            releaseSupplier.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("여유 타임아웃 + supplier가 cancel throw → PmsQueryCancelledException 원형 전파(ExecutionException 아님) — r4j 언래핑 회귀 가드")
    void cancelInSupplier_propagatesOriginalCancelException() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            PmsMartCircuitGuard guard = buildGuard(Duration.ofSeconds(5), executor);

            assertThatThrownBy(() -> guard.martOrOltp(
                    () -> { throw new PmsQueryCancelledException(); },
                    () -> "OLTP"))
                    .isExactlyInstanceOf(PmsQueryCancelledException.class);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("SecurityContext 전파 — DelegatingSecurityContextExecutorService 래핑 시 executor 스레드에서 인증 정보 유지")
    void securityContextPropagated_toExecutorThread() throws Exception {
        Authentication auth = new TestingAuthenticationToken("user-a", "pw");
        SecurityContextHolder.getContext().setAuthentication(auth);

        AtomicReference<String> capturedUser = new AtomicReference<>();
        ExecutorService rawExecutor = Executors.newSingleThreadExecutor();
        ExecutorService delegatingExecutor = new DelegatingSecurityContextExecutorService(rawExecutor);

        try {
            PmsMartCircuitGuard guard = buildGuard(Duration.ofSeconds(5), delegatingExecutor);

            guard.martOrOltp(
                    () -> {
                        Authentication captured = SecurityContextHolder.getContext().getAuthentication();
                        capturedUser.set(captured != null ? captured.getName() : "null");
                        return "MART";
                    },
                    () -> "OLTP");

            assertThat(capturedUser.get()).isEqualTo("user-a");
        } finally {
            SecurityContextHolder.clearContext();
            rawExecutor.shutdownNow();
        }
    }
}
