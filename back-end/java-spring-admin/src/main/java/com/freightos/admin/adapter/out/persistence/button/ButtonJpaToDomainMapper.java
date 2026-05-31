package com.freightos.admin.adapter.out.persistence.button;

import com.freightos.admin.domain.button.entity.ActionType;
import com.freightos.admin.domain.button.entity.Button;
import org.springframework.stereotype.Component;

@Component
public class ButtonJpaToDomainMapper {

    public Button toDomain(ButtonJpaEntity e) {
        Button domain = Button.create(e.getButtonCode(), e.getMenuId(), e.getLabel(), e.getLabelEn(), ActionType.valueOf(e.getActionType()), e.getApiMethod(), e.getApiPath(), e.getSortOrder(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }
}
