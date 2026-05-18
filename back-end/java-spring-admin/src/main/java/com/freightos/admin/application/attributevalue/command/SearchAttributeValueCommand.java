package com.freightos.admin.application.attributevalue.command;

public record SearchAttributeValueCommand(
        String attributeKey,
        String value,
        Boolean active,
        int page,
        int size
) {}
