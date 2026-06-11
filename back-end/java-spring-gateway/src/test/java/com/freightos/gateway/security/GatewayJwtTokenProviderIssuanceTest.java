package com.freightos.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GatewayJwtTokenProvider의 발급(토큰 서명)·해싱 메서드 단위 검증.
 * admin JwtTokenProvider와 동일 클레임 구조·해시 방식을 유지하는지 확인한다.
 * 외부 의존 없이 고정 secret으로 결정적으로 실행된다.
 */
class GatewayJwtTokenProviderIssuanceTest {

    private static final String SECRET = "test-secret-must-be-at-least-32-chars-ok";

    private GatewayJwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        GatewayJwtProperties props = new GatewayJwtProperties();
        props.setSecret(SECRET);
        props.setAccessTokenTtlMinutes(15);
        props.setRefreshTokenTtlDays(14);
        provider = new GatewayJwtTokenProvider(props, new ObjectMapper());
    }

    @Test
    @DisplayName("generateAccessToken — sub/auth/attr 클레임이 admin과 동일 구조로 서명된다")
    void generateAccessTokenHasCorrectClaims() {
        Map<String, List<String>> attrs = Map.of("role", List.of("FMS_USER"), "module", List.of("FMS"));
        Set<String> authorities = Set.of("ROLE_FMS_USER", "MENU_FMS_HOUSE_BL");

        String token = provider.generateAccessToken("alice", authorities, attrs);

        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);

        assertThat(jws.getPayload().getSubject()).isEqualTo("alice");
        // auth 클레임은 CSV 문자열
        String authClaim = (String) jws.getPayload().get("auth");
        assertThat(authClaim).contains("ROLE_FMS_USER");
        assertThat(authClaim).contains("MENU_FMS_HOUSE_BL");
        // attr 클레임은 Map 객체
        Object attrClaim = jws.getPayload().get("attr");
        assertThat(attrClaim).isInstanceOf(Map.class);
    }

    @Test
    @DisplayName("generateAccessToken — attr null이면 빈 Map 클레임이 들어간다")
    void generateAccessTokenNullAttrBecomesEmptyMap() {
        String token = provider.generateAccessToken("bob", List.of("ROLE_USER"), null);

        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);

        Object attrClaim = jws.getPayload().get("attr");
        assertThat(attrClaim).isInstanceOf(Map.class);
        assertThat((Map<?, ?>) attrClaim).isEmpty();
    }

    @Test
    @DisplayName("hashRefreshToken — 동일 입력은 항상 동일 hex 문자열을 반환한다(결정적)")
    void hashRefreshTokenIsDeterministic() {
        String hash1 = provider.hashRefreshToken("fixed-raw-token");
        String hash2 = provider.hashRefreshToken("fixed-raw-token");

        assertThat(hash1).isEqualTo(hash2);
        // SHA-256 결과는 64자 hex 소문자
        assertThat(hash1).hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    @DisplayName("hashRefreshToken — 입력이 다르면 해시도 다르다")
    void hashRefreshTokenDifferentInputsDifferentHashes() {
        String hash1 = provider.hashRefreshToken("token-a");
        String hash2 = provider.hashRefreshToken("token-b");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("generateRefreshTokenRaw — Base64URL 인코딩(no padding), 32바이트 → 43자")
    void generateRefreshTokenRawIsBase64Url() {
        String raw = provider.generateRefreshTokenRaw();

        // 32바이트 Base64URL(no padding) = ceil(32*4/3) = 43자, padding '=' 없음
        assertThat(raw).hasSize(43);
        assertThat(raw).doesNotContain("=");
        // Base64URL 문자셋: A-Z a-z 0-9 - _
        assertThat(raw).matches("[A-Za-z0-9\\-_]+");
    }

    @Test
    @DisplayName("validate — generateAccessToken이 발급한 토큰은 validate로 검증된다")
    void tokenIssuedByGatewayValidatesCorrectly() {
        String token = provider.generateAccessToken("carol", List.of("ROLE_ADMIN"), Map.of());

        assertThat(provider.validate(token)).isPresent();
    }
}
