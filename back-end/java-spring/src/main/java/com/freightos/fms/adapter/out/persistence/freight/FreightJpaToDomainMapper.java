package com.freightos.fms.adapter.out.persistence.freight;

import com.freightos.fms.application.freight.FreightLineView;
import com.freightos.fms.application.freight.FreightView;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * JPA 엔티티 → FreightView(application VO) 변환 매퍼.
 * 도메인 엔티티를 경유하지 않고 JPA → VO 직접 변환.
 */
@Component
public class FreightJpaToDomainMapper {

    /**
     * @param documentNoMap financial_document_id → document_no (빈 맵 허용)
     */
    public FreightView toFreightView(
            FreightHeaderJpaEntity header,
            Map<String, String> customerNames,
            Map<String, String> freightNames,
            Map<Long, String> documentNoMap) {
        List<FreightLineView> lineViews = header.getLines().stream()
            .map(line -> toLineView(line, customerNames, freightNames, documentNoMap))
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

    private FreightLineView toLineView(
            FreightLineJpaEntity line,
            Map<String, String> customerNames,
            Map<String, String> freightNames,
            Map<Long, String> documentNoMap) {
        String customerCode = line.getCustomerCode();
        String freightCode = line.getFreightCode();
        Long docId = line.getFinancialDocumentId();
        String documentNo = docId != null ? documentNoMap.getOrDefault(docId, null) : null;
        return new FreightLineView(
            line.getFreightLineId(),
            line.getFreightType(),
            freightCode,
            line.getPer(),
            line.getUnitQuantity(),
            line.getUnitPrice(),
            line.getCurrency(),
            customerCode,
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
            docId,
            documentNo,
            customerCode != null ? customerNames.getOrDefault(customerCode, "") : "",
            freightCode != null ? freightNames.getOrDefault(freightCode, "") : ""
        );
    }
}
