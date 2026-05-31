package com.freightos.fms.domain.common.enums;

public enum WorkDivision {
    SEA("Sea", "해상"),
    AIR("Air", "항공"),
    WAREHOUSE("Warehouse", "창고"),
    TRUCKING("Trucking", "육상운송");

    private final String label;
    private final String labelKo;

    WorkDivision(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }
}
