package com.freightos.admin.application.attributedefinition;

import java.util.List;

public record ModuleAttributeResult(
        String attributeKey,
        String name,
        String valueType,
        boolean allowMulti,
        List<ValueEntry> values
) {
    public record ValueEntry(String value, String label) {}
}
