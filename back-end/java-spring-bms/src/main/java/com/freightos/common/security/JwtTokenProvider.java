package com.freightos.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * FMS는 토큰 발급을 담당하지 않는다. admin이 발급한 JWT를 동일 secret(HS256)으로 검증하는 역할만 수행.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties properties;

    SecretKey key() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public Jws<Claims> validateAccessToken(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
    }

    public String extractUsername(Jws<Claims> jws) {
        return jws.getPayload().getSubject();
    }

    public List<String> extractAuthorities(Jws<Claims> jws) {
        String raw = (String) jws.getPayload().get("auth");
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.asList(raw.split(","));
    }
}
