package com.freightos.fms.domain.common.enums;

public enum ShipmentType {
    HOUSE("House"),
    DIRECT("Direct");

    private final String label;

    ShipmentType(String label) { this.label = label; }

    public String getLabel() { return label; }
}
