package com.freightos.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.Map;
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

        GatewayJwtTokenProvider provider = new GatewayJwtTokenProvider(jwtProps, new ObjectMapper());
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
    @DisplayName("유효 토큰이면 X-Auth-* 3종이 주입되고 X-Internal-Token도 있다 — attr이 Map(admin 실제 발급 형태)인 경우")
    void validTokenInjectsAllAuthHeaders() {
        AtomicReference<HttpHeaders> capturedHeaders = new AtomicReference<>();

        // admin은 Map<String, List<String>> 타입을 attr 클레임에 저장하며
        // JJWT는 이를 JSON 객체로 직렬화한다. 동일 구조로 픽스처를 구성한다.
        Map<String, List<String>> attrMap = Map.of("dept", List.of("IT"), "role", List.of("FMS_USER"));
        String token = buildTokenWithMapAttr("alice", "ROLE_USER,ROLE_FMS", attrMap);
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
        // attr Map은 Jackson으로 JSON 직렬화 후 UTF-8 Base64 인코딩된다.
        // Base64 디코드 결과가 JSON 파싱 가능한 객체 문자열인지 검증한다.
        String encodedAttr = headers.getFirst("X-Auth-Attr");
        assertThat(encodedAttr).isNotNull();
        String decodedAttr = new String(Base64.getDecoder().decode(encodedAttr), StandardCharsets.UTF_8);
        assertThat(decodedAttr).contains("dept").contains("IT");
        assertThat(headers.getFirst("X-Internal-Token")).isEqualTo(INTERNAL_TOKEN);
    }

    @Test
    @DisplayName("attr 클레임이 Map(JSON 객체)인 토큰 — X-Auth-Attr가 Base64 디코드 시 유효한 JSON 객체 문자열이 된다")
    void attrMapClaimIsSerializedToJsonAndBase64Encoded() {
        AtomicReference<HttpHeaders> capturedHeaders = new AtomicReference<>();

        // admin generateAccessToken이 실제로 넣는 구조: Map<String, List<String>>
        Map<String, List<String>> attrMap = Map.of(
            "MENU", List.of("MENU_FMS_HOUSE_BL", "MENU_FMS_MASTER_BL"),
            "BTN", List.of("BTN_FMS_HOUSE_BL_SAVE")
        );
        String token = buildTokenWithMapAttr("carol", "ROLE_FMS", attrMap);
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.GET, "/api/fms/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> {
            capturedHeaders.set(ex.getRequest().getHeaders());
            return Mono.empty();
        };

        filter.filter(exchange, chain).block();

        HttpHeaders headers = capturedHeaders.get();
        assertThat(headers.getFirst("X-Auth-User")).isEqualTo("carol");
        assertThat(headers.getFirst("X-Auth-Authorities")).isEqualTo("ROLE_FMS");

        String encodedAttr = headers.getFirst("X-Auth-Attr");
        assertThat(encodedAttr).isNotNull();

        // Base64 디코드 후 JSON 파싱 가능한 객체이어야 한다
        String decodedAttr = new String(Base64.getDecoder().decode(encodedAttr), StandardCharsets.UTF_8);
        assertThat(decodedAttr).startsWith("{");
        assertThat(decodedAttr).contains("MENU");
        assertThat(decodedAttr).contains("BTN");
        assertThat(headers.getFirst("X-Internal-Token")).isEqualTo(INTERNAL_TOKEN);
    }

    @Test
    @DisplayName("attr 클레임이 String인 토큰 — X-Auth-Attr에 Base64 인코딩된 원본 문자열이 주입된다")
    void attrStringClaimIsBase64Encoded() {
        AtomicReference<HttpHeaders> capturedHeaders = new AtomicReference<>();

        String attrJson = "{\"dept\":\"IT\"}";
        String token = buildToken("dave", "ROLE_USER", attrJson);
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
        assertThat(headers.getFirst("X-Auth-User")).isEqualTo("dave");
        String encodedAttr = headers.getFirst("X-Auth-Attr");
        assertThat(encodedAttr).isNotNull();
        String decodedAttr = new String(Base64.getDecoder().decode(encodedAttr), StandardCharsets.UTF_8);
        assertThat(decodedAttr).isEqualTo(attrJson);
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

    /**
     * attr 클레임에 Map을 넣는 헬퍼 — admin generateAccessToken의 실제 발급 형태와 일치.
     * JJWT는 Map을 JSON 객체로 직렬화하고, 파싱 시 LinkedHashMap으로 역직렬화한다.
     */
    private String buildTokenWithMapAttr(String username, String auth, Map<String, List<String>> attrMap) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(username)
                .claim("auth", auth)
                .claim("attr", attrMap)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    /** attr 클레임에 String을 넣는 헬퍼 — extractAttr instanceof String 분기 검증용. */
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
