package com.freightos.admin.common.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("gateway")
public class GatewayProperties {
    private String internalToken = "dev-internal-gateway-key-change-me";
}
