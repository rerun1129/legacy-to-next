package com.freightos.fms.domain.common.enums;

public enum SortDirection {
    ASC("ASC"),
    DESC("DESC");

    private final String label;

    SortDirection(String label) { this.label = label; }

    public String getLabel() { return label; }
}
