package com.freightos.fms.domain.common.enums;

public enum WeightUnit {
    KGS, LBS;

    public static WeightUnit fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return WeightUnit.valueOf(code);
    }
}
