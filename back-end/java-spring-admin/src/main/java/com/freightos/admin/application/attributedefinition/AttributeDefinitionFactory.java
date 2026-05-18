package com.freightos.admin.application.attributedefinition;

import com.freightos.admin.application.attributedefinition.command.CreateAttributeDefinitionCommand;
import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;
import com.freightos.admin.domain.attributedefinition.entity.ValueType;
import org.springframework.stereotype.Component;

@Component
public class AttributeDefinitionFactory {

    public AttributeDefinition from(CreateAttributeDefinitionCommand command) {
        return AttributeDefinition.create(
                command.attributeKey(),
                command.name(),
                command.description(),
                ValueType.valueOf(command.valueType()),
                command.active()
        );
    }
}
