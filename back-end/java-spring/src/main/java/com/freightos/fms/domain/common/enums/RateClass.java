package com.freightos.fms.domain.common.enums;

public enum RateClass {
    M, N, Q, C;

    public static RateClass fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return RateClass.valueOf(code);
    }
}
