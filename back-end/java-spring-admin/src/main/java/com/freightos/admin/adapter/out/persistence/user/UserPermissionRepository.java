package com.freightos.admin.adapter.out.persistence.user;

import com.freightos.admin.domain.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserPermissionRepository
        extends JpaRepository<UserPermissionJpaEntity, UserPermissionJpaEntity.UserPermissionId> {

    List<UserPermissionJpaEntity> findAllByUserId(Long userId);

    @Transactional
    long deleteAllByUserId(Long userId);
}
