package com.freightos.fms.domain.freight.enums;

/**
 * 운임 헤더가 참조하는 B/L 종류 — HOUSE | MASTER.
 * DDL: bms.freight_header.bl_type CHECK('HOUSE','MASTER').
 * DB 저장값 = enum.name().
 */
public enum FreightBlType {
    HOUSE,
    MASTER;

    /** name 문자열 → enum. null/blank 이면 null 반환. */
    public static FreightBlType fromName(String name) {
        if (name == null || name.isBlank()) return null;
        return FreightBlType.valueOf(name);
    }
}
