package com.freightos.admin.application.auth;

import com.freightos.admin.application.auth.command.LoginCommand;
import com.freightos.admin.application.auth.port.out.RefreshTokenPort;
import com.freightos.admin.application.auth.projection.LoginResult;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.application.permissionpreset.ComputeEffectiveAttributeValuesService;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.common.security.PolicyEvaluator;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.domain.user.entity.AdminUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

/**
 * AuthService 의 effective attribute 통합 시나리오 테스트.
 * 직접 부여만 / preset 만 / 혼합 / 없음 네 케이스를 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceEffectiveAttributeTest {

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

    @Mock
    private ComputeEffectiveAttributeValuesService effectiveAttributesService;

    @InjectMocks
    private AuthService authService;

    // ── 직접 부여만 있는 경우: effective = directAttrs ────────────────────────

    @Test
    void login_directAttributesOnly_usesDirectAttrsAsEffective() {
        Map<String, List<String>> directAttrs = Map.of("role", List.of("ADMIN"));
        AdminUser user = AdminUser.create("admin", "a@b.com", "hash", true, directAttrs);
        user.assignIdentity(1L, null, null, null, null);

        given(userUseCase.findUserByUsername("admin")).willReturn(user);
        given(passwordEncoder.matches("pw", "hash")).willReturn(true);
        // effectiveAttributesService 가 직접 속성 그대로 반환
        given(effectiveAttributesService.computeEffectiveAttributes(eq(1L), eq(directAttrs))).willReturn(directAttrs);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(eq(directAttrs), any())).willReturn(Set.of("ADMIN_CODE_LIST"));
        given(policyEvaluator.accessibleButtonCodes(eq(directAttrs), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), eq(directAttrs))).willReturn("at");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("rt");
        given(jwtTokenProvider.hashRefreshToken("rt")).willReturn("rth");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        willDoNothing().given(refreshTokenPort).save(any());

        LoginResult result = authService.login(new LoginCommand("admin", "pw"));

        assertThat(result.attributes()).isEqualTo(directAttrs);
        then(effectiveAttributesService).should().computeEffectiveAttributes(1L, directAttrs);
    }

    // ── preset 부여만 있는 경우: effective = preset 의 attributes ─────────────

    @Test
    void login_presetAttributesOnly_usesPresetAttrsAsEffective() {
        Map<String, List<String>> directAttrs = Collections.emptyMap();
        Map<String, List<String>> effectiveAttrs = Map.of("module", List.of("FMS"));

        AdminUser user = AdminUser.create("user2", "u@b.com", "hash", true, directAttrs);
        user.assignIdentity(2L, null, null, null, null);

        given(userUseCase.findUserByUsername("user2")).willReturn(user);
        given(passwordEncoder.matches("pw", "hash")).willReturn(true);
        given(effectiveAttributesService.computeEffectiveAttributes(eq(2L), eq(directAttrs))).willReturn(effectiveAttrs);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(eq(effectiveAttrs), any())).willReturn(Set.of("FMS_SEA_LIST"));
        given(policyEvaluator.accessibleButtonCodes(eq(effectiveAttrs), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), eq(effectiveAttrs))).willReturn("at2");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("rt2");
        given(jwtTokenProvider.hashRefreshToken("rt2")).willReturn("rth2");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        willDoNothing().given(refreshTokenPort).save(any());

        LoginResult result = authService.login(new LoginCommand("user2", "pw"));

        assertThat(result.attributes()).isEqualTo(effectiveAttrs);
        assertThat(result.accessibleMenus()).contains("MENU_FMS_SEA_LIST");
    }

    // ── 직접 + preset 혼합: effective = union ─────────────────────────────────

    @Test
    void login_directAndPresetMixed_usesUnionAsEffective() {
        Map<String, List<String>> directAttrs = Map.of("role", List.of("ADMIN"));
        Map<String, List<String>> effectiveAttrs = Map.of("role", List.of("ADMIN"), "module", List.of("FMS"));

        AdminUser user = AdminUser.create("combo", "c@b.com", "hash", true, directAttrs);
        user.assignIdentity(3L, null, null, null, null);

        given(userUseCase.findUserByUsername("combo")).willReturn(user);
        given(passwordEncoder.matches("pw", "hash")).willReturn(true);
        given(effectiveAttributesService.computeEffectiveAttributes(eq(3L), eq(directAttrs))).willReturn(effectiveAttrs);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(eq(effectiveAttrs), any())).willReturn(Set.of("ADMIN_CODE_LIST", "FMS_SEA_LIST"));
        given(policyEvaluator.accessibleButtonCodes(eq(effectiveAttrs), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), eq(effectiveAttrs))).willReturn("at3");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("rt3");
        given(jwtTokenProvider.hashRefreshToken("rt3")).willReturn("rth3");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        willDoNothing().given(refreshTokenPort).save(any());

        LoginResult result = authService.login(new LoginCommand("combo", "pw"));

        assertThat(result.attributes()).containsKey("role");
        assertThat(result.attributes()).containsKey("module");
        assertThat(result.accessibleMenus()).contains("MENU_ADMIN_CODE_LIST", "MENU_FMS_SEA_LIST");
    }

    // ── 직접/preset 모두 없음: effective = empty → 동작 동일 ─────────────────

    @Test
    void login_noDirectNorPreset_emptyEffectiveCausesEmptyAuthoritySet() {
        Map<String, List<String>> directAttrs = Collections.emptyMap();
        Map<String, List<String>> effectiveAttrs = Collections.emptyMap();

        AdminUser user = AdminUser.create("nobody", "n@b.com", "hash", true, directAttrs);
        user.assignIdentity(4L, null, null, null, null);

        given(userUseCase.findUserByUsername("nobody")).willReturn(user);
        given(passwordEncoder.matches("pw", "hash")).willReturn(true);
        given(effectiveAttributesService.computeEffectiveAttributes(eq(4L), eq(directAttrs))).willReturn(effectiveAttrs);
        given(menuPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(buttonPolicyPort.findAllActiveForEvaluation()).willReturn(List.of());
        given(policyEvaluator.accessibleMenuCodes(eq(effectiveAttrs), any())).willReturn(Set.of());
        given(policyEvaluator.accessibleButtonCodes(eq(effectiveAttrs), any())).willReturn(Set.of());
        given(jwtTokenProvider.generateAccessToken(anyString(), anyCollection(), eq(effectiveAttrs))).willReturn("at4");
        given(jwtTokenProvider.generateRefreshTokenRaw()).willReturn("rt4");
        given(jwtTokenProvider.hashRefreshToken("rt4")).willReturn("rth4");
        given(jwtTokenProvider.refreshTtlDays()).willReturn(14L);
        willDoNothing().given(refreshTokenPort).save(any());

        LoginResult result = authService.login(new LoginCommand("nobody", "pw"));

        assertThat(result.accessibleMenus()).isEmpty();
        assertThat(result.accessibleButtons()).isEmpty();
    }
}
