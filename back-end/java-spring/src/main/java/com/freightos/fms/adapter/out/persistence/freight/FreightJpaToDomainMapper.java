package com.freightos.fms.adapter.out.persistence.freight;

import com.freightos.fms.application.freight.FreightLineView;
import com.freightos.fms.application.freight.FreightView;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JPA 엔티티 → FreightView(application VO) 변환 매퍼.
 * 도메인 엔티티를 경유하지 않고 JPA → VO 직접 변환.
 */
@Component
public class FreightJpaToDomainMapper {

    public FreightView toFreightView(FreightHeaderJpaEntity header) {
        List<FreightLineView> lineViews = header.getLines().stream()
            .map(this::toLineView)
            .toList();

        return new FreightView(
            header.getFreightHeaderId(),
            header.getBlType(),
            header.getBlId(),
            header.getActualCustomerCode(),
            header.getLinerCode(),
            header.getSettlePartnerCode(),
            header.getSellRateDt(),
            header.getSellRateCurrencyCode(),
            header.getSellRate(),
            header.getBuyRateDt(),
            header.getBuyRateCurrencyCode(),
            header.getBuyRate(),
            header.getUsdRateDt(),
            header.getUsdRate(),
            lineViews
        );
    }

    private FreightLineView toLineView(FreightLineJpaEntity line) {
        return new FreightLineView(
            line.getFreightLineId(),
            line.getFreightType(),
            line.getFreightCode(),
            line.getPer(),
            line.getUnitQuantity(),
            line.getUnitPrice(),
            line.getCurrency(),
            line.getCustomerCode(),
            line.getTaxType(),
            line.getPerformanceDt(),
            line.getFinancialDocType(),
            line.getExchangeRate(),
            line.getSettleAmount(),
            line.getLocalAmount(),
            line.getSettleTaxAmount(),
            line.getLocalTaxAmount(),
            line.getUsdExchangeRate(),
            line.getUsdAmount(),
            line.getTaxNo(),
            line.getTaxDt(),
            line.getSlipNo(),
            line.getSlipDt(),
            line.getFinancialDocumentId(),
            // TODO(단계E): financial_document_id → document_no 조인 — FinancialDocumentJpaEntity 도입 후 연결
            null
        );
    }
}
