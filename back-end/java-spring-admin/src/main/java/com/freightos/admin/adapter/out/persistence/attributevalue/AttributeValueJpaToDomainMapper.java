package com.freightos.admin.adapter.out.persistence.attributevalue;

import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import org.springframework.stereotype.Component;

@Component
public class AttributeValueJpaToDomainMapper {

    public AttributeValue toDomain(AttributeValueJpaEntity e) {
        AttributeValue domain = AttributeValue.create(e.getAttributeKey(), e.getValue(), e.getLabel(), e.getSortOrder(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }
}
