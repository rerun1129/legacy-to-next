package com.freightos.fms.domain.common.enums;

public enum BlType {
    ORIGINAL("Original"),
    SURRENDER("Surrender"),
    SEAWAY("Sea-Way Bill"),
    NORMAL("Normal"),
    EXPRESS("Express");

    private final String label;

    BlType(String label) { this.label = label; }

    public String getLabel() { return label; }
}
