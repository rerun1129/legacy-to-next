package com.freightos.admin.adapter.out.persistence.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {
    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);
}
