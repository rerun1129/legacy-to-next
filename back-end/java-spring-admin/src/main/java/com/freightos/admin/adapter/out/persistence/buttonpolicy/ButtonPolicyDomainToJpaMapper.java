package com.freightos.admin.adapter.out.persistence.buttonpolicy;

import com.freightos.admin.domain.buttonpolicy.entity.ButtonPolicy;
import org.springframework.stereotype.Component;

@Component
public class ButtonPolicyDomainToJpaMapper {

    public ButtonPolicyJpaEntity toNewJpa(ButtonPolicy domain) {
        ButtonPolicyJpaEntity entity = new ButtonPolicyJpaEntity();
        entity.setButtonId(domain.getButtonId());
        entity.setAttributeKey(domain.getAttributeKey());
        entity.setRequiredValue(domain.getRequiredValue());
        return entity;
    }
}
