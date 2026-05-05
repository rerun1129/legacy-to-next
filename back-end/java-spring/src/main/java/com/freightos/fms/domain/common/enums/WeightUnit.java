package com.freightos.fms.domain.common.enums;

public enum WeightUnit {
    KGS("KGS"),
    LBS("LBS");

    private final String label;

    WeightUnit(String label) { this.label = label; }

    public String getLabel() { return label; }

    public static WeightUnit fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return WeightUnit.valueOf(code);
    }
}
