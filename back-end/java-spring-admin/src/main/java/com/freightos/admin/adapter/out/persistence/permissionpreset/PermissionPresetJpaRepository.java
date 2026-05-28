package com.freightos.admin.adapter.out.persistence.permissionpreset;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionPresetJpaRepository extends JpaRepository<PermissionPresetJpaEntity, Long>, PermissionPresetRepositoryCustom {
    Optional<PermissionPresetJpaEntity> findByCode(String code);
    boolean existsByCode(String code);
    List<PermissionPresetJpaEntity> findAllByActive(boolean active);
}
