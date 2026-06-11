package com.freightos.pms.adapter.out.routing;

import java.util.concurrent.TimeoutException;

/**
 * TimeLimiter 타임아웃의 런타임 래퍼.
 *
 * TimeLimiter.executeFutureSupplier는 checked java.util.concurrent.TimeoutException을 던진다.
 * RuntimeException 경계 밖으로 전파하기 위해 이 클래스로 래핑한다.
 *
 * ⚠️ application.yml CB recordExceptions 등재 필수.
 * 미등재 시 타임아웃이 '성공'으로 집계되어 half-dead Mongo 환경에서 CB가 영구 CLOSED 상태로 남는다.
 */
public class PmsMartQueryTimeoutException extends RuntimeException {

    public PmsMartQueryTimeoutException(TimeoutException cause) {
        super("Mart 조회 타임아웃 — TimeLimiter 예산 초과", cause);
    }
}
