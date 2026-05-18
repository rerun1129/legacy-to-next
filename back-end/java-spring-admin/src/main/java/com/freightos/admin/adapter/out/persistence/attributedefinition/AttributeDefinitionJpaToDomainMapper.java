package com.freightos.admin.adapter.out.persistence.attributedefinition;

import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;
import com.freightos.admin.domain.attributedefinition.entity.ValueType;
import org.springframework.stereotype.Component;

@Component
public class AttributeDefinitionJpaToDomainMapper {

    public AttributeDefinition toDomain(AttributeDefinitionJpaEntity e) {
        AttributeDefinition domain = AttributeDefinition.create(
                e.getAttributeKey(), e.getName(), e.getDescription(),
                ValueType.valueOf(e.getValueType()), e.getActive());
        domain.assignAudit(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }
}
