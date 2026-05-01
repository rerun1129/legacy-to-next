package com.freightos.fms.domain.common.enums;

public enum DescClause1 {
    A("SHIPPER'S LOAD & COUNT"),
    B("SHIPPER'S LOAD AND COUNT"),
    C("SHIPPER'S LOAD, COUNT, STOW & SEAL"),
    D("SHIPPER'S LOAD & COUNT & SEALED"),
    E("SHIPPER'S RISK & DAMAGE FOR ON DECK CARGO"),
    F("SHIPPER'S LOAD, STOW, WEIGHT, COUNT & SEAL"),
    G("PART OF CONTAINER");

    public final String label;

    DescClause1(String label) { this.label = label; }

    public static DescClause1 fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return DescClause1.valueOf(code);
    }
}
