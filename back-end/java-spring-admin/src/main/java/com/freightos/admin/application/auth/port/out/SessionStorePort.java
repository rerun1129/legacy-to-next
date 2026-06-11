package com.freightos.admin.application.auth.port.out;

import com.freightos.admin.application.auth.SessionBundle;

public interface SessionStorePort {
    /**
     * refresh token hash를 키로 세션 번들을 저장한다.
     * Redis 장애 시 호출자가 warn 로그 후 계속 진행할 수 있도록 unchecked exception으로 전파한다.
     */
    void saveSession(String refreshTokenHash, SessionBundle bundle, long ttlDays);
}
