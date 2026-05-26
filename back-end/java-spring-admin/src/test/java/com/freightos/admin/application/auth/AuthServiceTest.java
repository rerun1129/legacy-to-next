package com.freightos.admin.application.auth;

import com.freightos.admin.application.auth.command.LoginCommand;
import com.freightos.admin.application.auth.command.LogoutCommand;
import com.freightos.admin.application.auth.command.RefreshCommand;
import com.freightos.admin.common.security.AccessibleButton;
import com.freightos.admin.application.auth.port.out.RefreshTokenPort;
import com.freightos.admin.application.auth.projection.LoginResult;
import com.freightos.admin.application.auth.projection.MeProjection;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.security.PolicyEvaluator;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.domain.auth.entity.RefreshToken;
import com.freightos.admin.domain.user.entity.AdminUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenPort refreshTokenPort;

    @Mock
    private PolicyEvaluator policyEvaluator;

    @Mock
    private MenuPolicyPort menuPolicyPort;

    @Mock
    private ButtonPolicyPort buttonPolicyPort;

    @InjectMocks
    private AuthService authService;

    // ── login: 정상 → LoginResult 반환, refresh save 호출 ──────────────────────

    @Test
    void login_validCredentials_returnsLoginResult() {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashedPw", true, attrs);
        user.assignIdentity(1L, null, null, null, null);

        given(userUseCase.findUserByUsername("admin")).willReturn(user);
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(any(), any())).willReturn(Set.of("ADMIN_CODE_LIST"));
        given(policyEvaluator.accessibleButtonCodes(any(), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), anyMap())).willReturn("access.token");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("rawRefresh");
        given(jwtTokenProvider.hashRefreshToken("rawRefresh")).willReturn("hashedRefresh");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        willDoNothing().given(refreshTokenPort).save(any());

        LoginResult result = authService.login(new LoginCommand("admin", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("access.token");
        assertThat(result.refreshToken()).isEqualTo("rawRefresh");
        assertThat(result.user()).isSameAs(user);
        assertThat(result.accessibleMenus()).contains("MENU_ADMIN_CODE_LIST");

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        then(refreshTokenPort).should().save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getTokenHash()).isEqualTo("hashedRefresh");
    }

    // ── login: 비밀번호 불일치 → BadCredentialsException ──────────────────────

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashedPw", true, Collections.emptyMap());
        user.assignIdentity(1L, null, null, null, null);

        given(userUseCase.findUserByUsername("admin")).willReturn(user);
        given(passwordEncoder.matches("wrongPw", "hashedPw")).willReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginCommand("admin", "wrongPw")))
            .isInstanceOf(BadCredentialsException.class);
    }

    // ── login: 사용자 미존재 → BadCredentialsException ─────────────────────────

    @Test
    void login_userNotFound_throwsBadCredentials() {
        given(userUseCase.findUserByUsername("unknown"))
            .willThrow(ApplicationException.notFound("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        assertThatThrownBy(() -> authService.login(new LoginCommand("unknown", "anyPw")))
            .isInstanceOf(BadCredentialsException.class);
    }

    // ── login: 비활성 사용자 → BadCredentialsException ────────────────────────

    @Test
    void login_inactiveUser_throwsBadCredentials() {
        AdminUser inactiveUser = AdminUser.create("bob", "bob@example.com", "hashedPw", false, Collections.emptyMap());
        inactiveUser.assignIdentity(2L, null, null, null, null);

        given(userUseCase.findUserByUsername("bob")).willReturn(inactiveUser);
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginCommand("bob", "rawPw")))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("비활성");
    }

    // ── refresh: 정상 → 이전 revoke + 새 발급 ────────────────────────────────

    @Test
    void refresh_validToken_revokesOldAndIssuesNew() {
        RefreshToken oldToken = RefreshToken.issue(1L, "oldHash", LocalDateTime.now().plusDays(7));
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashedPw", true, Collections.emptyMap());
        user.assignIdentity(1L, null, null, null, null);

        given(jwtTokenProvider.hashRefreshToken("rawOld")).willReturn("oldHash");
        given(refreshTokenPort.findActiveByTokenHash("oldHash")).willReturn(Optional.of(oldToken));
        willDoNothing().given(refreshTokenPort).revokeByTokenHash("oldHash");
        given(userUseCase.findUserById(1L)).willReturn(user);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(any(), any())).willReturn(Set.of());
        given(policyEvaluator.accessibleButtonCodes(any(), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), anyMap())).willReturn("new.access.token");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("newRawRefresh");
        given(jwtTokenProvider.hashRefreshToken("newRawRefresh")).willReturn("newHash");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        willDoNothing().given(refreshTokenPort).save(any());

        LoginResult result = authService.refresh(new RefreshCommand("rawOld"));

        then(refreshTokenPort).should().revokeByTokenHash("oldHash");
        assertThat(result.accessToken()).isEqualTo("new.access.token");
        assertThat(result.refreshToken()).isEqualTo("newRawRefresh");
    }

    // ── refresh: 미존재 hash → BadCredentialsException ────────────────────────

    @Test
    void refresh_invalidToken_throwsBadCredentials() {
        given(jwtTokenProvider.hashRefreshToken("unknownRaw")).willReturn("unknownHash");
        given(refreshTokenPort.findActiveByTokenHash("unknownHash")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshCommand("unknownRaw")))
            .isInstanceOf(BadCredentialsException.class);
    }

    // ── logout: revoke 호출 ────────────────────────────────────────────────────

    @Test
    void logout_callsRevokeByTokenHash() {
        given(jwtTokenProvider.hashRefreshToken("rawToken")).willReturn("hashed");
        willDoNothing().given(refreshTokenPort).revokeByTokenHash("hashed");

        authService.logout(new LogoutCommand("rawToken"));

        then(refreshTokenPort).should().revokeByTokenHash("hashed");
    }

    // ── getMe: 정상 → MeProjection 반환 ──────────────────────────────────────

    @Test
    void getMe_returnsProjectionWithAccessibleMenusAndButtons() {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashedPw", true, attrs);
        user.assignIdentity(1L, null, null, null, null);

        given(userUseCase.findUserByUsername("admin")).willReturn(user);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(any(), any())).willReturn(Set.of("ADMIN_USER_LIST"));
        given(policyEvaluator.accessibleButtonCodes(any(), any())).willReturn(Set.of("ADMIN_USER_LIST_CREATE"));

        MeProjection projection = authService.getMe("admin");

        assertThat(projection.username()).isEqualTo("admin");
        // role 필드는 MeProjection에서 제거됨 — attributes에서 확인
        assertThat(projection.attributes().get("role")).contains("ADMIN");
        assertThat(projection.accessibleMenus()).contains("MENU_ADMIN_USER_LIST");
        assertThat(projection.accessibleButtons().stream().map(AccessibleButton::code).toList())
            .contains("BTN_ADMIN_USER_LIST_CREATE");
    }
}
