package com.freightos.common.config;

import com.freightos.pms.adapter.out.mart.cancel.PmsQueryCancelledException;
import com.mongodb.MongoException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.ServerAddress;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PmsMartConfig.retryableMartFault 진리표.
 *
 * package-private static 메서드이므로 동일 패키지(com.freightos.common.config)에서 직접 호출한다.
 * Spring 컨텍스트 없는 순수 JUnit5 단위 테스트.
 */
class PmsMartRetryPredicateTest {

    @Test
    @DisplayName("MongoSocketReadException 직접 → true")
    void mongoSocketReadException_direct_returnsTrue() {
        assertThat(PmsMartConfig.retryableMartFault(
                new MongoSocketReadException("reset", new ServerAddress()))).isTrue();
    }

    @Test
    @DisplayName("DataAccessResourceFailureException(cause=MongoSocketReadException) 1단 체인 → true")
    void dataAccessException_wrapsSocket_returnsTrue() {
        assertThat(PmsMartConfig.retryableMartFault(
                new DataAccessResourceFailureException("wrap",
                        new MongoSocketReadException("reset", new ServerAddress())))).isTrue();
    }

    @Test
    @DisplayName("2단 체인 → true")
    void twoLevelChain_returnsTrue() {
        RuntimeException middle = new RuntimeException("mid",
                new MongoSocketReadException("reset", new ServerAddress()));
        assertThat(PmsMartConfig.retryableMartFault(
                new DataAccessResourceFailureException("outer", middle))).isTrue();
    }

    @Test
    @DisplayName("DataAccessResourceFailureException(cause=MongoTimeoutException) → false — serverSelection 비재시도 회귀 가드")
    void mongoTimeoutException_notSocket_returnsFalse() {
        assertThat(PmsMartConfig.retryableMartFault(
                new DataAccessResourceFailureException("timeout",
                        new MongoTimeoutException("server selection timeout")))).isFalse();
    }

    @Test
    @DisplayName("plain MongoException → false")
    void plainMongoException_returnsFalse() {
        assertThat(PmsMartConfig.retryableMartFault(new MongoException("generic"))).isFalse();
    }

    @Test
    @DisplayName("PmsQueryCancelledException → false")
    void cancelledException_returnsFalse() {
        assertThat(PmsMartConfig.retryableMartFault(new PmsQueryCancelledException())).isFalse();
    }

    @Test
    @DisplayName("CallNotPermittedException → false")
    void callNotPermittedException_returnsFalse() {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(CircuitBreakerConfig.ofDefaults());
        CallNotPermittedException ex = CallNotPermittedException.createCallNotPermittedException(
                registry.circuitBreaker("test"));
        assertThat(PmsMartConfig.retryableMartFault(ex)).isFalse();
    }

    @Test
    @DisplayName("cause 없는 DataAccessResourceFailureException → false")
    void dataAccessException_noSocketCause_returnsFalse() {
        assertThat(PmsMartConfig.retryableMartFault(
                new DataAccessResourceFailureException("down"))).isFalse();
    }
}
