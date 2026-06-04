package com.freightos.bms.domain.financialdocument.enums;

/**
 * 금융 서류 종류. DB 저장값 = enum.name().
 * 채번 접두어 이니셜(initial)은 서류 번호 규칙 §6.11에 따른다.
 */
public enum DocumentType {

    INVOICE("I"),
    PAYMENT("P"),
    DEBIT("D"),
    CREDIT("C");

    private final String initial;

    DocumentType(String initial) {
        this.initial = initial;
    }

    /** 채번 이니셜. 예: INVOICE → "I" */
    public String initial() {
        return initial;
    }

    /** name 문자열 → enum. null/blank 이면 null 반환. 미존재 값은 IllegalArgumentException. */
    public static DocumentType fromName(String name) {
        if (name == null || name.isBlank()) return null;
        return DocumentType.valueOf(name);
    }
}
