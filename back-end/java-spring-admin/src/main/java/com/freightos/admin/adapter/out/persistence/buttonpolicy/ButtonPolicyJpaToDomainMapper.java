package com.freightos.admin.adapter.out.persistence.buttonpolicy;

import com.freightos.admin.domain.buttonpolicy.entity.ButtonPolicy;
import org.springframework.stereotype.Component;

@Component
public class ButtonPolicyJpaToDomainMapper {

    public ButtonPolicy toDomain(ButtonPolicyJpaEntity e) {
        ButtonPolicy domain = ButtonPolicy.create(e.getButtonId(), e.getAttributeKey(), e.getRequiredValue());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }
}
