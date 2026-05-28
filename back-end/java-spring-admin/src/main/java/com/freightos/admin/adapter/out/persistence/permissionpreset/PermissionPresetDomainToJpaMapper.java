package com.freightos.admin.adapter.out.persistence.permissionpreset;

import com.freightos.admin.domain.permissionpreset.entity.PermissionPreset;
import org.springframework.stereotype.Component;

@Component
public class PermissionPresetDomainToJpaMapper {

    public PermissionPresetJpaEntity toNewJpa(PermissionPreset domain) {
        PermissionPresetJpaEntity e = new PermissionPresetJpaEntity();
        e.setCode(domain.getCode());
        e.setName(domain.getName());
        e.setDescription(domain.getDescription());
        e.setActive(domain.isActive());
        return e;
    }

    /** 기존 JPA 엔티티에 도메인 patch 를 적용한다 (update 전용). */
    public void applyPatch(PermissionPreset patch, PermissionPresetJpaEntity target) {
        target.setName(patch.getName());
        target.setDescription(patch.getDescription());
        target.setActive(patch.isActive());
    }
}
