package com.freightos.admin.adapter.out.persistence.permissionpreset;

import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserPermissionPresetPersistenceAdapter implements UserPermissionPresetRepository {

    private final UserPermissionPresetJpaRepository jpaRepository;

    @Override
    public List<Long> findPresetIdsByUserId(Long userId) {
        return jpaRepository.findAllByUserId(userId)
                .stream()
                .map(UserPermissionPresetJpaEntity::getPresetId)
                .toList();
    }

    @Override
    public long countByPresetId(Long presetId) {
        return jpaRepository.countByPresetId(presetId);
    }
}
