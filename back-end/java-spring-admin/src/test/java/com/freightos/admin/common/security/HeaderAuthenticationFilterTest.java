package com.freightos.admin.common.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class HeaderAuthenticationFilterTest {

    private static final String VALID_TOKEN = "test-internal-token";

    private GatewayProperties gatewayProperties;
    private HeaderAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        gatewayProperties = new GatewayProperties();
        gatewayProperties.setInternalToken(VALID_TOKEN);
        filter = new HeaderAuthenticationFilter(gatewayProperties);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("내부 토큰 일치 + X-Auth-User 존재 → SecurityContext에 인증 세팅")
    void validTokenAndUser_setsAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderAuthenticationFilter.HEADER_INTERNAL_TOKEN, VALID_TOKEN);
        request.addHeader(HeaderAuthenticationFilter.HEADER_AUTH_USER, "alice");
        request.addHeader(HeaderAuthenticationFilter.HEADER_AUTH_AUTHORITIES, "ROLE_USER,ROLE_ADMIN");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("alice");
        assertThat(auth.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("내부 토큰 불일치 → 인증 미세팅, 요청은 통과")
    void invalidToken_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderAuthenticationFilter.HEADER_INTERNAL_TOKEN, "wrong-token");
        request.addHeader(HeaderAuthenticationFilter.HEADER_AUTH_USER, "alice");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("내부 토큰 없음 → 인증 미세팅, 요청은 통과")
    void missingToken_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderAuthenticationFilter.HEADER_AUTH_USER, "alice");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰 일치하지만 X-Auth-User 없음 → 인증 미세팅, 요청은 통과")
    void validTokenButMissingUser_doesNotSetAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderAuthenticationFilter.HEADER_INTERNAL_TOKEN, VALID_TOKEN);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("X-Auth-Attr 헤더 존재 시 request attribute에 보관")
    void withAttrHeader_storesAttrAsRequestAttribute() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderAuthenticationFilter.HEADER_INTERNAL_TOKEN, VALID_TOKEN);
        request.addHeader(HeaderAuthenticationFilter.HEADER_AUTH_USER, "alice");
        request.addHeader(HeaderAuthenticationFilter.HEADER_AUTH_ATTR, "eyJhY2NvdW50SWQiOiIxMjMifQ==");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(request.getAttribute(HeaderAuthenticationFilter.ATTR_AUTH_ATTR_KEY))
                .isEqualTo("eyJhY2NvdW50SWQiOiIxMjMifQ==");
    }

    @Test
    @DisplayName("X-Auth-Authorities 빈 CSV → 빈 권한 목록으로 인증 세팅")
    void emptyAuthorities_setsAuthenticationWithEmptyAuthorities() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HeaderAuthenticationFilter.HEADER_INTERNAL_TOKEN, VALID_TOKEN);
        request.addHeader(HeaderAuthenticationFilter.HEADER_AUTH_USER, "bob");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isEmpty();
    }
}
