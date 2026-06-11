package com.freightos.gateway.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 게이트웨이 내부 공유키 설정.
 * 다운스트림 모듈이 X-Internal-Token 헤더를 검증하여 게이트웨이 경유 여부를 확인한다.
 */
@Getter
@Setter
@ConfigurationProperties("gateway")
public class GatewayInternalProperties {
    private String internalToken;
}
