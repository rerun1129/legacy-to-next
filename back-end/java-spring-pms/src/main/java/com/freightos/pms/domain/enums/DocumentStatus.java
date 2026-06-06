package com.freightos.pms.domain.enums;

/**
 * 금융 서류 상태. PMS 필터 콤보 소비 전용.
 * BMS financialdocument.DocumentStatus 상태값 동기화.
 */
public enum DocumentStatus {
    CREATED("Created", "생성"),
    GROUPED("Grouped", "그룹화"),
    TAX("Tax Issued", "세금계산서 발행"),
    SLIP("Slip Issued", "전표 발행"),
    CLEAR("Cleared", "완료");

    private final String label;
    private final String labelKo;

    DocumentStatus(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }
}
