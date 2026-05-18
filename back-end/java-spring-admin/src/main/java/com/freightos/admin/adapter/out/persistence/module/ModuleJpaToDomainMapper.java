package com.freightos.admin.adapter.out.persistence.module;

import com.freightos.admin.domain.module.entity.Module;
import org.springframework.stereotype.Component;

@Component
public class ModuleJpaToDomainMapper {

    public Module toDomain(ModuleJpaEntity e) {
        Module domain = Module.create(e.getModuleCode(), e.getName(), e.getDescription(), e.getSortOrder(), e.getActive());
        domain.assignAudit(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }
}
