package com.freightos.bms.domain.financialdocument;

import com.freightos.bms.domain.financialdocument.enums.IssueType;

/**
 * 발급 번호 값 객체. 단계 E 채번 규칙: 이니셜 + YYMM + 5자리 시퀀스.
 * TAX: T + YYMM + seq5 → 예: "T260600001"
 * SLIP: S + YYMM + seq5 → 예: "S260600001"
 * 순수 VO — Spring/JPA import 없음.
 */
public record IssueNo(String value) {

    /**
     * 발급 번호 생성.
     * 예: IssueType.TAX, "2606", 1 → "T260600001"
     */
    public static IssueNo of(IssueType type, String yymm, int seq) {
        String formatted = type.initial() + yymm + String.format("%05d", seq);
        return new IssueNo(formatted);
    }
}
