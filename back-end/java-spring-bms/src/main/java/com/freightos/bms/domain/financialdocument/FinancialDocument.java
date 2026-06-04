package com.freightos.bms.domain.financialdocument;

import com.freightos.bms.domain.financialdocument.enums.DocumentStatus;
import com.freightos.bms.domain.financialdocument.enums.DocumentType;

import java.math.BigDecimal;

/**
 * 금융 서류 도메인 엔티티.
 * 순수 Java — Spring/JPA import 없음.
 */
public class FinancialDocument {

    private final String documentNo;
    private final DocumentType documentType;
    private final String documentDt;
    private final DocumentStatus status;
    private final String customerCode;
    private final BigDecimal settleTotalAmount;
    private final BigDecimal localTotalAmount;
    private final BigDecimal settleTotalVat;
    private final BigDecimal localTotalVat;
    private final BigDecimal usdTotalAmount;
    private final String performanceDt;
    private final String teamCode;
    private final String operator;

    private FinancialDocument(
            String documentNo,
            DocumentType documentType,
            String documentDt,
            DocumentStatus status,
            String customerCode,
            BigDecimal settleTotalAmount,
            BigDecimal localTotalAmount,
            BigDecimal settleTotalVat,
            BigDecimal localTotalVat,
            BigDecimal usdTotalAmount,
            String performanceDt,
            String teamCode,
            String operator) {
        this.documentNo = documentNo;
        this.documentType = documentType;
        this.documentDt = documentDt;
        this.status = status;
        this.customerCode = customerCode;
        this.settleTotalAmount = settleTotalAmount;
        this.localTotalAmount = localTotalAmount;
        this.settleTotalVat = settleTotalVat;
        this.localTotalVat = localTotalVat;
        this.usdTotalAmount = usdTotalAmount;
        this.performanceDt = performanceDt;
        this.teamCode = teamCode;
        this.operator = operator;
    }

    /**
     * 금융 서류 최초 발행 팩토리.
     * 상태는 항상 CREATED로 고정된다.
     */
    public static FinancialDocument issue(
            String documentNo,
            DocumentType documentType,
            String documentDt,
            String customerCode,
            BigDecimal settleTotalAmount,
            BigDecimal localTotalAmount,
            BigDecimal settleTotalVat,
            BigDecimal localTotalVat,
            BigDecimal usdTotalAmount,
            String performanceDt,
            String teamCode,
            String operator) {
        return new FinancialDocument(
            documentNo,
            documentType,
            documentDt,
            DocumentStatus.CREATED,
            customerCode,
            settleTotalAmount,
            localTotalAmount,
            settleTotalVat,
            localTotalVat,
            usdTotalAmount,
            performanceDt,
            teamCode,
            operator
        );
    }

    public String getDocumentNo() { return documentNo; }
    public DocumentType getDocumentType() { return documentType; }
    public String getDocumentDt() { return documentDt; }
    public DocumentStatus getStatus() { return status; }
    public String getCustomerCode() { return customerCode; }
    public BigDecimal getSettleTotalAmount() { return settleTotalAmount; }
    public BigDecimal getLocalTotalAmount() { return localTotalAmount; }
    public BigDecimal getSettleTotalVat() { return settleTotalVat; }
    public BigDecimal getLocalTotalVat() { return localTotalVat; }
    public BigDecimal getUsdTotalAmount() { return usdTotalAmount; }
    public String getPerformanceDt() { return performanceDt; }
    public String getTeamCode() { return teamCode; }
    public String getOperator() { return operator; }
}
