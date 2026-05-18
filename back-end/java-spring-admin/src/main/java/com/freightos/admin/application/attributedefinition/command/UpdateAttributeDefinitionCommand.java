package com.freightos.admin.application.attributedefinition.command;

public record UpdateAttributeDefinitionCommand(
        String name,
        String description,
        String valueType,
        Boolean active,
        Boolean allowMulti
) {}
