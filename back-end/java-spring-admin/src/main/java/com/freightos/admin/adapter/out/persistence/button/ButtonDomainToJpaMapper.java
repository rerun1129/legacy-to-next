package com.freightos.admin.adapter.out.persistence.button;

import com.freightos.admin.domain.button.entity.Button;
import org.springframework.stereotype.Component;

@Component
public class ButtonDomainToJpaMapper {

    public ButtonJpaEntity toNewJpa(Button domain) {
        ButtonJpaEntity entity = new ButtonJpaEntity();
        entity.setButtonCode(domain.getButtonCode());
        entity.setMenuId(domain.getMenuId());
        entity.setLabel(domain.getLabel());
        entity.setLabelEn(domain.getLabelEn());
        entity.setActionType(domain.getActionType().name());
        entity.setApiMethod(domain.getApiMethod());
        entity.setApiPath(domain.getApiPath());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.getActive());
        return entity;
    }

    /** 표시 필드만 반영. buttonCode는 식별 키이므로 변경하지 않는다. */
    public void applyUpdateFields(ButtonJpaEntity entity, Button patch) {
        entity.setMenuId(patch.getMenuId());
        entity.setLabel(patch.getLabel());
        entity.setActionType(patch.getActionType().name());
        entity.setApiMethod(patch.getApiMethod());
        entity.setApiPath(patch.getApiPath());
        entity.setSortOrder(patch.getSortOrder());
        entity.setActive(patch.getActive());
    }
}
