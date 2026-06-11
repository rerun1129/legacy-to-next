package com.freightos.gateway.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 검증·발급 설정.
 * jwt.secret — admin과 공유하는 HS256 대칭키.
 * access-token-ttl-minutes / refresh-token-ttl-days — admin 기본값과 동일(15m/14d).
 */
@Getter
@Setter
@ConfigurationProperties("jwt")
public class GatewayJwtProperties {
    private String secret;
    private long accessTokenTtlMinutes = 15;
    private long refreshTokenTtlDays = 14;
}
