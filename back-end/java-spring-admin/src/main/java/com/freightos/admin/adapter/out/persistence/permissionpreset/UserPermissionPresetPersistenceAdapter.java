package com.freightos.admin.adapter.out.persistence.permissionpreset;

import com.freightos.admin.application.permissionpreset.port.out.UserPermissionPresetRepository;
import com.freightos.admin.application.permissionpreset.projection.UserPermissionPresetRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserPermissionPresetPersistenceAdapter implements UserPermissionPresetRepository {

    private final UserPermissionPresetJpaRepository jpaRepository;
    private final UserPermissionPresetJpaQueryRepository queryRepository;

    @Override
    public List<Long> findPresetIdsByUserId(Long userId) {
        return jpaRepository.findAllByUserId(userId)
                .stream()
                .map(UserPermissionPresetJpaEntity::getPresetId)
                .toList();
    }

    @Override
    public List<UserPermissionPresetRow> findRowsByUserId(Long userId) {
        return queryRepository.findRowsByUserId(userId);
    }

    @Override
    public long countByPresetId(Long presetId) {
        return jpaRepository.countByPresetId(presetId);
    }

    @Override
    public boolean existsByUserIdAndPresetId(Long userId, Long presetId) {
        return jpaRepository.existsByUserIdAndPresetId(userId, presetId);
    }

    @Override
    public Long saveUserPermissionPreset(Long userId, Long presetId) {
        UserPermissionPresetJpaEntity entity = new UserPermissionPresetJpaEntity();
        entity.setUserId(userId);
        entity.setPresetId(presetId);
        jpaRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void deleteUserPermissionPresetById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsUserPermissionPresetById(Long id) {
        return jpaRepository.existsById(id);
    }
}
