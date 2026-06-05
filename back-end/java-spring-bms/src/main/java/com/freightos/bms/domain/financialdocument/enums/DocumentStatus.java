package com.freightos.bms.domain.financialdocument.enums;

/**
 * 금융 서류 상태. DB 저장값 = enum.name(). 상태 전이 우선순위 규칙 §6.12.
 * priority()가 낮을수록 초기 상태. 그룹 부여/해제 시 우선순위 비교로 승급/강등 여부를 결정한다.
 * 예: GROUPED(1) 미만만 GROUPED로 승급, GROUPED(1) 초과(TAX↑)는 강등 없음.
 */
public enum DocumentStatus {

    CREATED(0),
    GROUPED(1),
    TAX(2),
    SLIP(3),
    CLEAR(4);

    private final int priority;

    DocumentStatus(int priority) {
        this.priority = priority;
    }

    /** 상태 우선순위. 낮을수록 초기 상태. 그룹 승급/강등 판단에 사용. */
    public int priority() {
        return priority;
    }

    /** name 문자열 → enum. null/blank 이면 null 반환. 미존재 값은 IllegalArgumentException. */
    public static DocumentStatus fromName(String name) {
        if (name == null || name.isBlank()) return null;
        return DocumentStatus.valueOf(name);
    }
}
