package com.freightos.admin.adapter.out.persistence.menu;

import com.freightos.admin.domain.menu.entity.Menu;
import org.springframework.stereotype.Component;

@Component
public class MenuDomainToJpaMapper {

    public MenuJpaEntity toNewJpa(Menu domain) {
        MenuJpaEntity entity = new MenuJpaEntity();
        entity.setMenuCode(domain.getMenuCode());
        entity.setParentId(domain.getParentId());
        entity.setPath(domain.getPath());
        entity.setLabel(domain.getLabel());
        entity.setLabelEn(domain.getLabelEn());
        entity.setIcon(domain.getIcon());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.getActive());
        entity.setModuleCode(domain.getModuleCode());
        return entity;
    }

    /** 표시 필드만 반영. menuCode는 식별 키이므로 변경하지 않는다. */
    public void applyUpdateFields(MenuJpaEntity entity, Menu patch) {
        entity.setParentId(patch.getParentId());
        entity.setPath(patch.getPath());
        entity.setLabel(patch.getLabel());
        entity.setLabelEn(patch.getLabelEn());
        entity.setIcon(patch.getIcon());
        entity.setSortOrder(patch.getSortOrder());
        entity.setActive(patch.getActive());
        entity.setModuleCode(patch.getModuleCode());
    }
}
