package com.freightos.fms.domain.freight.enums;

/**
 * 운임 타입 — 매출(SELLING) / 매입(BUYING).
 * DB 저장값 = enum.name().
 */
public enum FreightType {
    SELLING,
    BUYING;

    /** name 문자열 → enum. null/blank 이면 null 반환. */
    public static FreightType fromName(String name) {
        if (name == null || name.isBlank()) return null;
        return FreightType.valueOf(name);
    }
}
