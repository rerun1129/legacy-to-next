package com.freightos.fms.domain.common.enums;

public enum PackageUnit {
    KGS, LBS;

    public static PackageUnit fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return PackageUnit.valueOf(code);
    }
}
