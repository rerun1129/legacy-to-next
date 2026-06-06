package com.freightos.pms.domain.enums;

/**
 * 항구 기준 구분. PMS 필터 콤보 소비 전용.
 */
public enum PortKind {
    POL("POL", "POL"),
    POD("POD", "POD");

    private final String label;
    private final String labelKo;

    PortKind(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }
}
