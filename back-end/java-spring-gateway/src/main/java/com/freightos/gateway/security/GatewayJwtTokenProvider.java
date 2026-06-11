package com.freightos.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * admin이 발급한 JWT를 동일 secret(HS256)으로 검증하는 역할만 수행.
 * 토큰 발급은 게이트웨이 책임 범위 밖이다.
 */
@Component
@RequiredArgsConstructor
public class GatewayJwtTokenProvider {

    private final GatewayJwtProperties properties;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 토큰이 유효하면 Claims를 반환, 서명 오류·만료 등 검증 실패면 empty를 반환.
     * 예외를 호출자에 노출하지 않으므로 GlobalFilter에서 분기 없이 orElse 처리 가능.
     */
    public Optional<Jws<Claims>> validate(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(key()).build().parseSignedClaims(token));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String extractUsername(Jws<Claims> jws) {
        return jws.getPayload().getSubject();
    }

    /** auth 클레임 문자열 — comma-separated, admin 발급 원본 그대로 반환 */
    public Optional<String> extractAuthorities(Jws<Claims> jws) {
        String raw = (String) jws.getPayload().get("auth");
        if (raw == null || raw.isBlank()) return Optional.empty();
        return Optional.of(raw);
    }

    /** attr 클레임 JSON 문자열 — 없을 수 있으므로 Optional */
    public Optional<String> extractAttr(Jws<Claims> jws) {
        String raw = (String) jws.getPayload().get("attr");
        if (raw == null || raw.isBlank()) return Optional.empty();
        return Optional.of(raw);
    }
}
