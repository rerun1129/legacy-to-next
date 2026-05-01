package com.freightos.fms.domain.common.enums;

public enum DescClause2 {
    A("SAID TO CONTAIN :"),
    B("SAID TO BE :"),
    C("SHIPPER'S WEIGHT & MEASUREMENT");

    public final String label;

    DescClause2(String label) { this.label = label; }

    public static DescClause2 fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return DescClause2.valueOf(code);
    }
}
