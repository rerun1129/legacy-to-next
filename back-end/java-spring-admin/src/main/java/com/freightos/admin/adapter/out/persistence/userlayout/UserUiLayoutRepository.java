package com.freightos.admin.adapter.out.persistence.userlayout;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserUiLayoutRepository extends JpaRepository<UserUiLayoutJpaEntity, Long> {

    Optional<UserUiLayoutJpaEntity> findByUserIdAndStorageKey(Long userId, String storageKey);

    void deleteByUserIdAndStorageKey(Long userId, String storageKey);
}
