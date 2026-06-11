package com.freightos.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 게이트웨이가 JWT를 중앙 검증한 뒤 주입하는 신뢰 헤더를 읽어 SecurityContext를 구성한다.
 * 클라이언트발 동명 헤더는 게이트웨이가 스트립하므로 X-Internal-Token 일치 여부로 게이트웨이 경유를 판별한다.
 */
@Component
@RequiredArgsConstructor
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    static final String HEADER_INTERNAL_TOKEN = "X-Internal-Token";
    static final String HEADER_AUTH_USER      = "X-Auth-User";
    static final String HEADER_AUTH_AUTHORITIES = "X-Auth-Authorities";
    static final String HEADER_AUTH_ATTR      = "X-Auth-Attr";
    static final String ATTR_AUTH_ATTR_KEY    = "gateway.auth.attr";

    private final GatewayProperties gatewayProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String internalToken = request.getHeader(HEADER_INTERNAL_TOKEN);
        String username      = request.getHeader(HEADER_AUTH_USER);

        if (StringUtils.hasText(internalToken)
                && internalToken.equals(gatewayProperties.getInternalToken())
                && StringUtils.hasText(username)) {

            List<SimpleGrantedAuthority> authorities = parseAuthorities(request.getHeader(HEADER_AUTH_AUTHORITIES));
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            String attrHeader = request.getHeader(HEADER_AUTH_ATTR);
            if (StringUtils.hasText(attrHeader)) {
                // X-Auth-Attr: UTF-8→Base64 인코딩된 JSON 문자열을 request attribute에 보관
                request.setAttribute(ATTR_AUTH_ATTR_KEY, attrHeader);
            }

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(request, response);
    }

    private List<SimpleGrantedAuthority> parseAuthorities(String csv) {
        if (!StringUtils.hasText(csv)) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
