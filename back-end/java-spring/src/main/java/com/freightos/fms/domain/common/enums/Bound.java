package com.freightos.fms.domain.common.enums;

/** Export / Import */
public enum Bound {
    EXP("Export", "수출"),
    IMP("Import", "수입");

    private final String label;
    private final String labelKo;

    Bound(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }
}
