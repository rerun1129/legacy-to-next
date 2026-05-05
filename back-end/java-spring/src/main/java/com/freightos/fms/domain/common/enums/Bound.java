package com.freightos.fms.domain.common.enums;

/** Export / Import */
public enum Bound {
    EXP("Export"),
    IMP("Import");

    private final String label;

    Bound(String label) { this.label = label; }

    public String getLabel() { return label; }
}
