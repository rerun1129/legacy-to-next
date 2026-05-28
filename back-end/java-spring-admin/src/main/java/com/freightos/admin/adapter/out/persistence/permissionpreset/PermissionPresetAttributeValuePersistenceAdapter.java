package com.freightos.admin.adapter.out.persistence.permissionpreset;

import com.freightos.admin.application.permissionpreset.port.out.PermissionPresetAttributeValueRepository;
import com.freightos.admin.domain.permissionpreset.entity.AttributeValueRef;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PermissionPresetAttributeValuePersistenceAdapter implements PermissionPresetAttributeValueRepository {

    private final PermissionPresetAttributeValueJpaRepository jpaRepository;

    @Override
    public List<AttributeValueRef> findAttributeValueRefsByPresetId(Long presetId) {
        return jpaRepository.findAllByPresetId(presetId)
                .stream()
                .map(e -> new AttributeValueRef(e.getAttributeKey(), e.getAvValue()))
                .toList();
    }

    @Override
    public void saveAllByPresetId(Long presetId, List<AttributeValueRef> refs) {
        List<PermissionPresetAttributeValueJpaEntity> toSave = refs.stream()
                // 이미 존재하는 조합은 건너뛴다 (UNIQUE 제약 회피)
                .filter(ref -> !jpaRepository.existsByPresetIdAndAttributeKeyAndAvValue(presetId, ref.attributeKey(), ref.value()))
                .map(ref -> PermissionPresetAttributeValueJpaEntity.of(presetId, ref.attributeKey(), ref.value()))
                .toList();
        if (!toSave.isEmpty()) {
            jpaRepository.saveAll(toSave);
        }
    }

    @Override
    public void deleteByPresetIdAndRefsIn(Long presetId, List<AttributeValueRef> refs) {
        // 각 ref 를 개별 삭제 — 삭제 건수가 적으므로 루프 방식이 적절하다
        for (AttributeValueRef ref : refs) {
            jpaRepository.deleteByPresetIdAndAttributeKeyAndAvValue(presetId, ref.attributeKey(), ref.value());
        }
    }
}
