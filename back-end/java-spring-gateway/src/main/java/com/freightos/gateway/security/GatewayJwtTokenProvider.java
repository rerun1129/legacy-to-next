package com.freightos.gateway.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * admin이 발급한 JWT를 동일 secret(HS256)으로 검증하고, refresh rotation 시 재발급하는 역할.
 * admin JwtTokenProvider와 클레임 구조(sub/auth/attr)·해시 방식(SHA-256 hex)을 동일하게 미러한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayJwtTokenProvider {

    private final GatewayJwtProperties properties;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom = new SecureRandom();

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

    /**
     * auth 클레임 문자열 — comma-separated, admin 발급 원본 그대로 반환.
     * admin은 String.join(",", authorities)로 auth를 String 클레임에 저장하지만,
     * JJWT 역직렬화 경로 오판을 방어하기 위해 Object로 수령 후 String으로 변환한다.
     */
    public Optional<String> extractAuthorities(Jws<Claims> jws) {
        Object raw = jws.getPayload().get("auth");
        if (raw == null) return Optional.empty();
        String value = String.valueOf(raw);
        if (value.isBlank()) return Optional.empty();
        return Optional.of(value);
    }

    /**
     * attr 클레임 JSON 문자열 — 없을 수 있으므로 Optional.
     *
     * admin은 Map<String,List<String>> 타입 객체를 claim에 넣으며, JJWT는 JSON 객체를
     * LinkedHashMap으로 역직렬화한다. String 직접 캐스트 시 ClassCastException이 발생하므로
     * Object로 수령하여 타입별로 분기한다.
     * 직렬화 실패 시 empty를 반환(게이트웨이는 enrich-only — attr 누락이 500보다 안전).
     */
    public Optional<String> extractAttr(Jws<Claims> jws) {
        Object raw = jws.getPayload().get("attr");
        if (raw == null) return Optional.empty();
        if (raw instanceof String s) {
            if (s.isBlank()) return Optional.empty();
            return Optional.of(s);
        }
        try {
            return Optional.of(objectMapper.writeValueAsString(raw));
        } catch (JsonProcessingException e) {
            log.warn("attr 클레임 직렬화 실패 — X-Auth-Attr 헤더 생략: {}", e.getMessage());
            return Optional.empty();
        }
    }

    // ── 발급 메서드 (refresh rotation 시 게이트웨이에서 직접 서명) ──────

    /**
     * 새 access token 발급.
     * admin generateAccessToken과 동일한 클레임 구조(sub/auth(CSV)/attr(Map))·TTL·secret.
     */
    public String generateAccessToken(String username, Collection<String> authorities, Map<String, List<String>> attributes) {
        Instant now = Instant.now();
        Instant exp = now.plus(properties.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
            .subject(username)
            .claim("auth", String.join(",", authorities))
            .claim("attr", attributes != null ? attributes : Collections.emptyMap())
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key())
            .compact();
    }

    /**
     * refresh token raw 값 생성 — admin generateRefreshTokenRaw와 동일 방식.
     * SecureRandom 32바이트 → Base64URL(no padding).
     */
    public String generateRefreshTokenRaw() {
        byte[] buf = new byte[32];
        secureRandom.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /**
     * refresh token 해시 — admin hashRefreshToken과 동일 방식.
     * SHA-256 → hex 소문자.
     */
    public String hashRefreshToken(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public long refreshTtlDays() { return properties.getRefreshTokenTtlDays(); }
}
