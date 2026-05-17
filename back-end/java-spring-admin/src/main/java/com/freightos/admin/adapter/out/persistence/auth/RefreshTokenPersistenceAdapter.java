package com.freightos.admin.adapter.out.persistence.auth;

import com.freightos.admin.application.auth.port.out.RefreshTokenPort;
import com.freightos.admin.domain.auth.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional
public class RefreshTokenPersistenceAdapter implements RefreshTokenPort {

    private final RefreshTokenRepository repository;
    private final RefreshTokenJpaToDomainMapper toDomainMapper;

    @Override
    public void save(RefreshToken token) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setUserId(token.getUserId());
        entity.setTokenHash(token.getTokenHash());
        entity.setExpiresAt(token.getExpiresAt());
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findActiveByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash)
            .filter(e -> e.getRevokedAt() == null)
            .filter(e -> e.getExpiresAt().isAfter(LocalDateTime.now()))
            .map(toDomainMapper::toDomain);
    }

    @Override
    public void revokeByTokenHash(String tokenHash) {
        repository.findByTokenHash(tokenHash).ifPresent(e -> e.setRevokedAt(LocalDateTime.now()));
    }
}
