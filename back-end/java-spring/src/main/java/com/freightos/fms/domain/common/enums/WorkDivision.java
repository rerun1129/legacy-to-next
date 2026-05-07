package com.freightos.fms.domain.common.enums;

public enum WorkDivision {
    SEA("Sea"),
    AIR("Air"),
    WAREHOUSE("Warehouse"),
    TRUCKING("Trucking");

    private final String label;

    WorkDivision(String label) { this.label = label; }

    public String getLabel() { return label; }
}
