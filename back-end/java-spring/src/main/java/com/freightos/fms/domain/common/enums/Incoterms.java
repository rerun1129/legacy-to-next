package com.freightos.fms.domain.common.enums;

public enum Incoterms {
    EXW, FCA, CPT, CIP, DAP, DPU, DDP, FAS, FOB, CFR, CIF;

    public static Incoterms fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return Incoterms.valueOf(code);
    }
}
