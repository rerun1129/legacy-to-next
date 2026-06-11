package com.freightos.pms.adapter.out.routing;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * OLTP 폴백 경로 동시 호출 수를 세마포어로 제한한다.
 *
 * CB OPEN(degraded) 시 Mart 트래픽이 전부 Postgres(Hikari 10)로 몰려
 * 커넥션 풀 고갈·CPU 스파이크로 이어지는 것을 방지한다.
 * 세마포어 5 = Hikari 10 중 검색 할당 5 + full rebuild ETL parallelism 4 + 여유 1.
 * 초과 시 BulkheadFullException → GlobalExceptionHandler가 503으로 변환한다.
 *
 * pms.mart.enabled=false이면 라우터 자체가 미생성되므로 Bulkhead도 불필요하다.
 * 해당 경우 OLTP가 Bulkhead 없이 노출되는 것은 의도된 동작이다(단일 어댑터 직접 사용).
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
public class PmsOltpBulkheadGuard {

    /** application.yml resilience4j.bulkhead.instances.<이 이름> 과 일치. */
    static final String INSTANCE = "pmsOltpSearch";

    private final Bulkhead bulkhead;

    public PmsOltpBulkheadGuard(BulkheadRegistry registry) {
        this.bulkhead = registry.bulkhead(INSTANCE);
    }

    /**
     * OLTP 호출을 세마포어 Bulkhead로 감싸 실행한다.
     * 허용 슬롯 초과 시 BulkheadFullException이 발생한다.
     */
    public <T> T execute(Supplier<T> oltpCall) {
        return bulkhead.executeSupplier(oltpCall);
    }
}
