package com.freightos.admin.adapter.out.persistence.permissionpreset;

import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetAttributeValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionPresetAttributeValuePersistenceAdapter implements PermissionPresetAttributeValueRepository {

    private final PermissionPresetAttributeValueJpaRepository jpaRepository;

    @Override
    public List<Long> findAttributeValueIdsByPresetId(Long presetId) {
        return jpaRepository.findAllByPresetId(presetId)
                .stream()
                .map(PermissionPresetAttributeValueJpaEntity::getAttributeValueId)
                .toList();
    }

    @Override
    public void saveAllByPresetId(Long presetId, List<Long> attributeValueIds) {
        List<PermissionPresetAttributeValueJpaEntity> toSave = attributeValueIds.stream()
                // 이미 존재하는 조합은 건너뛴다 (UNIQUE 제약 회피)
                .filter(avId -> !jpaRepository.existsByPresetIdAndAttributeValueId(presetId, avId))
                .map(avId -> PermissionPresetAttributeValueJpaEntity.of(presetId, avId))
                .toList();
        if (!toSave.isEmpty()) {
            jpaRepository.saveAll(toSave);
        }
    }

    @Override
    public void deleteByPresetIdAndAttributeValueIdsIn(Long presetId, List<Long> attributeValueIds) {
        if (!attributeValueIds.isEmpty()) {
            jpaRepository.deleteByPresetIdAndAttributeValueIdIn(presetId, attributeValueIds);
        }
    }
}
