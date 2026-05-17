package com.freightos.admin.domain.auth.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RefreshToken {

    private Long id;
    private final Long userId;
    private final String tokenHash;
    private final LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createdAt;

    private RefreshToken(Long userId, String tokenHash, LocalDateTime expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public static RefreshToken issue(Long userId, String tokenHash, LocalDateTime expiresAt) {
        return new RefreshToken(userId, tokenHash, expiresAt);
    }

    public void revoke() { this.revokedAt = LocalDateTime.now(); }

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(LocalDateTime.now());
    }

    public void assignIdentity(Long id, LocalDateTime createdAt, LocalDateTime revokedAt) {
        this.id = id;
        this.createdAt = createdAt;
        this.revokedAt = revokedAt;
    }
}
