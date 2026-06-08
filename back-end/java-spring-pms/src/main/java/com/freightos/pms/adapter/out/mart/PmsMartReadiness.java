package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.application.mart.port.out.PmsMartReadinessPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Mart 사용 가능(ready) 상태를 추적하는 아웃바운드 포트 구현체.
 *
 * pms_bl_mart 컬렉션이 완전히 빌드된 상태인지를 인메모리 플래그로 나타낸다.
 * 빌드 전·빌드 중에는 false → PmsMartFilterSupport가 OLTP 폴백을 선택한다.
 * PmsMartBootstrapRunner(기동 자동 빌드) 또는 PmsMartMaintenanceService(수동 rebuild)
 * 완료 시점에 markReady()를 호출해 true로 전환한다.
 *
 * @ConditionalOnProperty는 PmsMartFilterSupport와 동일 조건이므로
 * 두 빈이 항상 동반 등록된다.
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
public class PmsMartReadiness implements PmsMartReadinessPort {

    private volatile boolean ready = false;

    @Override
    public void markReady() {
        ready = true;
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
