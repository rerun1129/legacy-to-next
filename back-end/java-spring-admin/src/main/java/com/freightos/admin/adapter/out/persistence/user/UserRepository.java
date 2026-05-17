package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.domain.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserJpaEntity, Long>, UserRepositoryCustom {
    Optional<UserJpaEntity> findByUsernameAndDeletedAtIsNull(String username);
    long countByRoleAndActiveTrueAndDeletedAtIsNull(UserRole role);
}
