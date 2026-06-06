package com.freightos.pms.domain.enums;

/**
 * 수출/수입 구분. PMS 필터 콤보 소비 전용.
 * FMS common.Bound 레이블 동기화.
 */
public enum Bound {
    EXP("Export", "수출"),
    IMP("Import", "수입");

    private final String label;
    private final String labelKo;

    Bound(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }
}
