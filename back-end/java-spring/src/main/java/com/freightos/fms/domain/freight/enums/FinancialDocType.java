package com.freightos.fms.domain.freight.enums;

/**
 * 금융 서류 종류 — §6.16 자동 산정.
 * DB 저장값 = enum.name().
 */
public enum FinancialDocType {
    INVOICE,
    PAYMENT,
    DEBIT,
    CREDIT;

    /** name 문자열 → enum. null/blank 이면 null 반환. 미존재 값은 IllegalArgumentException. */
    public static FinancialDocType fromName(String name) {
        if (name == null || name.isBlank()) return null;
        return FinancialDocType.valueOf(name);
    }
}
