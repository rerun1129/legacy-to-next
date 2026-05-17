package com.freightos.admin.common.security;

import com.freightos.admin.adapter.out.persistence.user.UserJpaEntity;
import com.freightos.admin.adapter.out.persistence.user.UserPermissionJpaEntity;
import com.freightos.admin.adapter.out.persistence.user.UserPermissionRepository;
import com.freightos.admin.adapter.out.persistence.user.UserRepository;
import com.freightos.admin.domain.user.entity.Permission;
import com.freightos.admin.domain.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserPermissionRepository userPermissionRepository;

    @InjectMocks
    private JpaUserDetailsService jpaUserDetailsService;

    // ── 정상 사용자 → UserDetails 반환, ROLE_ADMIN 포함 ────────────────────

    @Test
    void loadUserByUsername_activeAdminUser_returnsUserDetailsWithRoleAdmin() {
        UserJpaEntity entity = buildEntity(1L, "admin", "$2a$10$hashed", UserRole.ADMIN, true);
        given(userRepository.findByUsernameAndDeletedAtIsNull("admin")).willReturn(Optional.of(entity));
        given(userPermissionRepository.findAllByUserId(1L)).willReturn(List.of());

        UserDetails details = jpaUserDetailsService.loadUserByUsername("admin");

        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getPassword()).isEqualTo("$2a$10$hashed");
        String authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        assertThat(authorities).contains("ROLE_ADMIN");
    }

    // ── 정상 USER 사용자 → ROLE_USER 포함 ────────────────────────────────────

    @Test
    void loadUserByUsername_activeUserRole_returnsUserDetailsWithRoleUser() {
        UserJpaEntity entity = buildEntity(2L, "alice", "$2a$10$hashed", UserRole.USER, true);
        given(userRepository.findByUsernameAndDeletedAtIsNull("alice")).willReturn(Optional.of(entity));
        given(userPermissionRepository.findAllByUserId(2L)).willReturn(List.of());

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
        UserJpaEntity entity = buildEntity(3L, "inactive", "$2a$10$hashed", UserRole.USER, false);
        given(userRepository.findByUsernameAndDeletedAtIsNull("inactive")).willReturn(Optional.of(entity));

        assertThatThrownBy(() -> jpaUserDetailsService.loadUserByUsername("inactive"))
                .isInstanceOf(DisabledException.class);
    }

    // ── 활성 사용자 + permission 2개 → authorities에 ROLE_USER + 2개 permission 포함 ──

    @Test
    void loadUserByUsername_activeUserWithPermissions_authoritiesContainPermissions() {
        UserJpaEntity entity = buildEntity(4L, "tester", "$2a$10$hashed", UserRole.USER, true);
        given(userRepository.findByUsernameAndDeletedAtIsNull("tester")).willReturn(Optional.of(entity));
        given(userPermissionRepository.findAllByUserId(4L)).willReturn(List.of(
                new UserPermissionJpaEntity(4L, Permission.CODE_MANAGE),
                new UserPermissionJpaEntity(4L, Permission.USER_MANAGE)
        ));

        UserDetails details = jpaUserDetailsService.loadUserByUsername("tester");

        List<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        assertThat(authorities).contains("ROLE_USER", "CODE_MANAGE", "USER_MANAGE");
        assertThat(authorities).hasSize(3);
    }

    private UserJpaEntity buildEntity(Long id, String username, String passwordHash, UserRole role, boolean active) {
        UserJpaEntity entity = new UserJpaEntity();
        ReflectionTestUtils.setField(entity, "id", id);
        entity.setUsername(username);
        entity.setPasswordHash(passwordHash);
        entity.setRole(role);
        entity.setActive(active);
        return entity;
    }
}
