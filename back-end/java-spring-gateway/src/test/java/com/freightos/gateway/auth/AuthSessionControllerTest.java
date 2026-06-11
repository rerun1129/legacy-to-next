package com.freightos.gateway.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.gateway.security.GatewayJwtProperties;
import com.freightos.gateway.security.GatewayJwtTokenProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AuthSessionController 단위 검증.
 * ReactiveStringRedisTemplate을 mock으로 대체하여 외부 Redis 없이 결정적으로 실행한다.
 * CircuitBreakerRegistry는 기본 인스턴스(CLOSED 상태)로 사용하여 정상 경로를 통과시킨다.
 */
class AuthSessionControllerTest {

    private static final String SECRET = "test-secret-must-be-at-least-32-chars-ok";

    private AuthSessionController controller;
    private ReactiveStringRedisTemplate redisTemplate;
    private ReactiveValueOperations<String, String> valueOps;
    private GatewayJwtTokenProvider jwtTokenProvider;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        GatewayJwtProperties props = new GatewayJwtProperties();
        props.setSecret(SECRET);
        props.setAccessTokenTtlMinutes(15);
        props.setRefreshTokenTtlDays(14);
        jwtTokenProvider = new GatewayJwtTokenProvider(props, new ObjectMapper());
        objectMapper = new ObjectMapper();

        redisTemplate = mock(ReactiveStringRedisTemplate.class);
        valueOps = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        controller = new AuthSessionController(redisTemplate, jwtTokenProvider, objectMapper, registry);
    }

    @Test
    @DisplayName("refresh — 세션 존재 시 새 accessToken/refreshToken을 200으로 반환한다")
    void refreshWithValidSession() throws Exception {
        String rawToken = "old-refresh-raw-fixture";
        String hash = jwtTokenProvider.hashRefreshToken(rawToken);
        String sessionKey = "auth:session:" + hash;

        String bundleJson = objectMapper.writeValueAsString(
            new com.freightos.gateway.auth.SessionBundle(
                "alice",
                "ROLE_FMS_USER,MENU_FMS_HOUSE_BL",
                Map.of("role", List.of("FMS_USER"))
            )
        );

        when(valueOps.getAndDelete(sessionKey)).thenReturn(Mono.just(bundleJson));
        when(valueOps.set(any(String.class), eq(bundleJson), any(Duration.class))).thenReturn(Mono.just(true));

        Mono<ResponseEntity<com.freightos.gateway.common.ApiResponse<AuthSessionController.RefreshResponse>>> result =
            controller.refresh(new AuthSessionController.RefreshRequest(rawToken));

        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getData()).isNotNull();
                assertThat(response.getBody().getData().accessToken()).isNotBlank();
                assertThat(response.getBody().getData().refreshToken()).isNotBlank();
                // rotation: 새 refresh token은 원래와 달라야 한다
                assertThat(response.getBody().getData().refreshToken()).isNotEqualTo(rawToken);
                assertThat(response.getBody().getMessage()).isEqualTo("토큰이 갱신되었습니다.");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("refresh — 세션이 없으면 401을 반환한다")
    void refreshWithNoSessionReturns401() {
        String rawToken = "nonexistent-token";
        String hash = jwtTokenProvider.hashRefreshToken(rawToken);
        String sessionKey = "auth:session:" + hash;

        when(valueOps.getAndDelete(sessionKey)).thenReturn(Mono.empty());

        Mono<ResponseEntity<com.freightos.gateway.common.ApiResponse<AuthSessionController.RefreshResponse>>> result =
            controller.refresh(new AuthSessionController.RefreshRequest(rawToken));

        StepVerifier.create(result)
            .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
            .verifyComplete();
    }

    @Test
    @DisplayName("refresh — Redis 예외 시 503을 반환한다")
    void refreshWithRedisErrorReturns503() {
        String rawToken = "error-token";
        String hash = jwtTokenProvider.hashRefreshToken(rawToken);
        String sessionKey = "auth:session:" + hash;

        when(valueOps.getAndDelete(sessionKey)).thenReturn(Mono.error(new RuntimeException("Redis connection refused")));

        Mono<ResponseEntity<com.freightos.gateway.common.ApiResponse<AuthSessionController.RefreshResponse>>> result =
            controller.refresh(new AuthSessionController.RefreshRequest(rawToken));

        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getMessage()).contains("세션 저장소");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("logout — 항상 200 OK를 반환한다(멱등)")
    void logoutAlwaysReturns200() {
        String rawToken = "logout-token";
        String hash = jwtTokenProvider.hashRefreshToken(rawToken);
        String sessionKey = "auth:session:" + hash;

        when(redisTemplate.delete(sessionKey)).thenReturn(Mono.just(1L));

        Mono<ResponseEntity<com.freightos.gateway.common.ApiResponse<Void>>> result =
            controller.logout(new AuthSessionController.LogoutRequest(rawToken));

        StepVerifier.create(result)
            .assertNext(response -> {
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getMessage()).isEqualTo("로그아웃되었습니다.");
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("logout — 세션이 없어도(DEL=0) 200 OK를 반환한다(멱등)")
    void logoutWithAbsentSessionStillReturns200() {
        String rawToken = "already-logged-out";
        String hash = jwtTokenProvider.hashRefreshToken(rawToken);
        String sessionKey = "auth:session:" + hash;

        when(redisTemplate.delete(sessionKey)).thenReturn(Mono.just(0L));

        Mono<ResponseEntity<com.freightos.gateway.common.ApiResponse<Void>>> result =
            controller.logout(new AuthSessionController.LogoutRequest(rawToken));

        StepVerifier.create(result)
            .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK))
            .verifyComplete();
    }
}
