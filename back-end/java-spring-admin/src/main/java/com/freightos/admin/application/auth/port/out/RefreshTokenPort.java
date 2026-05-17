package com.freightos.admin.application.auth.port.out;

import com.freightos.admin.domain.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenPort {
    void save(RefreshToken token);
    Optional<RefreshToken> findActiveByTokenHash(String tokenHash);
    void revokeByTokenHash(String tokenHash);
}
