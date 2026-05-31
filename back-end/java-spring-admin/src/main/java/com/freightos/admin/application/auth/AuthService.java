package com.freightos.admin.application.auth;

import com.freightos.admin.application.auth.command.LoginCommand;
import com.freightos.admin.application.auth.command.LogoutCommand;
import com.freightos.admin.application.auth.command.RefreshCommand;
import com.freightos.admin.application.auth.port.in.AuthUseCase;
import com.freightos.admin.application.auth.port.out.RefreshTokenPort;
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
import com.freightos.admin.common.security.MenuEvalRow;
import com.freightos.admin.common.security.PolicyEvaluator;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.domain.auth.entity.RefreshToken;
import com.freightos.admin.domain.user.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserUseCase userUseCase;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenPort refreshTokenPort;
    private final PolicyEvaluator policyEvaluator;
    private final MenuPolicyPort menuPolicyPort;
    private final ButtonPolicyPort buttonPolicyPort;
    private final ComputeEffectiveAttributeValuesService effectiveAttributesService;
    private final SubscriptionQueryPort subscriptionQueryPort;
    private final Clock clock;

    @Override
    @Transactional
    public LoginResult login(LoginCommand command) {
        AdminUser user;
        try {
            user = userUseCase.findUserByUsername(command.username());
        } catch (ApplicationException e) {
            throw new BadCredentialsException("자격 증명이 올바르지 않습니다.");
        }
        if (!passwordEncoder.matches(command.rawPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("자격 증명이 올바르지 않습니다.");
        }
        if (!user.isActive()) {
            throw new BadCredentialsException("비활성 사용자입니다.");
        }
        verifySubscriptionOrThrow(user);

        // direct ∪ active preset attribute_value union
        Map<String, List<String>> attrs = effectiveAttributesService.computeEffectiveAttributes(user.getId(), user.getAttributes());
        Set<String> accessibleMenus = evaluateMenus(attrs);
        List<AccessibleButton> accessibleButtons = evaluateButtons(attrs);
        Set<String> authorities = buildAuthorities(attrs, accessibleMenus, accessibleButtons);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), authorities, attrs);
        String refreshRaw = jwtTokenProvider.generateRefreshTokenRaw();
        String refreshHash = jwtTokenProvider.hashRefreshToken(refreshRaw);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtTokenProvider.refreshTtlDays());

        refreshTokenPort.save(RefreshToken.issue(user.getId(), refreshHash, expiresAt));

        // FE 컨벤션(MENU_*/BTN_*)에 맞춰 prefix 부착 후 반환
        return new LoginResult(accessToken, refreshRaw, user, attrs,
                accessibleMenus.stream().map(c -> "MENU_" + c).toList(),
                accessibleButtons.stream().map(ab -> new AccessibleButton("BTN_" + ab.code(), ab.label(), ab.labelEn())).toList());
    }

    @Override
    @Transactional
    public LoginResult refresh(RefreshCommand command) {
        String oldHash = jwtTokenProvider.hashRefreshToken(command.refreshToken());
        RefreshToken oldToken = refreshTokenPort.findActiveByTokenHash(oldHash)
            .orElseThrow(() -> new BadCredentialsException("유효하지 않은 refresh token"));

        refreshTokenPort.revokeByTokenHash(oldHash);

        AdminUser user = userUseCase.findUserById(oldToken.getUserId());
        if (!user.isActive()) {
            throw new BadCredentialsException("비활성 사용자입니다.");
        }
        verifySubscriptionOrThrow(user);

        // direct ∪ active preset attribute_value union
        Map<String, List<String>> attrs = effectiveAttributesService.computeEffectiveAttributes(user.getId(), user.getAttributes());
        Set<String> accessibleMenus = evaluateMenus(attrs);
        List<AccessibleButton> accessibleButtons = evaluateButtons(attrs);
        Set<String> authorities = buildAuthorities(attrs, accessibleMenus, accessibleButtons);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), authorities, attrs);
        String newRefreshRaw = jwtTokenProvider.generateRefreshTokenRaw();
        String newRefreshHash = jwtTokenProvider.hashRefreshToken(newRefreshRaw);
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(jwtTokenProvider.refreshTtlDays());

        refreshTokenPort.save(RefreshToken.issue(user.getId(), newRefreshHash, newExpiresAt));

        // FE 컨벤션(MENU_*/BTN_*)에 맞춰 prefix 부착 후 반환
        return new LoginResult(accessToken, newRefreshRaw, user, attrs,
                accessibleMenus.stream().map(c -> "MENU_" + c).toList(),
                accessibleButtons.stream().map(ab -> new AccessibleButton("BTN_" + ab.code(), ab.label(), ab.labelEn())).toList());
    }

    @Override
    @Transactional
    public void logout(LogoutCommand command) {
        String hash = jwtTokenProvider.hashRefreshToken(command.refreshToken());
        refreshTokenPort.revokeByTokenHash(hash);
    }

    @Override
    public MeProjection getMe(String username) {
        AdminUser user = userUseCase.findUserByUsername(username);
        verifySubscriptionOrThrow(user);
        // direct ∪ active preset attribute_value union
        Map<String, List<String>> attrs = effectiveAttributesService.computeEffectiveAttributes(user.getId(), user.getAttributes());
        Set<String> accessibleMenus = evaluateMenus(attrs);
        List<AccessibleButton> accessibleButtons = evaluateButtons(attrs);
        // FE 컨벤션(MENU_*/BTN_*)에 맞춰 prefix 부착 후 반환
        return new MeProjection(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                attrs,
                accessibleMenus.stream().map(c -> "MENU_" + c).toList(),
                accessibleButtons.stream().map(ab -> new AccessibleButton("BTN_" + ab.code(), ab.label(), ab.labelEn())).toList()
        );
    }

    /**
     * 유저의 module 권한과 소속 고객사 유효 구독 교집합이 없으면 FORBIDDEN 예외를 던진다.
     * module 권한 자체가 없으면 안전망으로 통과시킨다.
     */
    private void verifySubscriptionOrThrow(AdminUser user) {
        List<String> modules = user.getAttributes().getOrDefault("module", List.of());
        if (modules.isEmpty()) {
            return;
        }
        Set<String> valid = subscriptionQueryPort.findValidModuleCodes(user.getSubscriberId(), LocalDate.now(clock));
        if (modules.stream().noneMatch(valid::contains)) {
            throw new ApplicationException(HttpStatus.FORBIDDEN, "SUBSCRIPTION_EXPIRED", "구독 기간이 만료하였으므로 사용이 불가능합니다.");
        }
    }

    private Set<String> evaluateMenus(Map<String, List<String>> attrs) {
        List<MenuEvalRow> menuRows = menuPolicyPort.findAllActiveForEvaluation();
        return policyEvaluator.accessibleMenuCodes(attrs, menuRows);
    }

    private List<AccessibleButton> evaluateButtons(Map<String, List<String>> attrs) {
        List<ButtonEvalRow> buttonRows = buttonPolicyPort.findAllActiveForEvaluation();
        Set<String> accessibleCodes = policyEvaluator.accessibleButtonCodes(attrs, buttonRows);
        return buttonRows.stream()
                .filter(row -> accessibleCodes.contains(row.buttonCode()))
                .map(row -> new AccessibleButton(row.buttonCode(), row.label(), row.labelEn()))
                .toList();
    }

    private Set<String> buildAuthorities(Map<String, List<String>> attrs, Set<String> accessibleMenus, List<AccessibleButton> accessibleButtons) {
        Set<String> authorities = new HashSet<>();
        // attributes의 role 키 값들을 ROLE_*로 부여
        attrs.getOrDefault("role", List.of()).forEach(r -> authorities.add("ROLE_" + r));
        accessibleMenus.forEach(code -> authorities.add("MENU_" + code));
        accessibleButtons.forEach(ab -> authorities.add("BTN_" + ab.code()));
        return authorities;
    }
}
