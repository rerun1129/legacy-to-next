package com.freightos.admin.adapter.out.persistence.attributevalue;

import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import org.springframework.stereotype.Component;

@Component
public class AttributeValueDomainToJpaMapper {

    public AttributeValueJpaEntity toNewJpa(AttributeValue domain) {
        AttributeValueJpaEntity entity = new AttributeValueJpaEntity();
        entity.setAttributeKey(domain.getAttributeKey());
        entity.setValue(domain.getValue());
        entity.setLabel(domain.getLabel());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.getActive());
        return entity;
    }

    /** label·sortOrder·active만 반영. (attributeKey, value) 는 변경하지 않는다. */
    public void applyUpdateFields(AttributeValueJpaEntity entity, AttributeValue patch) {
        entity.setLabel(patch.getLabel());
        entity.setSortOrder(patch.getSortOrder());
        entity.setActive(patch.getActive());
    }
}
