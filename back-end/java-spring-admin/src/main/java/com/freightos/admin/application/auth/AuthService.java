package com.freightos.admin.application.auth;

import com.freightos.admin.application.auth.command.LoginCommand;
import com.freightos.admin.application.auth.command.LogoutCommand;
import com.freightos.admin.application.auth.command.RefreshCommand;
import com.freightos.admin.application.auth.port.in.AuthUseCase;
import com.freightos.admin.application.auth.port.out.RefreshTokenPort;
import com.freightos.admin.application.auth.projection.LoginResult;
import com.freightos.admin.application.auth.projection.MeProjection;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.security.ButtonEvalRow;
import com.freightos.admin.common.security.MenuEvalRow;
import com.freightos.admin.common.security.PolicyEvaluator;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.domain.auth.entity.RefreshToken;
import com.freightos.admin.domain.user.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

        Map<String, List<String>> attrs = user.getAttributes();
        Set<String> accessibleMenus = evaluateMenus(attrs);
        Set<String> accessibleButtons = evaluateButtons(attrs);
        Set<String> authorities = buildAuthorities(user, accessibleMenus, accessibleButtons);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), authorities, attrs);
        String refreshRaw = jwtTokenProvider.generateRefreshTokenRaw();
        String refreshHash = jwtTokenProvider.hashRefreshToken(refreshRaw);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtTokenProvider.refreshTtlDays());

        refreshTokenPort.save(RefreshToken.issue(user.getId(), refreshHash, expiresAt));

        return new LoginResult(accessToken, refreshRaw, user, attrs,
                new ArrayList<>(accessibleMenus), new ArrayList<>(accessibleButtons));
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

        Map<String, List<String>> attrs = user.getAttributes();
        Set<String> accessibleMenus = evaluateMenus(attrs);
        Set<String> accessibleButtons = evaluateButtons(attrs);
        Set<String> authorities = buildAuthorities(user, accessibleMenus, accessibleButtons);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), authorities, attrs);
        String newRefreshRaw = jwtTokenProvider.generateRefreshTokenRaw();
        String newRefreshHash = jwtTokenProvider.hashRefreshToken(newRefreshRaw);
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(jwtTokenProvider.refreshTtlDays());

        refreshTokenPort.save(RefreshToken.issue(user.getId(), newRefreshHash, newExpiresAt));

        return new LoginResult(accessToken, newRefreshRaw, user, attrs,
                new ArrayList<>(accessibleMenus), new ArrayList<>(accessibleButtons));
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
        Map<String, List<String>> attrs = user.getAttributes();
        Set<String> accessibleMenus = evaluateMenus(attrs);
        Set<String> accessibleButtons = evaluateButtons(attrs);
        return new MeProjection(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                attrs,
                new ArrayList<>(accessibleMenus),
                new ArrayList<>(accessibleButtons)
        );
    }

    private Set<String> evaluateMenus(Map<String, List<String>> attrs) {
        List<MenuEvalRow> menuRows = menuPolicyPort.findAllActiveForEvaluation();
        return policyEvaluator.accessibleMenuCodes(attrs, menuRows);
    }

    private Set<String> evaluateButtons(Map<String, List<String>> attrs) {
        List<ButtonEvalRow> buttonRows = buttonPolicyPort.findAllActiveForEvaluation();
        return policyEvaluator.accessibleButtonCodes(attrs, buttonRows);
    }

    private Set<String> buildAuthorities(AdminUser user, Set<String> accessibleMenus, Set<String> accessibleButtons) {
        Set<String> authorities = new HashSet<>();
        authorities.add("ROLE_" + user.getRole().name());
        accessibleMenus.forEach(code -> authorities.add("MENU_" + code));
        accessibleButtons.forEach(code -> authorities.add("BTN_" + code));
        return authorities;
    }
}
