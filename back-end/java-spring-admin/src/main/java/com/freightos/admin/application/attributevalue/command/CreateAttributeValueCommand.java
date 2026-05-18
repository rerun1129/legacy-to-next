package com.freightos.admin.application.attributevalue.command;

public record CreateAttributeValueCommand(
        String attributeKey,
        String value,
        String label,
        Integer sortOrder,
        Boolean active
) {}
