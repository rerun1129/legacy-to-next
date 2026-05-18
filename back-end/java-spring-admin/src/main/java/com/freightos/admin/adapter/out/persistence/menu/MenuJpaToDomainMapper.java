package com.freightos.admin.adapter.out.persistence.menu;

import com.freightos.admin.domain.menu.entity.Menu;
import org.springframework.stereotype.Component;

@Component
public class MenuJpaToDomainMapper {

    public Menu toDomain(MenuJpaEntity e) {
        Menu domain = Menu.create(e.getMenuCode(), e.getParentId(), e.getPath(), e.getLabel(), e.getLabelEn(), e.getIcon(), e.getSortOrder(), e.getActive(), e.getModuleCode());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }
}
