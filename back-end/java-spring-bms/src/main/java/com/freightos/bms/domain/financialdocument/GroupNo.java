package com.freightos.bms.domain.financialdocument;

import com.freightos.bms.domain.financialdocument.enums.GroupCategory;

/**
 * 그룹 금융 번호 값 객체. §6.12 그룹 채번 규칙:
 * 'G' + 카테고리이니셜(I/P/D) + YYMM + 5자리 시퀀스.
 * 예: GroupCategory.INVOICE, "2606", 1 → "GI260600001"
 * 순수 VO — Spring/JPA import 없음.
 */
public record GroupNo(String value) {

    /**
     * 그룹 번호 생성.
     * 예: GroupCategory.INVOICE, "2606", 1 → "GI260600001"
     */
    public static GroupNo of(GroupCategory category, String yymm, int seq) {
        String formatted = "G" + category.initial() + yymm + String.format("%05d", seq);
        return new GroupNo(formatted);
    }
}
