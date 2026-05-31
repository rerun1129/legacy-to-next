package com.freightos.admin.application.auth.port.out;

import java.time.LocalDate;
import java.util.Set;

/** 로그인 시점 구독 유효성 판단을 위한 아웃바운드 포트. */
public interface SubscriptionQueryPort {

    /**
     * 특정 고객사의 오늘 기준 유효 모듈 코드 집합을 반환한다.
     * active=true AND startDate <= today AND endDate >= today 조건.
     */
    Set<String> findValidModuleCodes(Long subscriberId, LocalDate today);
}
