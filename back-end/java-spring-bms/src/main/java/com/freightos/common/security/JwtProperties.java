package com.freightos.common.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenTtlMinutes = 15;
    private long refreshTokenTtlDays = 14;
}
