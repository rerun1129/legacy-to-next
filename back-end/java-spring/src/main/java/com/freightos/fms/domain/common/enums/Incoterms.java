package com.freightos.fms.domain.common.enums;

public enum Incoterms {
    EXW("EXW"),
    FCA("FCA"),
    CPT("CPT"),
    CIP("CIP"),
    DAP("DAP"),
    DPU("DPU"),
    DDP("DDP"),
    FAS("FAS"),
    FOB("FOB"),
    CFR("CFR"),
    CIF("CIF");

    private final String label;

    Incoterms(String label) { this.label = label; }

    public String getLabel() { return label; }

    public static Incoterms fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return Incoterms.valueOf(code);
    }
}
