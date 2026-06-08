package com.freightos.pms.application.mart.port.out;

/**
 * Mart 사용 가능 상태를 표시하는 아웃바운드 포트.
 * Application 계층은 이 인터페이스를 통해 Mart readiness를 전환한다.
 * 구현체(PmsMartReadiness)는 adapter/out에 위치한다.
 */
public interface PmsMartReadinessPort {

    /** Mart 빌드 완료 후 호출 — 이후 OLTP 폴백 없이 Mart 경로를 사용한다. */
    void markReady();

    /** 현재 Mart가 사용 가능한 상태인지 반환한다. */
    boolean isReady();
}
