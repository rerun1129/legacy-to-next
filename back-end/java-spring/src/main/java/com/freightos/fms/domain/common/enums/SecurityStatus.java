package com.freightos.fms.domain.common.enums;

public enum SecurityStatus {
    SPX("SPX"),
    SCO("SCO"),
    UNK("UNK");

    private final String label;

    SecurityStatus(String label) { this.label = label; }

    public String getLabel() { return label; }

    public static SecurityStatus fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return SecurityStatus.valueOf(code);
    }
}
