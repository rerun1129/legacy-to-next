package com.freightos.admin.adapter.in.web.attributedefinition.dto;

import java.util.List;

public record ModuleAttributeResponse(
        String attributeKey,
        String name,
        String valueType,
        boolean allowMulti,
        List<ValueOption> values
) {
    public record ValueOption(String value, String label) {}
}
