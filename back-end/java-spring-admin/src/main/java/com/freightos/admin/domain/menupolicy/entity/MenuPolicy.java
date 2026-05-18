package com.freightos.admin.domain.menupolicy.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

@Getter
public class MenuPolicy extends BaseEntity {

    private final Long menuId;
    private final String attributeKey;
    private final String requiredValue;

    private MenuPolicy(Long menuId, String attributeKey, String requiredValue) {
        this.menuId        = menuId;
        this.attributeKey  = attributeKey;
        this.requiredValue = requiredValue;
    }

    public static MenuPolicy create(Long menuId, String attributeKey, String requiredValue) {
        return new MenuPolicy(menuId, attributeKey, requiredValue);
    }
}
