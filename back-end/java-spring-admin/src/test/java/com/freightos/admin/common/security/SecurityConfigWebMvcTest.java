package com.freightos.admin.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 헤더 기반 인증 모델 검증.
 * 유효한 게이트웨이 헤더가 있으면 인증이 수립되고, 없으면 401이 반환된다.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:adminsec;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;INIT=CREATE SCHEMA IF NOT EXISTS admin",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.default_schema=admin",
        "spring.sql.init.mode=never",
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
        "jwt.secret=test-secret-key-must-be-at-least-32-chars!!",
        "gateway.internal-token=test-internal-token"
})
class SecurityConfigWebMvcTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void securityFilterChainBeanIsLoaded() {
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    void securedEndpoint_withoutGatewayHeaders_returns401() {
        // 헤더 없음 → 인증 미수립 → 401
        ResponseEntity<String> response = restTemplate.getForEntity("/api/admin/auth/me", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void securedEndpoint_withInvalidInternalToken_returns401() {
        // 내부 토큰 불일치 → 인증 미수립 → 401
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", "wrong-token");
        headers.set("X-Auth-User", "admin");
        headers.set("X-Auth-Authorities", "ROLE_ADMIN");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/admin/auth/me", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void securedEndpoint_withValidGatewayHeaders_isAuthenticated() {
        // 유효한 게이트웨이 헤더 → 인증 수립 → Security 필터에 의한 401이 아닌 응답
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Token", "test-internal-token");
        headers.set("X-Auth-User", "admin");
        headers.set("X-Auth-Authorities", "ROLE_ADMIN");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/admin/auth/me", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        // 인증 필터는 통과 → Spring Security 401 엔트리포인트가 아닌 실제 응답 코드
        assertThat(response.getStatusCode()).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void actuatorHealth_isPermitAll() {
        // /actuator/health는 인증 없이 접근 가능
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
