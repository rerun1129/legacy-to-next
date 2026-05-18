package com.freightos.admin.adapter.out.persistence.menupolicy;

import com.freightos.admin.domain.menupolicy.entity.MenuPolicy;
import org.springframework.stereotype.Component;

@Component
public class MenuPolicyJpaToDomainMapper {

    public MenuPolicy toDomain(MenuPolicyJpaEntity e) {
        MenuPolicy domain = MenuPolicy.create(e.getMenuId(), e.getAttributeKey(), e.getRequiredValue());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }
}
