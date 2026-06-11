package com.freightos.pms.adapter.out.routing;

import com.freightos.common.config.PmsMartConfig;
import com.freightos.common.config.PmsMartProperties;
import com.freightos.pms.adapter.out.mart.PmsMartFilterSupport;
import com.freightos.pms.adapter.out.mart.PmsMartQueryAdapter;
import com.freightos.pms.adapter.out.persistence.pms.PmsPerformancePersistenceAdapter;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.PmsRawBlSearchResult;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.dao.DataAccessResourceFailureException;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

/**
 * 순수 JUnit5 + Mockito BDD 스타일 — 라우터 Bulkhead 통합 단위 테스트.
 *
 * 실물 PmsOltpBulkheadGuard(in-memory registry, max=1·wait=0) + 실물 PmsMartCircuitGuard 사용.
 * 포트 2개(mart·oltp)는 mock.
 */
class PmsPerformanceQueryRouterBulkheadTest {

    private PmsPerformancePersistenceAdapter oltpPort;
    private PmsMartQueryAdapter martPort;
    private PmsMartFilterSupport filterSupport;
    private PmsMartProperties props;
    private PmsOltpBulkheadGuard oltpBulkhead;
    private PmsMartCircuitGuard guard;
    private Bulkhead bulkhead;
    private BulkheadRegistry bulkheadRegistry;
    private CircuitBreakerRegistry cbRegistry;
    private PmsPerformanceQueryRouter router;
    private ExecutorService executor;

    @BeforeEach
    void setUp() {
        oltpPort = mock(PmsPerformancePersistenceAdapter.class);
        martPort = mock(PmsMartQueryAdapter.class);
        filterSupport = mock(PmsMartFilterSupport.class);
        props = new PmsMartProperties();

        BulkheadConfig bulkheadConfig = BulkheadConfig.custom()
                .maxConcurrentCalls(1)
                .maxWaitDuration(Duration.ZERO)
                .build();
        bulkheadRegistry = BulkheadRegistry.of(bulkheadConfig);
        oltpBulkhead = new PmsOltpBulkheadGuard(bulkheadRegistry);
        bulkhead = bulkheadRegistry.bulkhead(PmsOltpBulkheadGuard.INSTANCE);

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(50)
                .automaticTransitionFromOpenToHalfOpenEnabled(false)
                .recordException(ex ->
                        ex instanceof org.springframework.dao.DataAccessException
                        || ex instanceof PmsMartQueryTimeoutException)
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

        router = new PmsPerformanceQueryRouter(
                oltpPort, martPort, filterSupport, props, guard, oltpBulkhead);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    private SearchPmsPerformanceCommand command() {
        // basis, page, size, jobDiv, bound, dateKind, dateFrom, dateTo,
        // performanceDtFrom, performanceDtTo, documentDtFrom, documentDtTo,
        // documentTypes, documentStatus, exactCount, searchNonce
        return new SearchPmsPerformanceCommand(
                AggregationBasis.FREIGHT_INPUT, 0, 20,
                null, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }

    @Test
    @DisplayName("useMart=false 직행 경로도 Bulkhead 통과 — 포화 시 BulkheadFullException 전파·oltp 미호출")
    void directOltpPath_bulkheadSaturated_throwsBulkheadFull() {
        // useMart=false — filterSupport가 false 반환, martOnly도 false
        given(filterSupport.supportedByMart(any())).willReturn(false);

        // Bulkhead 선점
        bulkhead.acquirePermission();
        try {
            assertThatThrownBy(() -> router.searchByFreightLine(command(), PageRequest.of(0, 20)))
                    .isInstanceOf(BulkheadFullException.class);

            then(oltpPort).should(never()).searchByFreightLine(any(), any());
        } finally {
            bulkhead.onComplete();
        }
    }

    @Test
    @DisplayName("useMart=true·mart 실패·Bulkhead 포화 → BulkheadFullException 전파, CB failedCalls는 mart 실패 1건만")
    void martFailsAndBulkheadSaturated_throwsBulkheadFull_cbOnlyCountsMartFault() {
        given(filterSupport.supportedByMart(any())).willReturn(true);

        // mart는 DataAccessResourceFailureException throw → OLTP 폴백 시도, 그 시점에 Bulkhead 포화
        given(martPort.searchByFreightLine(any(), any()))
                .willThrow(new DataAccessResourceFailureException("mart blip"));

        Pageable pageable = PageRequest.of(0, 20);
        bulkhead.acquirePermission();
        try {
            assertThatThrownBy(() -> router.searchByFreightLine(command(), pageable))
                    .isInstanceOf(BulkheadFullException.class);
        } finally {
            bulkhead.onComplete();
        }

        // mart 실패만 CB에 기록 — BulkheadFull은 폴백 catch 밖이라 CB 기록 불가
        long failed = cbRegistry.circuitBreaker(PmsMartCircuitGuard.INSTANCE)
                .getMetrics().getNumberOfFailedCalls();
        assertThat(failed).isEqualTo(1);
    }
}
