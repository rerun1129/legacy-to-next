package com.freightos.admin.adapter.out.persistence.module;

import com.freightos.admin.domain.module.entity.Module;
import org.springframework.stereotype.Component;

@Component
public class ModuleDomainToJpaMapper {

    public ModuleJpaEntity toNewJpa(Module domain) {
        ModuleJpaEntity entity = new ModuleJpaEntity();
        entity.setModuleCode(domain.getModuleCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.getActive());
        return entity;
    }

    /** 표시 필드만 반영. moduleCode는 식별 키이므로 변경하지 않는다. */
    public void applyUpdateFields(ModuleJpaEntity entity, Module patch) {
        entity.setName(patch.getName());
        entity.setDescription(patch.getDescription());
        entity.setSortOrder(patch.getSortOrder());
        entity.setActive(patch.getActive());
    }
}
