package com.freightos.admin.adapter.out.persistence.auth;

import com.freightos.admin.domain.auth.entity.RefreshToken;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenJpaToDomainMapper {

    public RefreshToken toDomain(RefreshTokenJpaEntity e) {
        RefreshToken domain = RefreshToken.issue(e.getUserId(), e.getTokenHash(), e.getExpiresAt());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getRevokedAt());
        return domain;
    }
}
