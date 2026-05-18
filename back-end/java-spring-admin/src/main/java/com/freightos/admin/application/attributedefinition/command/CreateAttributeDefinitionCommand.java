package com.freightos.admin.application.attributedefinition.command;

public record CreateAttributeDefinitionCommand(
        String attributeKey,
        String name,
        String description,
        String valueType,
        Boolean active
) {}
