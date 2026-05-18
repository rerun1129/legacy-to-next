package com.freightos.admin.adapter.out.persistence.menupolicy;

import com.freightos.admin.domain.menupolicy.entity.MenuPolicy;
import org.springframework.stereotype.Component;

@Component
public class MenuPolicyDomainToJpaMapper {

    public MenuPolicyJpaEntity toNewJpa(MenuPolicy domain) {
        MenuPolicyJpaEntity entity = new MenuPolicyJpaEntity();
        entity.setMenuId(domain.getMenuId());
        entity.setAttributeKey(domain.getAttributeKey());
        entity.setRequiredValue(domain.getRequiredValue());
        return entity;
    }
}
