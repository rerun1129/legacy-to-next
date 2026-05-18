package com.freightos.admin.adapter.out.persistence.attributedefinition;

import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;
import org.springframework.stereotype.Component;

@Component
public class AttributeDefinitionDomainToJpaMapper {

    public AttributeDefinitionJpaEntity toNewJpa(AttributeDefinition domain) {
        AttributeDefinitionJpaEntity entity = new AttributeDefinitionJpaEntity();
        entity.setAttributeKey(domain.getAttributeKey());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setValueType(domain.getValueType().name());
        entity.setActive(domain.getActive());
        return entity;
    }

    /** 표시 필드만 반영. attributeKey는 식별 키이므로 변경하지 않는다. */
    public void applyUpdateFields(AttributeDefinitionJpaEntity entity, AttributeDefinition patch) {
        entity.setName(patch.getName());
        entity.setDescription(patch.getDescription());
        entity.setValueType(patch.getValueType().name());
        entity.setActive(patch.getActive());
    }
}
