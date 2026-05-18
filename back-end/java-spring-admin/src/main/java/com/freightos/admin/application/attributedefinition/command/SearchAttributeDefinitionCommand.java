package com.freightos.admin.application.attributedefinition.command;

public record SearchAttributeDefinitionCommand(
        String attributeKey,
        String name,
        String valueType,
        Boolean active,
        int page,
        int size
) {}
