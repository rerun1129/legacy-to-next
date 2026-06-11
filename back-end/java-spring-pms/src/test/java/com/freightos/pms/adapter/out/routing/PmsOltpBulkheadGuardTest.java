package com.freightos.pms.adapter.out.routing;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 JUnit5 — in-memory BulkheadRegistry.
 *
 * 단일 스레드 결정적: acquirePermission() 선점 후 포화·해제 후 정상을 검증한다.
 * 시간·랜덤·sleep 의존 없음.
 */
class PmsOltpBulkheadGuardTest {

    private BulkheadRegistry registry;
    private PmsOltpBulkheadGuard guard;
    private Bulkhead bulkhead;

    @BeforeEach
    void setUp() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(1)
                .maxWaitDuration(Duration.ZERO)
                .build();
        registry = BulkheadRegistry.of(config);
        guard = new PmsOltpBulkheadGuard(registry);
        bulkhead = registry.bulkhead(PmsOltpBulkheadGuard.INSTANCE);
    }

    @Test
    @DisplayName("슬롯 선점 상태에서 execute → BulkheadFullException")
    void whenPermissionTaken_execute_throwsBulkheadFull() {
        bulkhead.acquirePermission();
        try {
            assertThatThrownBy(() -> guard.execute(() -> "X"))
                    .isInstanceOf(BulkheadFullException.class);
        } finally {
            bulkhead.onComplete();
        }
    }

    @Test
    @DisplayName("슬롯 해제 후 execute → 정상 반환")
    void afterPermissionReleased_execute_returnsValue() {
        bulkhead.acquirePermission();
        bulkhead.onComplete();

        String result = guard.execute(() -> "X");

        assertThat(result).isEqualTo("X");
    }
}
