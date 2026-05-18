package com.freightos.admin.adapter.out.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserJpaEntity, Long>, UserRepositoryCustom {
    Optional<UserJpaEntity> findByUsernameAndDeletedAtIsNull(String username);
    List<UserJpaEntity> findAllByActiveTrueAndDeletedAtIsNull();
}
