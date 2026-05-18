package com.freightos.admin.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-must-be-at-least-32-chars!!");
        props.setAccessTokenTtlMinutes(15);
        props.setRefreshTokenTtlDays(14);
        provider = new JwtTokenProvider(props);
    }

    // ── access token 생성/검증 round-trip ──────────────────────────────────────

    @Test
    void generateAndValidate_accessToken_roundTrip() {
        List<String> authorities = List.of("ROLE_ADMIN", "MENU_ADMIN_CODE_LIST");

        String token = provider.generateAccessToken("admin", authorities, java.util.Map.of("role", List.of("ADMIN")));
        Jws<Claims> jws = provider.validateAccessToken(token);

        assertThat(provider.extractUsername(jws)).isEqualTo("admin");
        assertThat(provider.extractAuthorities(jws)).containsExactlyInAnyOrderElementsOf(authorities);
    }

    @Test
    void extractAuthorities_emptyAuth_returnsEmptyList() {
        String token = provider.generateAccessToken("alice", List.of(), java.util.Collections.emptyMap());
        Jws<Claims> jws = provider.validateAccessToken(token);

        assertThat(provider.extractAuthorities(jws)).isEmpty();
    }

    // ── attr claim round-trip ─────────────────────────────────────────────────

    @Test
    void extractAttributes_withAttrs_returnsMap() {
        java.util.Map<String, List<String>> attrs = java.util.Map.of("role", List.of("ADMIN"));
        String token = provider.generateAccessToken("admin", List.of("ROLE_ADMIN"), attrs);
        Jws<Claims> jws = provider.validateAccessToken(token);

        java.util.Map<String, List<String>> extracted = provider.extractAttributes(jws);
        assertThat(extracted).containsKey("role");
    }

    // ── 만료 토큰 검증 실패 ────────────────────────────────────────────────────

    @Test
    void validateAccessToken_expiredToken_throwsJwtException() {
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret("test-secret-key-must-be-at-least-32-chars!!");
        // TTL을 음수로 설정하면 이미 만료된 토큰이 생성된다.
        expiredProps.setAccessTokenTtlMinutes(-1);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);

        String expiredToken = expiredProvider.generateAccessToken("admin", List.of(), java.util.Collections.emptyMap());

        // 검증 시 만료 예외 발생 확인
        assertThatThrownBy(() -> provider.validateAccessToken(expiredToken))
            .isInstanceOf(JwtException.class);
    }

    // ── refresh token 생성 및 hash ────────────────────────────────────────────

    @Test
    void generateRefreshTokenRaw_isDifferentEachCall() {
        String raw1 = provider.generateRefreshTokenRaw();
        String raw2 = provider.generateRefreshTokenRaw();

        assertThat(raw1).isNotEqualTo(raw2);
    }

    @Test
    void hashRefreshToken_sameInput_sameOutput() {
        String raw = provider.generateRefreshTokenRaw();
        String hash1 = provider.hashRefreshToken(raw);
        String hash2 = provider.hashRefreshToken(raw);

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64); // SHA-256 hex = 32 bytes = 64 hex chars
    }
}
