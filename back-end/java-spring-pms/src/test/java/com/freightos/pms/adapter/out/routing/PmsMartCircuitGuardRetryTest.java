package com.freightos.pms.adapter.out.routing;

import com.freightos.common.config.PmsMartConfig;
import com.freightos.pms.adapter.out.mart.cancel.PmsQueryCancelledException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.ServerAddress;
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
 * 순수 JUnit5 — Retry 합성 동작 단위 테스트.
 *
 * MongoSocketException cause-chain 재시도 술어와 Retry 합성 순서를 검증한다.
 * PmsMartCircuitGuardTest와 동일한 in-memory 구성을 사용한다.
 * sleep·시간·랜덤 없는 결정적 실행.
 */
class PmsMartCircuitGuardRetryTest {

    private CircuitBreakerRegistry cbRegistry;
    private PmsMartCircuitGuard guard;
    private ExecutorService executor;

    /** MongoSocketReadException cause-chain을 가진 DataAccessResourceFailureException. */
    private static DataAccessResourceFailureException socketBlip() {
        return new DataAccessResourceFailureException("blip",
                new MongoSocketReadException("reset", new ServerAddress()));
    }

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                .recordException(ex ->
                        ex instanceof DataAccessResourceFailureException
                        || ex instanceof PmsMartQueryTimeoutException)
                .ignoreException(ex ->
                        ex instanceof PmsQueryCancelledException)
                .build();
        cbRegistry = CircuitBreakerRegistry.of(cbConfig);

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ZERO)
                .retryOnException(PmsMartConfig::retryableMartFault)
                .build();
        RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);
        retryRegistry.retry(PmsMartCircuitGuard.INSTANCE, retryConfig);

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .build();
        TimeLimiterRegistry timeLimiterRegistry = TimeLimiterRegistry.of(timeLimiterConfig);

        executor = Executors.newSingleThreadExecutor();

        guard = new PmsMartCircuitGuard(cbRegistry, retryRegistry, timeLimiterRegistry, executor);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    @DisplayName("socket blip 1회 후 성공 → 결과 MART·mart 호출 2회·oltp 0회")
    void socketBlip_thenSuccess_retriesAndReturnsMart() {
        AtomicInteger martCalls = new AtomicInteger(0);
        AtomicInteger oltpCalls = new AtomicInteger(0);

        String result = guard.martOrOltp(
                () -> {
                    if (martCalls.incrementAndGet() == 1) throw socketBlip();
                    return "MART";
                },
                () -> { oltpCalls.incrementAndGet(); return "OLTP"; });

        assertThat(result).isEqualTo("MART");
        assertThat(martCalls.get()).isEqualTo(2);
        assertThat(oltpCalls.get()).isZero();
    }

    @Test
    @DisplayName("socket 연속 실패 → mart 2회(소진) 후 OLTP 폴백")
    void socketContinuousFail_exhaustsRetry_fallsBackToOltp() {
        AtomicInteger martCalls = new AtomicInteger(0);

        String result = guard.martOrOltp(
                () -> { martCalls.incrementAndGet(); throw socketBlip(); },
                () -> "OLTP");

        assertThat(result).isEqualTo("OLTP");
        assertThat(martCalls.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("MongoTimeoutException(serverSelection) → mart 1회만(비재시도) 후 OLTP 폴백 — 회귀 가드")
    void mongoTimeoutException_notRetried_fallsBackToOltp() {
        AtomicInteger martCalls = new AtomicInteger(0);

        // MongoTimeoutException(serverSelection 타임아웃) = MongoClientException 하위, 비재시도
        DataAccessResourceFailureException ex = new DataAccessResourceFailureException("timeout",
                new MongoTimeoutException("server selection timeout"));

        String result = guard.martOrOltp(
                () -> { martCalls.incrementAndGet(); throw ex; },
                () -> "OLTP");

        assertThat(result).isEqualTo("OLTP");
        assertThat(martCalls.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("PmsQueryCancelledException → mart 1회·재throw·oltp 미호출 — 비재시도 회귀 가드")
    void cancelException_notRetried_propagates() {
        AtomicInteger martCalls = new AtomicInteger(0);
        AtomicInteger oltpCalls = new AtomicInteger(0);

        assertThatThrownBy(() -> guard.martOrOltp(
                () -> { martCalls.incrementAndGet(); throw new PmsQueryCancelledException(); },
                () -> { oltpCalls.incrementAndGet(); return "OLTP"; }))
                .isInstanceOf(PmsQueryCancelledException.class);

        assertThat(martCalls.get()).isEqualTo(1);
        assertThat(oltpCalls.get()).isZero();
    }

    @Test
    @DisplayName("socket 연속 실패 후 CB failedCalls==2 — Retry 바깥 합성으로 시도마다 CB 기록")
    void socketContinuousFail_cbRecordsBothAttempts() {
        guard.martOrOltp(
                () -> { throw socketBlip(); },
                () -> "OLTP");

        long failedCalls = cbRegistry.circuitBreaker(PmsMartCircuitGuard.INSTANCE)
                .getMetrics().getNumberOfFailedCalls();
        assertThat(failedCalls).isEqualTo(2);
    }
}
