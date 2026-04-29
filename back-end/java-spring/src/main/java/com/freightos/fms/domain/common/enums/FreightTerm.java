package com.freightos.fms.domain.common.enums;

public enum FreightTerm {
    PREPAID,
    COLLECT;

    public static FreightTerm fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return FreightTerm.valueOf(code);
    }
}
