package com.freightos.pms.domain.enums;

/**
 * 예/아니오 구분. PMS 필터 콤보 소비 전용.
 */
public enum YesNo {
    Y("Yes", "예"),
    N("No", "아니오");

    private final String label;
    private final String labelKo;

    YesNo(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }
}
