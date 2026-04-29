package com.freightos.fms.domain.common.enums;

public enum SecurityStatus {
    SPX, SCO, UNK;

    public static SecurityStatus fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return SecurityStatus.valueOf(code);
    }
}
