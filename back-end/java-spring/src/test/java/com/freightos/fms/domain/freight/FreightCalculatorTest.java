package com.freightos.fms.domain.freight;

import com.freightos.fms.domain.freight.enums.FreightType;
import com.freightos.fms.domain.freight.enums.TaxType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FreightCalculatorTest {

    @Test
    void settleAmount_정상계산() {
        BigDecimal result = FreightCalculator.calcSettleAmount(new BigDecimal("3"), new BigDecimal("100.50"));
        assertThat(result).isEqualByComparingTo("301.50");
    }

    @Test
    void settleAmount_null입력_ZERO반환() {
        assertThat(FreightCalculator.calcSettleAmount(null, new BigDecimal("100"))).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(FreightCalculator.calcSettleAmount(new BigDecimal("3"), null)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void exchangeRate_SELLING이면_sellRate() {
        BigDecimal result = FreightCalculator.resolveExchangeRate(FreightType.SELLING, new BigDecimal("1300"), new BigDecimal("1200"));
        assertThat(result).isEqualByComparingTo("1300.000000");
    }

    @Test
    void exchangeRate_BUYING이면_buyRate() {
        BigDecimal result = FreightCalculator.resolveExchangeRate(FreightType.BUYING, new BigDecimal("1300"), new BigDecimal("1200"));
        assertThat(result).isEqualByComparingTo("1200.000000");
    }

    @Test
    void settleTaxAmount_TAXABLE이면_10퍼센트() {
        BigDecimal result = FreightCalculator.calcSettleTaxAmount(TaxType.TAXABLE, new BigDecimal("1000"));
        assertThat(result).isEqualByComparingTo("100.00");
    }

    @Test
    void settleTaxAmount_ZERO_RATED이면_ZERO() {
        BigDecimal result = FreightCalculator.calcSettleTaxAmount(TaxType.ZERO_RATED, new BigDecimal("1000"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void settleTaxAmount_EXEMPT이면_ZERO() {
        BigDecimal result = FreightCalculator.calcSettleTaxAmount(TaxType.EXEMPT, new BigDecimal("1000"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void applyCalculations_전체계산_정합성() {
        FreightLine line = new FreightLine();
        line.setFreightType(FreightType.SELLING);
        line.setUnitQuantity(new BigDecimal("2"));
        line.setUnitPrice(new BigDecimal("500"));
        line.setTaxType(TaxType.TAXABLE);

        BigDecimal sellRate = new BigDecimal("1300");
        BigDecimal buyRate  = new BigDecimal("1200");
        BigDecimal usdRate  = new BigDecimal("1");

        FreightCalculator.applyCalculations(line, sellRate, buyRate, usdRate);

        assertThat(line.getSettleAmount()).isEqualByComparingTo("1000.00");
        assertThat(line.getExchangeRate()).isEqualByComparingTo("1300.000000");
        assertThat(line.getLocalAmount()).isEqualByComparingTo("1300000.00");
        assertThat(line.getUsdAmount()).isEqualByComparingTo("1000.00");
        assertThat(line.getSettleTaxAmount()).isEqualByComparingTo("100.00");
        assertThat(line.getLocalTaxAmount()).isEqualByComparingTo("130000.00");
    }
}
