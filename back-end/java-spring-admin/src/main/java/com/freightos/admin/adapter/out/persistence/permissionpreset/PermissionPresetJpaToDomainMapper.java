package com.freightos.admin.adapter.out.persistence.permissionpreset;

import com.freightos.admin.domain.permissionpreset.entity.AttributeValueRef;
import com.freightos.admin.domain.permissionpreset.entity.PermissionPreset;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PermissionPresetJpaToDomainMapper {

    public PermissionPreset toDomain(PermissionPresetJpaEntity e, List<AttributeValueRef> refs) {
        PermissionPreset domain = PermissionPreset.restore(
                e.getCode(), e.getName(), e.getDescription(), e.isActive(), refs
        );
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }

    /** attributeValueRefs 없이 프리셋만 변환 (refs 를 별도 로딩한 뒤 조합하지 않을 때). */
    public PermissionPreset toDomainWithoutRefs(PermissionPresetJpaEntity e) {
        return toDomain(e, List.of());
    }
}
