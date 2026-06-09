package com.freightos.pms.adapter.out.routing;

import com.freightos.pms.adapter.out.mart.cancel.PmsQueryCancelledException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Mart(Mongo) 조회를 회로차단기로 감싸고 실패 시 OLTP로 폴백한다.
 *
 * - mart supplier만 차단기로 감싼다(oltp 분기는 차단기 밖) → Mongo 결함만 집계.
 * - 프로그래매틱(annotation 미사용): 폴백 예외 집합 정밀 제어 + AOP self-invocation 회피.
 * - PmsQueryCancelledException(사용자 취소 = 정상 제어흐름)은 폴백/트립 대상이 아니라 그대로 전파(→409).
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
public class PmsMartCircuitGuard {

    /** application.yml resilience4j.circuitbreaker.instances.<이 이름> 과 일치. */
    static final String INSTANCE = "pmsMart";

    private final CircuitBreaker breaker;

    public PmsMartCircuitGuard(CircuitBreakerRegistry registry) {
        this.breaker = registry.circuitBreaker(INSTANCE);
    }

    /**
     * mart 우선 실행 → Mongo 결함/차단기 OPEN이면 oltp 폴백.
     * 취소 예외만 예외적으로 그대로 전파한다.
     */
    public <T> T martOrOltp(Supplier<T> mart, Supplier<T> oltp) {
        try {
            return breaker.executeSupplier(mart);
        } catch (PmsQueryCancelledException cancel) {
            throw cancel; // 사용자 취소 → 폴백·트립 금지, 409 핸들러로 전파
        } catch (CallNotPermittedException open) {
            log.warn("PMS Mart 차단기 OPEN — OLTP 폴백(degraded mode)");
            return oltp.get();
        } catch (RuntimeException fault) {
            log.warn("PMS Mart 조회 실패 — OLTP 폴백(degraded mode): {}", fault.toString());
            return oltp.get();
        }
    }
}
