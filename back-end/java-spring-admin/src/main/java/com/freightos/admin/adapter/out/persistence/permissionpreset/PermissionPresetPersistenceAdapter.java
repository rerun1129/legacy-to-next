package com.freightos.admin.adapter.out.persistence.permissionpreset;

import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetRepository;
import com.freightos.admin.domain.permissionpreset.entity.AttributeValueRef;
import com.freightos.admin.domain.permissionpreset.entity.PermissionPreset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PermissionPresetPersistenceAdapter implements PermissionPresetRepository {

    private final PermissionPresetJpaRepository jpaRepository;
    private final PermissionPresetAttributeValueJpaRepository attributeValueJpaRepository;
    private final PermissionPresetJpaToDomainMapper jpaToDomainMapper;
    private final PermissionPresetDomainToJpaMapper domainToJpaMapper;

    @Override
    public Optional<PermissionPreset> findPermissionPresetById(Long presetId) {
        return jpaRepository.findById(presetId).map(e -> {
            List<AttributeValueRef> refs = loadRefs(presetId);
            return jpaToDomainMapper.toDomain(e, refs);
        });
    }

    @Override
    public Optional<PermissionPreset> findPermissionPresetByCode(String code) {
        return jpaRepository.findByCode(code).map(e -> {
            List<AttributeValueRef> refs = loadRefs(e.getId());
            return jpaToDomainMapper.toDomain(e, refs);
        });
    }

    @Override
    public boolean existsPermissionPresetByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public boolean existsPermissionPresetById(Long presetId) {
        return jpaRepository.existsById(presetId);
    }

    @Override
    public Long savePermissionPreset(PermissionPreset preset) {
        PermissionPresetJpaEntity entity = domainToJpaMapper.toNewJpa(preset);
        jpaRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void updatePermissionPreset(Long presetId, PermissionPreset patch) {
        PermissionPresetJpaEntity entity = jpaRepository.getReferenceById(presetId);
        domainToJpaMapper.applyPatch(patch, entity);
        jpaRepository.save(entity);
    }

    @Override
    public void deletePermissionPresetById(Long presetId) {
        jpaRepository.deleteById(presetId);
    }

    @Override
    public List<PermissionPreset> findAllPermissionPresets(boolean activeOnly) {
        List<PermissionPresetJpaEntity> entities = activeOnly
                ? jpaRepository.findAllByActive(true)
                : jpaRepository.findAll();

        return entities.stream().map(e -> {
            List<AttributeValueRef> refs = loadRefs(e.getId());
            return jpaToDomainMapper.toDomain(e, refs);
        }).toList();
    }

    private List<AttributeValueRef> loadRefs(Long presetId) {
        return attributeValueJpaRepository.findAllByPresetId(presetId)
                .stream()
                .map(e -> new AttributeValueRef(e.getAttributeKey(), e.getAvValue()))
                .toList();
    }
}
