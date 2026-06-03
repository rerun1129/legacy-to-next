package com.freightos.fms.domain.freight;

import com.freightos.fms.domain.common.enums.CustomerType;
import com.freightos.fms.domain.freight.enums.FinancialDocType;
import com.freightos.fms.domain.freight.enums.FreightType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FinancialDocTypePolicyTest {

    @Test
    void SELLING_일반고객_INVOICE() {
        FinancialDocType result = FinancialDocTypePolicy.resolve(FreightType.SELLING, CustomerType.CUSTOMER.name());
        assertThat(result).isEqualTo(FinancialDocType.INVOICE);
    }

    @Test
    void BUYING_일반고객_PAYMENT() {
        FinancialDocType result = FinancialDocTypePolicy.resolve(FreightType.BUYING, CustomerType.CUSTOMER.name());
        assertThat(result).isEqualTo(FinancialDocType.PAYMENT);
    }

    @Test
    void SELLING_PARTNER_DEBIT() {
        FinancialDocType result = FinancialDocTypePolicy.resolve(FreightType.SELLING, CustomerType.PARTNER.name());
        assertThat(result).isEqualTo(FinancialDocType.DEBIT);
    }

    @Test
    void BUYING_PARTNER_CREDIT() {
        FinancialDocType result = FinancialDocTypePolicy.resolve(FreightType.BUYING, CustomerType.PARTNER.name());
        assertThat(result).isEqualTo(FinancialDocType.CREDIT);
    }

    @Test
    void customerType_null이면_INVOICE_또는_PAYMENT() {
        assertThat(FinancialDocTypePolicy.resolve(FreightType.SELLING, null)).isEqualTo(FinancialDocType.INVOICE);
        assertThat(FinancialDocTypePolicy.resolve(FreightType.BUYING, null)).isEqualTo(FinancialDocType.PAYMENT);
    }

    @Test
    void customerType_빈문자열이면_INVOICE_또는_PAYMENT() {
        assertThat(FinancialDocTypePolicy.resolve(FreightType.SELLING, "")).isEqualTo(FinancialDocType.INVOICE);
        assertThat(FinancialDocTypePolicy.resolve(FreightType.BUYING, "")).isEqualTo(FinancialDocType.PAYMENT);
    }
}
