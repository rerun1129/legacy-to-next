package com.freightos.gateway.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.gateway.common.ApiResponse;
import com.freightos.gateway.security.GatewayJwtTokenProvider;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * refresh/logout 엔드포인트를 게이트웨이에서 로컬 처리한다.
 *
 * SCG의 RequestMappingHandlerMapping(order=0)이 RoutePredicateHandlerMapping(order=1)보다
 * 먼저 매핑되므로 /api/admin/auth/refresh, /api/admin/auth/logout은 admin 프록시 라우트로
 * 전달되지 않고 이 컨트롤러가 처리한다.
 *
 * FE 무변경 조건: 요청·응답 계약을 admin 현행과 byte-수준으로 미러한다.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthSessionController {

    private static final String SESSION_KEY_PREFIX = "auth:session:";
    private static final String REFRESH_OK_MSG = "토큰이 갱신되었습니다.";
    private static final String LOGOUT_OK_MSG = "로그아웃되었습니다.";
    private static final String STORAGE_ERROR_MSG = "세션 저장소가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해주세요.";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final GatewayJwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record RefreshResponse(String accessToken, String refreshToken) {}
    public record LogoutRequest(@NotBlank String refreshToken) {}

    /**
     * refresh token rotation.
     * GETDEL(원자적) → 번들 파싱 → 새 refresh 생성 → SET+TTL → 새 access 발급.
     * 세션 없음: 401, Redis 장애: 503.
     */
    @PostMapping("/refresh")
    public Mono<ResponseEntity<ApiResponse<RefreshResponse>>> refresh(@Valid @RequestBody RefreshRequest req) {
        String oldHash = jwtTokenProvider.hashRefreshToken(req.refreshToken());
        String sessionKey = SESSION_KEY_PREFIX + oldHash;

        Mono<ResponseEntity<ApiResponse<RefreshResponse>>> operation = redisTemplate.opsForValue()
            .getAndDelete(sessionKey)
            .flatMap(json -> buildRotatedResponse(json))
            .switchIfEmpty(Mono.just(invalidTokenResponse()));

        return operation
            .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("gatewaySession")))
            .onErrorResume(CallNotPermittedException.class, e -> Mono.just(storageUnavailableResponse()))
            .onErrorResume(Exception.class, e -> {
                log.warn("세션 처리 중 오류: {}", e.getMessage());
                return Mono.just(storageUnavailableResponse());
            });
    }

    /**
     * 로그아웃 — DEL(멱등).
     * Redis 장애 시 503 반환. 세션이 없어도 200 OK(멱등).
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResponse<Void>>> logout(@Valid @RequestBody LogoutRequest req) {
        String hash = jwtTokenProvider.hashRefreshToken(req.refreshToken());
        String sessionKey = SESSION_KEY_PREFIX + hash;

        Mono<ResponseEntity<ApiResponse<Void>>> operation = redisTemplate.delete(sessionKey)
            .thenReturn(ResponseEntity.ok(ApiResponse.<Void>ok(LOGOUT_OK_MSG)));

        return operation
            .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("gatewaySession")))
            .onErrorResume(CallNotPermittedException.class, e -> Mono.just(logoutStorageUnavailableResponse()))
            .onErrorResume(Exception.class, e -> {
                log.warn("로그아웃 중 Redis 오류: {}", e.getMessage());
                return Mono.just(logoutStorageUnavailableResponse());
            });
    }

    private Mono<ResponseEntity<ApiResponse<RefreshResponse>>> buildRotatedResponse(String json) {
        SessionBundle bundle;
        try {
            bundle = objectMapper.readValue(json, SessionBundle.class);
        } catch (JsonProcessingException e) {
            log.error("세션 번들 역직렬화 실패 — 세션 키 형식 불일치: {}", e.getMessage());
            return Mono.error(new RedisSessionException("세션 번들 파싱 실패", e));
        }

        String newRefreshRaw = jwtTokenProvider.generateRefreshTokenRaw();
        String newHash = jwtTokenProvider.hashRefreshToken(newRefreshRaw);
        String newSessionKey = SESSION_KEY_PREFIX + newHash;
        Duration ttl = Duration.ofDays(jwtTokenProvider.refreshTtlDays());

        List<String> authorityList = parseAuthorities(bundle.authorities());
        String newAccessToken = jwtTokenProvider.generateAccessToken(bundle.username(), authorityList, bundle.attr());

        return redisTemplate.opsForValue().set(newSessionKey, json, ttl)
            .thenReturn(ResponseEntity.ok(ApiResponse.of(
                new RefreshResponse(newAccessToken, newRefreshRaw),
                REFRESH_OK_MSG
            )));
    }

    private List<String> parseAuthorities(String authCsv) {
        if (authCsv == null || authCsv.isBlank()) return List.of();
        return Arrays.stream(authCsv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private ResponseEntity<ApiResponse<RefreshResponse>> invalidTokenResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.of(null, "유효하지 않은 refresh token"));
    }

    private ResponseEntity<ApiResponse<RefreshResponse>> storageUnavailableResponse() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.of(null, STORAGE_ERROR_MSG));
    }

    private ResponseEntity<ApiResponse<Void>> logoutStorageUnavailableResponse() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.of(null, STORAGE_ERROR_MSG));
    }
}
