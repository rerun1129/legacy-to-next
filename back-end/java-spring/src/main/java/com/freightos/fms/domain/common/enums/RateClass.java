package com.freightos.fms.domain.common.enums;

public enum RateClass {
    M("M"),
    N("N"),
    Q("Q"),
    C("C");

    private final String label;

    RateClass(String label) { this.label = label; }

    public String getLabel() { return label; }

    public static RateClass fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return RateClass.valueOf(code);
    }
}
