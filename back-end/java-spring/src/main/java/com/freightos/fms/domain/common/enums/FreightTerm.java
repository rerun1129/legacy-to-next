package com.freightos.fms.domain.common.enums;

public enum FreightTerm {
    PREPAID("Prepaid", "선불"),
    COLLECT("Collect", "후불");

    private final String label;
    private final String labelKo;

    FreightTerm(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }

    public static FreightTerm fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return FreightTerm.valueOf(code);
    }
}
