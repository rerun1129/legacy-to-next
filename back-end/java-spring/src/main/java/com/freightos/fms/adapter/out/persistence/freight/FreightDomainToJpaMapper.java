package com.freightos.fms.adapter.out.persistence.freight;

import com.freightos.fms.application.freight.command.FreightInputCommand;
import com.freightos.fms.application.freight.command.FreightLineCommand;
import com.freightos.fms.domain.freight.FreightCalculator;
import com.freightos.fms.domain.freight.FinancialDocTypePolicy;
import com.freightos.fms.domain.freight.FreightLine;
import com.freightos.fms.domain.freight.enums.FreightType;
import com.freightos.fms.domain.freight.enums.TaxType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Domain Command → JPA 엔티티 변환 매퍼.
 * 계산값(FreightCalculator) + 정책(FinancialDocTypePolicy) 적용 후 엔티티로 변환한다.
 */
@Component
public class FreightDomainToJpaMapper {

    /**
     * 헤더 커맨드를 기존(또는 신규) FreightHeaderJpaEntity에 적용한다.
     * blType·blId는 upsert 로직에서 별도 설정하므로 여기서는 다루지 않는다.
     */
    public void applyHeaderFields(FreightInputCommand cmd, FreightHeaderJpaEntity entity) {
        entity.setActualCustomerCode(cmd.actualCustomerCode());
        entity.setLinerCode(cmd.linerCode());
        entity.setSettlePartnerCode(cmd.settlePartnerCode());
        entity.setSellRateDt(cmd.sellRateDt());
        entity.setSellRateCurrencyCode(cmd.sellRateCurrencyCode());
        entity.setSellRate(cmd.sellRate());
        entity.setBuyRateDt(cmd.buyRateDt());
        entity.setBuyRateCurrencyCode(cmd.buyRateCurrencyCode());
        entity.setBuyRate(cmd.buyRate());
        entity.setUsdRateDt(cmd.usdRateDt());
        entity.setUsdRate(cmd.usdRate());
    }

    /**
     * 라인 커맨드 목록 → FreightLineJpaEntity 목록 변환.
     * customerTypes: customer_code → customer_type(PARTNER 여부 판정용).
     * 계산값과 FinancialDocType을 이 메서드에서 결정한다.
     */
    public List<FreightLineJpaEntity> buildLineEntities(
        List<FreightLineCommand> lineCmds,
        FreightInputCommand header,
        Map<String, String> customerTypes,
        FreightHeaderJpaEntity headerEntity
    ) {
        List<FreightLineJpaEntity> result = new ArrayList<>();
        for (FreightLineCommand cmd : lineCmds) {
            FreightLineJpaEntity entity = new FreightLineJpaEntity();
            entity.setFreightHeader(headerEntity);

            // 입력값 설정
            entity.setFreightType(cmd.freightType());
            entity.setFreightCode(cmd.freightCode());
            entity.setPer(cmd.per());
            entity.setUnitQuantity(cmd.unitQuantity());
            entity.setUnitPrice(cmd.unitPrice());
            entity.setCurrency(cmd.currency());
            entity.setCustomerCode(cmd.customerCode());
            entity.setTaxType(cmd.taxType());
            entity.setPerformanceDt(cmd.performanceDt());

            // 계산값 적용 (도메인 순수 함수 위임)
            FreightLine line = toFreightLine(cmd);
            FreightCalculator.applyCalculations(line, header.sellRate(), header.buyRate(), header.usdRate());

            entity.setExchangeRate(line.getExchangeRate());
            entity.setSettleAmount(line.getSettleAmount());
            entity.setLocalAmount(line.getLocalAmount());
            entity.setUsdExchangeRate(line.getUsdExchangeRate());
            entity.setUsdAmount(line.getUsdAmount());
            entity.setSettleTaxAmount(line.getSettleTaxAmount());
            entity.setLocalTaxAmount(line.getLocalTaxAmount());

            // FinancialDocType 정책 적용 (라인 customer_code 기준)
            String customerType = customerTypes.get(cmd.customerCode());
            FreightType freightType = FreightType.fromName(cmd.freightType());
            if (freightType != null) {
                entity.setFinancialDocType(FinancialDocTypePolicy.resolve(freightType, customerType).name());
            }

            // 단계B 필드: null 저장
            entity.setTaxNo(null);
            entity.setTaxDt(null);
            entity.setSlipNo(null);
            entity.setSlipDt(null);
            entity.setFinancialDocumentId(null);

            result.add(entity);
        }
        return result;
    }

    private FreightLine toFreightLine(FreightLineCommand cmd) {
        FreightLine line = new FreightLine();
        line.setFreightType(FreightType.fromName(cmd.freightType()));
        line.setUnitQuantity(cmd.unitQuantity());
        line.setUnitPrice(cmd.unitPrice());
        line.setTaxType(TaxType.fromName(cmd.taxType()));
        return line;
    }
}
