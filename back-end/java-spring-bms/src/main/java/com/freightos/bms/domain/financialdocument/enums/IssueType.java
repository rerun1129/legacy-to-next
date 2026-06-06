package com.freightos.bms.domain.financialdocument.enums;

/**
 * 발급 종류. DB 저장값 = enum.name() (TAX / SLIP).
 * 채번 접두어 이니셜(initial)은 발급 번호 규칙(단계 E)에 따른다.
 * T→세금계산서, S→전표.
 */
public enum IssueType {

    TAX("T"),
    SLIP("S");

    private final String initial;

    IssueType(String initial) {
        this.initial = initial;
    }

    /** 채번 이니셜. 예: TAX → "T" */
    public String initial() {
        return initial;
    }

    /** name 문자열 → enum. null/blank 이면 null 반환. 미존재 값은 IllegalArgumentException. */
    public static IssueType fromName(String name) {
        if (name == null || name.isBlank()) return null;
        return IssueType.valueOf(name);
    }
}
