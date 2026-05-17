package com.freightos.admin.application.auth;

import com.freightos.admin.application.auth.command.LoginCommand;
import com.freightos.admin.application.auth.command.LogoutCommand;
import com.freightos.admin.application.auth.command.RefreshCommand;
import com.freightos.admin.application.auth.port.in.AuthUseCase;
import com.freightos.admin.application.auth.port.out.RefreshTokenPort;
import com.freightos.admin.application.auth.projection.LoginResult;
import com.freightos.admin.application.user.port.in.UserUseCase;
import com.freightos.admin.application.user.port.out.UserPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.security.JwtTokenProvider;
import com.freightos.admin.domain.auth.entity.RefreshToken;
import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.user.entity.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

    private final UserUseCase userUseCase;
    private final UserPort userPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenPort refreshTokenPort;

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

        Set<Permission> permissions = userPort.findPermissionsByUserId(user.getId());
        Set<String> authorities = buildAuthorities(user, permissions);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), authorities);
        String refreshRaw = jwtTokenProvider.generateRefreshTokenRaw();
        String refreshHash = jwtTokenProvider.hashRefreshToken(refreshRaw);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtTokenProvider.refreshTtlDays());

        refreshTokenPort.save(RefreshToken.issue(user.getId(), refreshHash, expiresAt));

        return new LoginResult(accessToken, refreshRaw, user, permissions);
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

        Set<Permission> permissions = userPort.findPermissionsByUserId(user.getId());
        Set<String> authorities = buildAuthorities(user, permissions);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), authorities);
        String newRefreshRaw = jwtTokenProvider.generateRefreshTokenRaw();
        String newRefreshHash = jwtTokenProvider.hashRefreshToken(newRefreshRaw);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtTokenProvider.refreshTtlDays());

        refreshTokenPort.save(RefreshToken.issue(user.getId(), newRefreshHash, expiresAt));

        return new LoginResult(accessToken, newRefreshRaw, user, permissions);
    }

    @Override
    @Transactional
    public void logout(LogoutCommand command) {
        String hash = jwtTokenProvider.hashRefreshToken(command.refreshToken());
        refreshTokenPort.revokeByTokenHash(hash);
    }

    private Set<String> buildAuthorities(AdminUser user, Set<Permission> permissions) {
        Set<String> authorities = new HashSet<>();
        authorities.add("ROLE_" + user.getRole().name());
        permissions.forEach(p -> authorities.add(p.name()));
        return authorities;
    }
}
