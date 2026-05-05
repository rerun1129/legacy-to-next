package com.freightos.fms.domain.masterbl.enums;

public enum MasterBlJobDiv {
    SEA("Sea"),
    AIR("Air");

    private final String label;

    MasterBlJobDiv(String label) { this.label = label; }

    public String getLabel() { return label; }

    public static MasterBlJobDiv fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        try {
            return MasterBlJobDiv.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
