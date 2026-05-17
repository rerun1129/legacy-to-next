package com.freightos.admin.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final SecureRandom random = new SecureRandom();

    private SecretKey key() {
        return Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, Collection<String> authorities) {
        Instant now = Instant.now();
        Instant exp = now.plus(properties.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
            .subject(username)
            .claim("auth", String.join(",", authorities))
            .issuedAt(Date.from(now))
            .expiration(Date.from(exp))
            .signWith(key())
            .compact();
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

    public String generateRefreshTokenRaw() {
        byte[] buf = new byte[32];
        random.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

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
