package com.freightos.pms.adapter.out.routing;

import com.freightos.pms.adapter.out.mart.cancel.PmsQueryCancelledException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Mart(Mongo) 조회를 Retry→CB→TimeLimiter 합성으로 감싸고 실패 시 OLTP로 폴백한다.
 *
 * 합성 순서: Retry(바깥) → CB(중간) → TimeLimiter(안쪽)
 * - Retry 바깥: 시도마다 CB에 실패를 기록(min5 기준 약 3요청 만에 OPEN)
 * - CB 중간: OPEN이면 executor 미진입, 즉시 단락
 * - TimeLimiter 안쪽: 타임아웃이 CB 실패로 집계되도록 PmsMartQueryTimeoutException으로 래핑
 *   (CB recordExceptions 미등재 시 타임아웃이 '성공' 집계 → half-dead Mongo에서 CB 영구 CLOSED)
 *
 * catch 순서 절대 유지(cancel→CallNotPermitted→Timeout→RuntimeException):
 * 전부 RuntimeException 하위이므로 순서가 분기 의미를 결정한다.
 *
 * - PmsQueryCancelledException: 사용자 취소 → 폴백·트립·재시도 금지, 409 핸들러로 전파
 * - CallNotPermittedException: CB OPEN → 재시도 없이 바로 OLTP 폴백
 * - PmsMartQueryTimeoutException: TimeLimiter 예산 초과 → OLTP 폴백, CB 실패 집계
 * - RuntimeException: Mongo 결함 등 그 외 런타임 오류 → OLTP 폴백
 *
 * 프로그래매틱(annotation 미사용): 폴백 예외 집합 정밀 제어 + AOP self-invocation 회피.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
public class PmsMartCircuitGuard {

    /** application.yml resilience4j.*.instances.<이 이름> 과 일치. */
    static final String INSTANCE = "pmsMart";

    private final CircuitBreaker breaker;
    private final Retry retry;
    private final TimeLimiter timeLimiter;
    private final ExecutorService executor;

    public PmsMartCircuitGuard(
            CircuitBreakerRegistry cbRegistry,
            RetryRegistry retryRegistry,
            TimeLimiterRegistry timeLimiterRegistry,
            @Qualifier("pmsMartQueryExecutor") ExecutorService executor) {
        this.breaker = cbRegistry.circuitBreaker(INSTANCE);
        this.retry = retryRegistry.retry(INSTANCE);
        this.timeLimiter = timeLimiterRegistry.timeLimiter(INSTANCE);
        this.executor = executor;

        // yml 인스턴스명 오타 시 조용히 디폴트 설정으로 생성되는 함정을 탐지하기 위해 resolved 값 출력
        log.info("PmsMartCircuitGuard 초기화 — retry.maxAttempts={}, timeLimiter.timeout={}",
                retry.getRetryConfig().getMaxAttempts(),
                timeLimiter.getTimeLimiterConfig().getTimeoutDuration());
    }

    /**
     * mart 우선 실행 → Mongo 결함/차단기 OPEN/타임아웃이면 oltp 폴백.
     * 취소 예외만 예외적으로 그대로 전파한다.
     *
     * 합성: Retry 바깥 → CB 중간 → TimeLimiter(executor) 안쪽
     */
    public <T> T martOrOltp(Supplier<T> mart, Supplier<T> oltp) {
        try {
            return retry.executeSupplier(() -> breaker.executeSupplier(() -> timeBoxed(mart)));
        } catch (PmsQueryCancelledException cancel) {
            throw cancel; // 사용자 취소 → 폴백·트립·재시도 금지, 409 핸들러로 전파
        } catch (CallNotPermittedException open) {
            log.warn("PMS Mart 차단기 OPEN — OLTP 폴백(degraded mode)");
            return oltp.get();
        } catch (PmsMartQueryTimeoutException timeout) {
            log.warn("PMS Mart 조회 타임아웃 상한 초과 — OLTP 폴백(degraded mode): {}", timeout.getMessage());
            return oltp.get();
        } catch (RuntimeException fault) {
            log.warn("PMS Mart 조회 실패 — OLTP 폴백(degraded mode): {}", fault.toString());
            return oltp.get();
        }
    }

    /**
     * TimeLimiter로 Mart 조회를 executor에서 시간 제한 실행한다.
     *
     * checked 예외를 이 메서드에 봉인해 martOrOltp 시그니처를 RuntimeException만 전파하도록 유지한다.
     * TimeLimiter.executeFutureSupplier는 checked TimeoutException을 던지므로
     * PmsMartQueryTimeoutException(RuntimeException)으로 래핑해 CB recordExceptions가 집계하도록 한다.
     */
    private <T> T timeBoxed(Supplier<T> mart) {
        try {
            return timeLimiter.executeFutureSupplier(() -> executor.submit(mart::get));
        } catch (RuntimeException re) {
            throw re; // cancel·Mongo 예외 등 원형(r4j가 ExecutionException cause 언래핑)
        } catch (TimeoutException te) {
            throw new PmsMartQueryTimeoutException(te);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new DataAccessResourceFailureException("mart 실행 중단", ie);
        } catch (Exception other) {
            throw new IllegalStateException(other);
        }
    }
}
