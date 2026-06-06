package com.freightos.pms.domain.enums;

/**
 * 업무 구분. PMS 필터 콤보 소비 전용.
 * FMS housebl.JobDiv 레이블 동기화(SEA/AIR/TRUCK/NON_BL).
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
