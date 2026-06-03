package com.freightos.fms.domain.freight;

import com.freightos.fms.domain.common.enums.CustomerType;
import com.freightos.fms.domain.freight.enums.FinancialDocType;
import com.freightos.fms.domain.freight.enums.FreightType;

/**
 * §6.16 금융 서류 종류 자동 산정 정책.
 * customerType은 각 라인의 customer_code 기준(라인별 독립 판정).
 *
 * PARTNER: SELLING → DEBIT,  BUYING → CREDIT
 * 그 외(NULL 포함): SELLING → INVOICE, BUYING → PAYMENT
 */
public final class FinancialDocTypePolicy {

    private FinancialDocTypePolicy() {}

    /**
     * @param freightType  라인의 매출/매입 구분
     * @param customerType admin.customer.customer_type 문자열 (nullable, 미조회 시 null)
     * @return 자동 산정된 FinancialDocType
     */
    public static FinancialDocType resolve(FreightType freightType, String customerType) {
        boolean isPartner = CustomerType.PARTNER.name().equals(customerType);
        return switch (freightType) {
            case SELLING -> isPartner ? FinancialDocType.DEBIT : FinancialDocType.INVOICE;
            case BUYING  -> isPartner ? FinancialDocType.CREDIT : FinancialDocType.PAYMENT;
        };
    }
}
