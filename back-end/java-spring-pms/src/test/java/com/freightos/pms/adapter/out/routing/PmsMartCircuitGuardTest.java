package com.freightos.pms.adapter.out.routing;

import com.freightos.pms.adapter.out.mart.cancel.PmsQueryCancelledException;
import com.mongodb.MongoException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

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
        guard = new PmsMartCircuitGuard(registry);
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
