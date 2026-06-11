package com.freightos.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * JWT 중앙 검증 GlobalFilter.
 *
 * 동작 원칙: 게이트웨이는 차단하지 않고 신원 정보를 주입(enrich-only)한다.
 * - 클라이언트가 보낸 X-Auth-*/X-Internal-Token은 위조 차단을 위해 무조건 제거한다.
 * - Bearer 토큰이 있고 검증 성공 시 X-Auth-User/X-Auth-Authorities/X-Auth-Attr 주입.
 * - 검증 실패 또는 토큰 부재 시 X-Auth-* 주입 없이 통과(보호 판단은 모듈 SecurityConfig 몫).
 * - 항상 X-Internal-Token을 주입하여 다운스트림이 게이트웨이 경유 여부를 확인할 수 있게 한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    /**
     * 라우팅 필터(RouteToRequestUrlFilter = 10000)보다 앞에서 실행되어야
     * 다운스트림으로 나가기 전에 헤더를 확정할 수 있다.
     */
    static final int FILTER_ORDER = -100;

    private static final String HEADER_AUTH_USER = "X-Auth-User";
    private static final String HEADER_AUTH_AUTHORITIES = "X-Auth-Authorities";
    private static final String HEADER_AUTH_ATTR = "X-Auth-Attr";
    private static final String HEADER_INTERNAL_TOKEN = "X-Internal-Token";
    private static final String BEARER_PREFIX = "Bearer ";

    private final GatewayJwtTokenProvider jwtTokenProvider;
    private final GatewayInternalProperties internalProperties;

    @Override
    public int getOrder() {
        return FILTER_ORDER;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest mutatedRequest = buildMutatedRequest(exchange);
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private ServerHttpRequest buildMutatedRequest(ServerWebExchange exchange) {
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate();

        // 클라이언트가 보낸 신원/내부 헤더를 무조건 제거 — 위조 차단
        builder.headers(headers -> {
            headers.remove(HEADER_AUTH_USER);
            headers.remove(HEADER_AUTH_AUTHORITIES);
            headers.remove(HEADER_AUTH_ATTR);
            headers.remove(HEADER_INTERNAL_TOKEN);
        });

        // 유효 토큰이 있으면 신원 헤더 주입
        extractBearerToken(exchange.getRequest().getHeaders())
                .flatMap(jwtTokenProvider::validate)
                .ifPresent(jws -> injectAuthHeaders(builder, jws));

        // 게이트웨이 경유 확인용 내부 토큰은 항상 주입
        builder.header(HEADER_INTERNAL_TOKEN, internalProperties.getInternalToken());

        return builder.build();
    }

    private Optional<String> extractBearerToken(HttpHeaders headers) {
        String authorization = headers.getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        return Optional.of(authorization.substring(BEARER_PREFIX.length()));
    }

    private void injectAuthHeaders(ServerHttpRequest.Builder builder, Jws<Claims> jws) {
        String username = jwtTokenProvider.extractUsername(jws);
        builder.header(HEADER_AUTH_USER, username);
        log.debug("JWT 검증 성공: user={}", username);

        jwtTokenProvider.extractAuthorities(jws).ifPresent(auth -> builder.header(HEADER_AUTH_AUTHORITIES, auth));

        // attr 클레임이 있으면 UTF-8 bytes → Base64 인코딩(헤더 ASCII 안전 보장)
        jwtTokenProvider.extractAttr(jws).ifPresent(attr -> {
            String encoded = Base64.getEncoder().encodeToString(attr.getBytes(StandardCharsets.UTF_8));
            builder.header(HEADER_AUTH_ATTR, encoded);
        });
    }
}
