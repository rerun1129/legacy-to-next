package com.freightos.fms.domain.common.enums;

public enum FreightTerm {
    PREPAID("Prepaid"),
    COLLECT("Collect");

    private final String label;

    FreightTerm(String label) { this.label = label; }

    public String getLabel() { return label; }

    public static FreightTerm fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return FreightTerm.valueOf(code);
    }
}
