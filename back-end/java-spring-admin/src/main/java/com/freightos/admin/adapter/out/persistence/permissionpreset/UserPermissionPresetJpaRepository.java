package com.freightos.admin.adapter.out.persistence.permissionpreset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPermissionPresetJpaRepository extends JpaRepository<UserPermissionPresetJpaEntity, Long> {
    List<UserPermissionPresetJpaEntity> findAllByUserId(Long userId);
    long countByPresetId(Long presetId);
    boolean existsByUserIdAndPresetId(Long userId, Long presetId);
}
