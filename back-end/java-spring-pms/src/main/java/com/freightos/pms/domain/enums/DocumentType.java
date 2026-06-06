package com.freightos.pms.domain.enums;

/**
 * 금융 서류 종류. PMS 필터 콤보 소비 전용.
 * BMS financialdocument.DocumentType 레이블 동기화.
 */
public enum DocumentType {
    INVOICE("Invoice", "청구서"),
    PAYMENT("Payment", "수금"),
    DEBIT("Debit", "차변"),
    CREDIT("Credit", "대변");

    private final String label;
    private final String labelKo;

    DocumentType(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }
}
