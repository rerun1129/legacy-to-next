package com.freightos.pms.application.pms;

/**
 * 집계 모수 기준 선택자.
 * - FREIGHT_INPUT: 운임 입력 건 기준 (freight_line 전체, 조건 없음)
 * - DOCUMENT_CREATED: 서류 생성 기준 (financial_document 합계 사용)
 * - TAX_ISSUED: 세금계산서 발급 기준 (freight_line WHERE tax_no IS NOT NULL)
 * - SLIP_ISSUED: 전표 발급 기준 (freight_line WHERE slip_no IS NOT NULL)
 */
public enum AggregationBasis {
    FREIGHT_INPUT("Freight Input", "운임 입력 건"),
    DOCUMENT_CREATED("Document Created", "서류 생성"),
    TAX_ISSUED("Tax Invoice Issued", "세금계산서 발행"),
    SLIP_ISSUED("Slip Issued", "전표 발행");

    private final String label;
    private final String labelKo;

    AggregationBasis(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }
}
