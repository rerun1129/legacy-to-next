package com.freightos.gateway.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 검증 및 내부 토큰 설정.
 * jwt.secret — admin과 공유하는 HS256 대칭키.
 * gateway.internal-token — 다운스트림 모듈이 게이트웨이 경유 여부를 확인하는 내부 공유키.
 */
@Getter
@Setter
@ConfigurationProperties("jwt")
public class GatewayJwtProperties {
    private String secret;
}
