package com.freightos.admin.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.out.persistence.user.UserJpaEntity;
import com.freightos.admin.adapter.out.persistence.user.UserPermissionRepository;
import com.freightos.admin.adapter.out.persistence.user.UserRepository;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.domain.user.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

/**
 * Phase 3 ABAC 전환 후 JpaUserDetailsService 단위 테스트.
 * ROLE_* 권한은 유지되고, MENU_*/BTN_* 권한이 ABAC 평가 결과로 부여된다.
 * 실제 ObjectMapper를 사용하여 JSON 파싱 경로를 실제와 동일하게 유지.
 */
@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPermissionRepository userPermissionRepository;

    @Mock
    private PolicyEvaluator policyEvaluator;

    @Mock
    private MenuPolicyPort menuPolicyPort;

    @Mock
    private ButtonPolicyPort buttonPolicyPort;

    private JpaUserDetailsService jpaUserDetailsService;

    @BeforeEach
    void setUp() {
        // 실제 ObjectMapper 사용 — JSON 파싱 경로를 실제와 동일하게 유지
        jpaUserDetailsService = new JpaUserDetailsService(
                userRepository, userPermissionRepository,
                new ObjectMapper(), policyEvaluator, menuPolicyPort, buttonPolicyPort);
    }

    // ── 정상 사용자 → UserDetails 반환, ROLE_ADMIN + MENU_* 포함 ────────────────

    @Test
    void loadUserByUsername_activeAdminUser_returnsUserDetailsWithRoleAdminAndMenuAuthority() {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        UserJpaEntity entity = buildEntity(1L, "admin", "$2a$10$hashed", UserRole.ADMIN, true,
                "{\"role\":[\"ADMIN\"]}");
        given(userRepository.findByUsernameAndDeletedAtIsNull("admin")).willReturn(Optional.of(entity));
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(eq(attrs), any())).willReturn(Set.of("ADMIN_CODE_LIST"));
        given(policyEvaluator.accessibleButtonCodes(eq(attrs), any())).willReturn(Set.of());

        UserDetails details = jpaUserDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashed");
        List<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        assertThat(authorities).contains("ROLE_ADMIN");
        assertThat(authorities).contains("MENU_ADMIN_CODE_LIST");
    }

    // ── 정상 USER 사용자 → ROLE_USER 포함 ────────────────────────────────────

    @Test
    void loadUserByUsername_activeUserRole_returnsUserDetailsWithRoleUser() {
        UserJpaEntity entity = buildEntity(2L, "alice", "$2a$10$hashed", UserRole.USER, true, "{}");
        given(userRepository.findByUsernameAndDeletedAtIsNull("alice")).willReturn(Optional.of(entity));
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(eq(Map.of()), any())).willReturn(Set.of());
        given(policyEvaluator.accessibleButtonCodes(eq(Map.of()), any())).willReturn(Set.of());

        UserDetails details = jpaUserDetailsService.loadUserByUsername("alice");

        String authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        assertThat(authorities).contains("ROLE_USER");
    }

    // ── 미존재 → UsernameNotFoundException ────────────────────────────────────

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        given(userRepository.findByUsernameAndDeletedAtIsNull("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> jpaUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    // ── 비활성 사용자 → DisabledException ────────────────────────────────────

    @Test
    void loadUserByUsername_inactiveUser_throwsDisabledException() {
        UserJpaEntity entity = buildEntity(3L, "inactive", "$2a$10$hashed", UserRole.USER, false, "{}");
        given(userRepository.findByUsernameAndDeletedAtIsNull("inactive")).willReturn(Optional.of(entity));

        assertThatThrownBy(() -> jpaUserDetailsService.loadUserByUsername("inactive"))
                .isInstanceOf(DisabledException.class);
    }

    // ── ABAC 평가 결과 menu/button 권한 부여 확인 ─────────────────────────────

    @Test
    void loadUserByUsername_abacEval_authoritiesContainMenuAndButtonCodes() {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        UserJpaEntity entity = buildEntity(4L, "tester", "$2a$10$hashed", UserRole.USER, true,
                "{\"role\":[\"ADMIN\"]}");
        given(userRepository.findByUsernameAndDeletedAtIsNull("tester")).willReturn(Optional.of(entity));
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(eq(attrs), any())).willReturn(Set.of("ADMIN_CODE_LIST", "ADMIN_USER_LIST"));
        given(policyEvaluator.accessibleButtonCodes(eq(attrs), any())).willReturn(Set.of("ADMIN_CODE_LIST_CREATE"));

        UserDetails details = jpaUserDetailsService.loadUserByUsername("tester");

        List<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        assertThat(authorities).contains("ROLE_USER", "MENU_ADMIN_CODE_LIST", "MENU_ADMIN_USER_LIST", "BTN_ADMIN_CODE_LIST_CREATE");
    }

    private UserJpaEntity buildEntity(Long id, String username, String passwordHash, UserRole role,
                                      boolean active, String attributes) {
        UserJpaEntity entity = new UserJpaEntity();
        ReflectionTestUtils.setField(entity, "id", id);
        entity.setUsername(username);
        entity.setPasswordHash(passwordHash);
        entity.setRole(role);
        entity.setActive(active);
        entity.setAttributes(attributes);
        return entity;
    }
}
