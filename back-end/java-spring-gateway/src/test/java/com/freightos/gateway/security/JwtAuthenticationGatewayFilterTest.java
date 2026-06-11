package com.freightos.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtAuthenticationGatewayFilter 단위 테스트.
 * 외부 의존 없이 고정 secret으로 토큰을 생성하여 결정적으로 검증한다.
 */
class JwtAuthenticationGatewayFilterTest {

    private static final String SECRET = "test-secret-must-be-at-least-32-chars-ok";
    private static final String INTERNAL_TOKEN = "test-internal-token";

    private JwtAuthenticationGatewayFilter filter;

    @BeforeEach
    void setUp() {
        GatewayJwtProperties jwtProps = new GatewayJwtProperties();
        jwtProps.setSecret(SECRET);

        GatewayInternalProperties internalProps = new GatewayInternalProperties();
        internalProps.setInternalToken(INTERNAL_TOKEN);

        GatewayJwtTokenProvider provider = new GatewayJwtTokenProvider(jwtProps);
        filter = new JwtAuthenticationGatewayFilter(provider, internalProps);
    }

    @Test
    @DisplayName("클라이언트가 보낸 X-Auth-* 위조 헤더는 모두 제거된다")
    void forgedAuthHeadersAreStripped() {
        AtomicReference<HttpHeaders> capturedHeaders = new AtomicReference<>();

        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/api/test")
                .header("X-Auth-User", "hacker")
                .header("X-Auth-Authorities", "ROLE_ADMIN")
                .header("X-Auth-Attr", "forged-attr")
                .header("X-Internal-Token", "forged-internal")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> {
            capturedHeaders.set(ex.getRequest().getHeaders());
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        HttpHeaders headers = capturedHeaders.get();
        assertThat(headers.getFirst("X-Auth-User")).isNull();
        assertThat(headers.getFirst("X-Auth-Authorities")).isNull();
        assertThat(headers.getFirst("X-Auth-Attr")).isNull();
        // 내부 토큰은 게이트웨이 발급으로 교체됨
        assertThat(headers.getFirst("X-Internal-Token")).isEqualTo(INTERNAL_TOKEN);
    }

    @Test
    @DisplayName("유효 토큰이면 X-Auth-* 3종이 주입되고 X-Internal-Token도 있다")
    void validTokenInjectsAllAuthHeaders() {
        AtomicReference<HttpHeaders> capturedHeaders = new AtomicReference<>();

        String token = buildToken("alice", "ROLE_USER,ROLE_FMS", "{\"dept\":\"IT\"}");
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> {
            capturedHeaders.set(ex.getRequest().getHeaders());
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        HttpHeaders headers = capturedHeaders.get();
        assertThat(headers.getFirst("X-Auth-User")).isEqualTo("alice");
        assertThat(headers.getFirst("X-Auth-Authorities")).isEqualTo("ROLE_USER,ROLE_FMS");
        // attr는 UTF-8 Base64 인코딩 값이어야 한다
        String expectedAttr = Base64.getEncoder().encodeToString("{\"dept\":\"IT\"}".getBytes(StandardCharsets.UTF_8));
        assertThat(headers.getFirst("X-Auth-Attr")).isEqualTo(expectedAttr);
        assertThat(headers.getFirst("X-Internal-Token")).isEqualTo(INTERNAL_TOKEN);
    }

    @Test
    @DisplayName("attr 클레임이 없는 유효 토큰이면 X-Auth-Attr은 주입되지 않는다")
    void validTokenWithoutAttrOmitsAttrHeader() {
        AtomicReference<HttpHeaders> capturedHeaders = new AtomicReference<>();

        String token = buildTokenNoAttr("bob", "ROLE_USER");
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> {
            capturedHeaders.set(ex.getRequest().getHeaders());
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        HttpHeaders headers = capturedHeaders.get();
        assertThat(headers.getFirst("X-Auth-User")).isEqualTo("bob");
        assertThat(headers.getFirst("X-Auth-Authorities")).isEqualTo("ROLE_USER");
        assertThat(headers.getFirst("X-Auth-Attr")).isNull();
        assertThat(headers.getFirst("X-Internal-Token")).isEqualTo(INTERNAL_TOKEN);
    }

    @Test
    @DisplayName("무효 토큰이면 X-Auth-* 주입 없이 X-Internal-Token만 있다")
    void invalidTokenSkipsAuthHeadersButInjectsInternalToken() {
        AtomicReference<HttpHeaders> capturedHeaders = new AtomicReference<>();

        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/api/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer this.is.invalid")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> {
            capturedHeaders.set(ex.getRequest().getHeaders());
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        HttpHeaders headers = capturedHeaders.get();
        assertThat(headers.getFirst("X-Auth-User")).isNull();
        assertThat(headers.getFirst("X-Auth-Authorities")).isNull();
        assertThat(headers.getFirst("X-Auth-Attr")).isNull();
        assertThat(headers.getFirst("X-Internal-Token")).isEqualTo(INTERNAL_TOKEN);
    }

    @Test
    @DisplayName("토큰이 없으면 X-Auth-* 주입 없이 X-Internal-Token만 있다")
    void noTokenSkipsAuthHeadersButInjectsInternalToken() {
        AtomicReference<HttpHeaders> capturedHeaders = new AtomicReference<>();

        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/api/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> {
            capturedHeaders.set(ex.getRequest().getHeaders());
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        HttpHeaders headers = capturedHeaders.get();
        assertThat(headers.getFirst("X-Auth-User")).isNull();
        assertThat(headers.getFirst("X-Auth-Authorities")).isNull();
        assertThat(headers.getFirst("X-Auth-Attr")).isNull();
        assertThat(headers.getFirst("X-Internal-Token")).isEqualTo(INTERNAL_TOKEN);
    }

    // ── 테스트용 토큰 생성 헬퍼 ──────────────────────────────────────────

    private String buildToken(String username, String auth, String attr) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(username)
                .claim("auth", auth)
                .claim("attr", attr)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    private String buildTokenNoAttr(String username, String auth) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(username)
                .claim("auth", auth)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }
}
