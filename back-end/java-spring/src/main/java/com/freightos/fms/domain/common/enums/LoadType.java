package com.freightos.fms.domain.common.enums;

public enum LoadType {
    FCL("FCL"),
    LCL("LCL"),
    BULK("BULK");

    private final String label;

    LoadType(String label) { this.label = label; }

    public String getLabel() { return label; }
}
