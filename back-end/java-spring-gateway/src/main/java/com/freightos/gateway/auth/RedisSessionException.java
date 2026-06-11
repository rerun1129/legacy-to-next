package com.freightos.gateway.auth;

/**
 * 세션 처리(Redis) 중 발생하는 예외 — 503 응답으로 매핑된다.
 */
class RedisSessionException extends RuntimeException {

    RedisSessionException(String message, Throwable cause) {
        super(message, cause);
    }
}
