package com.freightos.fms.domain.housebl.enums;

/**
 * E-08 기본 속성. 어느 Entry 화면에서 접근했는지에 따라 자동 결정 — 사용자 입력 불가.
 * DDL 컬럼명: job_div
 */
public enum JobDiv {
    SEA("Sea", "해상"),
    AIR("Air", "항공"),
    TRUCK("Truck", "육로"),
    NON_BL("Non B/L", null);

    private final String label;
    private final String labelKo;

    JobDiv(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }
}
