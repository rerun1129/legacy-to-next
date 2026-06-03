package com.freightos.fms.adapter.out.persistence.freight;

import com.freightos.fms.application.freight.command.FreightInputCommand;
import com.freightos.fms.application.freight.command.FreightLineCommand;
import com.freightos.fms.domain.freight.FinancialDocTypePolicy;
import com.freightos.fms.domain.freight.enums.FinancialDocType;
import com.freightos.fms.domain.freight.enums.FreightType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Domain Command → JPA 엔티티 변환 매퍼.
 * FE가 실시간 계산한 값을 그대로 저장한다(BE 재계산 없음).
 * financialDocType은 FE 전달값 검증 후 사용, 미전달 시 FinancialDocTypePolicy 폴백.
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
     * customerTypes: customer_code → customer_type(PARTNER 여부 판정 — financialDocType 폴백용).
     * FE 계산값을 그대로 저장하며, financialDocType만 검증·폴백 처리한다.
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

            // FE 계산값 그대로 저장 (BE 재계산 없음)
            entity.setExchangeRate(cmd.exchangeRate());
            entity.setSettleAmount(cmd.settleAmount());
            entity.setLocalAmount(cmd.localAmount());
            entity.setUsdExchangeRate(cmd.usdExchangeRate());
            entity.setUsdAmount(cmd.usdAmount());
            entity.setLocalTaxAmount(cmd.localTaxAmount());
            // settleTaxAmount: 미사용 — null 저장
            entity.setSettleTaxAmount(null);

            // financialDocType: FE 전달값 검증 → 없으면 정책 폴백
            entity.setFinancialDocType(resolveFinancialDocType(cmd, customerTypes));

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

    /**
     * financialDocType 결정.
     * cmd.financialDocType()이 non-blank이면 FreightType 제약 검증 후 사용.
     * blank/null이면 FinancialDocTypePolicy 폴백.
     *
     * 검증 규칙(§6.16):
     *   SELLING → {INVOICE, DEBIT},  BUYING → {PAYMENT, CREDIT}
     */
    private String resolveFinancialDocType(FreightLineCommand cmd, Map<String, String> customerTypes) {
        FreightType freightType = FreightType.fromName(cmd.freightType());

        if (cmd.financialDocType() != null && !cmd.financialDocType().isBlank()) {
            FinancialDocType docType = FinancialDocType.fromName(cmd.financialDocType());
            validateFinancialDocTypeConstraint(freightType, docType, cmd.financialDocType());
            return docType.name();
        }

        // 폴백: FinancialDocTypePolicy (freightType이 인식 불가면 null 저장)
        if (freightType == null) return null;
        String customerType = customerTypes.get(cmd.customerCode());
        return FinancialDocTypePolicy.resolve(freightType, customerType).name();
    }

    /**
     * SELLING이면 INVOICE·DEBIT만, BUYING이면 PAYMENT·CREDIT만 허용.
     * freightType이 null이면 검증 생략(upstream 밸리데이션에서 이미 처리).
     */
    private static void validateFinancialDocTypeConstraint(
            FreightType freightType, FinancialDocType docType, String rawDocType) {
        if (freightType == null) return;
        boolean valid = switch (freightType) {
            case SELLING -> docType == FinancialDocType.INVOICE || docType == FinancialDocType.DEBIT;
            case BUYING  -> docType == FinancialDocType.PAYMENT || docType == FinancialDocType.CREDIT;
        };
        if (!valid) {
            throw new IllegalArgumentException(
                    "financialDocType '" + rawDocType + "'은(는) freightType '" + freightType.name() + "'에 허용되지 않습니다. "
                    + "SELLING: INVOICE·DEBIT, BUYING: PAYMENT·CREDIT");
        }
    }
}
