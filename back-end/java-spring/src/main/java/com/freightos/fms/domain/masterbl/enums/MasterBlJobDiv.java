package com.freightos.fms.domain.masterbl.enums;

public enum MasterBlJobDiv {
    SEA("Sea", "해상"),
    AIR("Air", "항공");

    private final String label;
    private final String labelKo;

    MasterBlJobDiv(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }

    public static MasterBlJobDiv fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        try {
            return MasterBlJobDiv.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
