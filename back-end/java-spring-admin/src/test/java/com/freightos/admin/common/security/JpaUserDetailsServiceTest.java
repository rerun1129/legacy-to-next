package com.freightos.admin.common.security;

import com.freightos.admin.adapter.out.persistence.user.UserJpaEntity;
import com.freightos.admin.adapter.out.persistence.user.UserRepository;
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

import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JpaUserDetailsService jpaUserDetailsService;

    // ── 정상 사용자 → UserDetails 반환, ROLE_ADMIN 포함 ────────────────────

    @Test
    void loadUserByUsername_activeAdminUser_returnsUserDetailsWithRoleAdmin() {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUsername("admin");
        entity.setPasswordHash("$2a$10$hashed");
        entity.setRole(UserRole.ADMIN);
        entity.setActive(true);
        given(userRepository.findByUsernameAndDeletedAtIsNull("admin")).willReturn(Optional.of(entity));

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
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUsername("alice");
        entity.setPasswordHash("$2a$10$hashed");
        entity.setRole(UserRole.USER);
        entity.setActive(true);
        given(userRepository.findByUsernameAndDeletedAtIsNull("alice")).willReturn(Optional.of(entity));

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
        UserJpaEntity entity = new UserJpaEntity();
        entity.setUsername("inactive");
        entity.setPasswordHash("$2a$10$hashed");
        entity.setRole(UserRole.USER);
        entity.setActive(false);
        given(userRepository.findByUsernameAndDeletedAtIsNull("inactive")).willReturn(Optional.of(entity));

        assertThatThrownBy(() -> jpaUserDetailsService.loadUserByUsername("inactive"))
                .isInstanceOf(DisabledException.class);
    }
}
