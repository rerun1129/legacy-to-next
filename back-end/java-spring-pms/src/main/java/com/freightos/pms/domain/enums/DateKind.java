package com.freightos.pms.domain.enums;

/**
 * 날짜 기준 구분. PMS 필터 콤보 소비 전용.
 */
public enum DateKind {
    ETD("ETD", "ETD"),
    ETA("ETA", "ETA"),
    PERF("Perf. Date", "실적일자"),
    DOC("Doc. Date", "서류일자");

    private final String label;
    private final String labelKo;

    DateKind(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }
}
