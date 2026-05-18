package com.freightos.admin.domain.buttonpolicy.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

@Getter
public class ButtonPolicy extends BaseEntity {

    private final Long buttonId;
    private final String attributeKey;
    private final String requiredValue;

    private ButtonPolicy(Long buttonId, String attributeKey, String requiredValue) {
        this.buttonId      = buttonId;
        this.attributeKey  = attributeKey;
        this.requiredValue = requiredValue;
    }

    public static ButtonPolicy create(Long buttonId, String attributeKey, String requiredValue) {
        return new ButtonPolicy(buttonId, attributeKey, requiredValue);
    }
}
