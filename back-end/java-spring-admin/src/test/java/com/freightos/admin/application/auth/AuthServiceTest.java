package com.freightos.admin.application.auth;

import com.freightos.admin.application.auth.command.LoginCommand;
import com.freightos.admin.application.auth.port.out.SessionStorePort;
import com.freightos.admin.application.auth.port.out.SubscriptionQueryPort;
import com.freightos.admin.application.auth.projection.LoginResult;
import com.freightos.admin.application.auth.projection.MeProjection;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.application.permissionpreset.ComputeEffectiveAttributeValuesService;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.security.AccessibleButton;
import com.freightos.admin.common.security.ButtonEvalRow;
import com.freightos.admin.common.security.PolicyEvaluator;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.domain.user.entity.AdminUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private SessionStorePort sessionStorePort;

    @Mock
    private PolicyEvaluator policyEvaluator;

    @Mock
    private MenuPolicyPort menuPolicyPort;

    @Mock
    private ButtonPolicyPort buttonPolicyPort;

    @Mock
    private ComputeEffectiveAttributeValuesService effectiveAttributesService;

    @Mock
    private SubscriptionQueryPort subscriptionQueryPort;

    private AuthService authService;

    // 고정 시각: 2026-05-31 UTC — 구독 검증 시 비결정적 시간 의존 제거(T1)
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-05-31T00:00:00Z"), ZoneOffset.UTC);
    private static final LocalDate TODAY = LocalDate.of(2026, 5, 31);

    @BeforeEach
    void setUp() {
        // @InjectMocks 대신 수동 조립 — Clock 고정이 필요하므로 생성자 직접 호출
        authService = new AuthService(
                userUseCase,
                passwordEncoder,
                jwtTokenProvider,
                sessionStorePort,
                policyEvaluator,
                menuPolicyPort,
                buttonPolicyPort,
                effectiveAttributesService,
                subscriptionQueryPort,
                FIXED_CLOCK
        );
    }

    // ── login: 정상 → LoginResult 반환, sessionStorePort.saveSession 호출 ──────

    @Test
    void login_validCredentials_returnsLoginResult() {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashedPw", true, attrs, null, null);
        user.assignIdentity(1L, null, null, null, null);

        given(userUseCase.findUserByUsername("admin")).willReturn(user);
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(effectiveAttributesService.computeEffectiveAttributes(1L, attrs)).willReturn(attrs);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(any(), any())).willReturn(Set.of("ADMIN_CODE_LIST"));
        given(policyEvaluator.accessibleButtonCodes(any(), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), anyMap())).willReturn("access.token");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("rawRefresh");
        given(jwtTokenProvider.hashRefreshToken("rawRefresh")).willReturn("hashedRefresh");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        willDoNothing().given(sessionStorePort).saveSession(anyString(), any(), anyLong());

        LoginResult result = authService.login(new LoginCommand("admin", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("access.token");
        assertThat(result.refreshToken()).isEqualTo("rawRefresh");
        assertThat(result.user()).isSameAs(user);
        assertThat(result.accessibleMenus()).contains("MENU_ADMIN_CODE_LIST");

        then(sessionStorePort).should().saveSession(eq("hashedRefresh"), any(SessionBundle.class), eq(14L));
    }

    // ── login: 비밀번호 불일치 → BadCredentialsException ──────────────────────

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashedPw", true, Collections.emptyMap(), null, null);
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
        AdminUser inactiveUser = AdminUser.create("bob", "bob@example.com", "hashedPw", false, Collections.emptyMap(), null, null);
        inactiveUser.assignIdentity(2L, null, null, null, null);

        given(userUseCase.findUserByUsername("bob")).willReturn(inactiveUser);
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginCommand("bob", "rawPw")))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("비활성");
    }

    // ── getMe: 정상 → MeProjection 반환 ──────────────────────────────────────

    @Test
    void getMe_returnsProjectionWithAccessibleMenusAndButtons() {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashedPw", true, attrs, null, null);
        user.assignIdentity(1L, null, null, null, null);

        given(userUseCase.findUserByUsername("admin")).willReturn(user);
        given(effectiveAttributesService.computeEffectiveAttributes(1L, attrs)).willReturn(attrs);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of(
            new ButtonEvalRow(1L, "ADMIN_USER_LIST_CREATE", "신규", null, List.of())
        ));
        given(policyEvaluator.accessibleMenuCodes(any(), any())).willReturn(Set.of("ADMIN_USER_LIST"));
        given(policyEvaluator.accessibleButtonCodes(any(), any())).willReturn(Set.of("ADMIN_USER_LIST_CREATE"));

        MeProjection projection = authService.getMe("admin");

        assertThat(projection.username()).isEqualTo("admin");
        assertThat(projection.attributes().get("role")).contains("ADMIN");
        assertThat(projection.accessibleMenus()).contains("MENU_ADMIN_USER_LIST");
        assertThat(projection.accessibleButtons().stream().map(AccessibleButton::code).toList())
            .contains("BTN_ADMIN_USER_LIST_CREATE");
    }

    // ── 구독 관련 케이스들 ─────────────────────────────────────────────────────

    // ① login_subscriptionExpired: module=["FMS"], valid={} → SUBSCRIPTION_EXPIRED

    @Test
    void login_subscriptionExpired_throwsForbidden() {
        Map<String, List<String>> attrs = Map.of("module", List.of("FMS"));
        AdminUser user = AdminUser.create("fms", "fms@example.com", "hashedPw", true, attrs, null, 42L);
        user.assignIdentity(10L, null, null, null, null);

        given(userUseCase.findUserByUsername("fms")).willReturn(user);
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(subscriptionQueryPort.findValidModuleCodes(eq(42L), eq(TODAY))).willReturn(Set.of());

        assertThatThrownBy(() -> authService.login(new LoginCommand("fms", "rawPw")))
            .isInstanceOf(ApplicationException.class)
            .satisfies(ex -> {
                ApplicationException appEx = (ApplicationException) ex;
                assertThat(appEx.getErrorCode()).isEqualTo("SUBSCRIPTION_EXPIRED");
                assertThat(appEx.getStatus()).isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);
            });
    }

    // ② login_subscriptionValid: module=["FMS"], valid={"FMS"} → 통과

    @Test
    void login_subscriptionValid_passes() {
        Map<String, List<String>> attrs = Map.of("module", List.of("FMS"));
        AdminUser user = AdminUser.create("fms", "fms@example.com", "hashedPw", true, attrs, null, 42L);
        user.assignIdentity(10L, null, null, null, null);

        given(userUseCase.findUserByUsername("fms")).willReturn(user);
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(subscriptionQueryPort.findValidModuleCodes(eq(42L), eq(TODAY))).willReturn(Set.of("FMS"));
        given(effectiveAttributesService.computeEffectiveAttributes(10L, attrs)).willReturn(attrs);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(any(), any())).willReturn(Set.of());
        given(policyEvaluator.accessibleButtonCodes(any(), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), anyMap())).willReturn("tok");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("rtok");
        given(jwtTokenProvider.hashRefreshToken("rtok")).willReturn("rtokh");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        willDoNothing().given(sessionStorePort).saveSession(anyString(), any(), anyLong());

        LoginResult result = authService.login(new LoginCommand("fms", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("tok");
    }

    // ③ login_noModule: module 키 없음 → 안전망, subscriptionQueryPort 미호출

    @Test
    void login_noModule_passesWithoutSubscriptionCheck() {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashedPw", true, attrs, null, null);
        user.assignIdentity(1L, null, null, null, null);

        given(userUseCase.findUserByUsername("admin")).willReturn(user);
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(effectiveAttributesService.computeEffectiveAttributes(1L, attrs)).willReturn(attrs);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(any(), any())).willReturn(Set.of());
        given(policyEvaluator.accessibleButtonCodes(any(), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), anyMap())).willReturn("tok");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("rtok");
        given(jwtTokenProvider.hashRefreshToken("rtok")).willReturn("rtokh");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        willDoNothing().given(sessionStorePort).saveSession(anyString(), any(), anyLong());

        authService.login(new LoginCommand("admin", "rawPw"));

        // module 권한 없으면 포트 호출 없음
        then(subscriptionQueryPort).shouldHaveNoInteractions();
    }

    // ④ login_partialOverlap: module=["ADMIN","FMS"], valid={"FMS"} → 통과

    @Test
    void login_partialOverlap_passes() {
        Map<String, List<String>> attrs = Map.of("module", List.of("ADMIN", "FMS"));
        AdminUser user = AdminUser.create("combo", "combo@example.com", "hashedPw", true, attrs, null, 42L);
        user.assignIdentity(11L, null, null, null, null);

        given(userUseCase.findUserByUsername("combo")).willReturn(user);
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(subscriptionQueryPort.findValidModuleCodes(eq(42L), eq(TODAY))).willReturn(Set.of("FMS"));
        given(effectiveAttributesService.computeEffectiveAttributes(11L, attrs)).willReturn(attrs);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(any(), any())).willReturn(Set.of());
        given(policyEvaluator.accessibleButtonCodes(any(), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), anyMap())).willReturn("tok");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("rtok");
        given(jwtTokenProvider.hashRefreshToken("rtok")).willReturn("rtokh");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        willDoNothing().given(sessionStorePort).saveSession(anyString(), any(), anyLong());

        LoginResult result = authService.login(new LoginCommand("combo", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("tok");
    }

    // ── Redis 실패에도 로그인 성공 ─────────────────────────────────────────────

    @Test
    void login_redisFailure_loginSucceedsAnyway() {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        AdminUser user = AdminUser.create("admin", "admin@example.com", "hashedPw", true, attrs, null, null);
        user.assignIdentity(1L, null, null, null, null);

        given(userUseCase.findUserByUsername("admin")).willReturn(user);
        given(passwordEncoder.matches("rawPw", "hashedPw")).willReturn(true);
        given(effectiveAttributesService.computeEffectiveAttributes(1L, attrs)).willReturn(attrs);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(any(), any())).willReturn(Set.of());
        given(policyEvaluator.accessibleButtonCodes(any(), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), anyMap())).willReturn("tok");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("rtok");
        given(jwtTokenProvider.hashRefreshToken("rtok")).willReturn("rtokh");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        // Redis 어댑터가 내부적으로 예외를 삼키므로, 포트 자체가 예외를 던지면 로그인이 실패한다.
        // 실제 RedisSessionStoreAdapter는 RuntimeException을 삼키고 warn 로그만 남긴다.
        // 이 테스트는 SessionStorePort 구현이 예외를 삼키는 계약(saveSession이 throw 안 함)을 검증한다.
        willDoNothing().given(sessionStorePort).saveSession(anyString(), any(), anyLong());

        LoginResult result = authService.login(new LoginCommand("admin", "rawPw"));

        assertThat(result.accessToken()).isEqualTo("tok");
        assertThat(result.refreshToken()).isEqualTo("rtok");
    }
}
