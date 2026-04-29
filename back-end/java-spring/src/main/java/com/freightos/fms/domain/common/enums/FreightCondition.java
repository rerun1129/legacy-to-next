package com.freightos.fms.domain.common.enums;

public enum FreightCondition {
    P("Prepaid"),
    C("Collect");

    private final String description;

    FreightCondition(String description) { this.description = description; }

    public String getDescription() { return description; }

    public static FreightCondition fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return FreightCondition.valueOf(code);
    }
}
