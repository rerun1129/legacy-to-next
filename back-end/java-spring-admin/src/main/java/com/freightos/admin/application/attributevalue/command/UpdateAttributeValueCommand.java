package com.freightos.admin.application.attributevalue.command;

public record UpdateAttributeValueCommand(
        String label,
        Integer sortOrder,
        Boolean active
) {}
