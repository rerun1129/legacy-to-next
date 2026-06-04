package com.freightos.fms.adapter.in.web.freight.dto;

import com.freightos.fms.application.freight.FreightLineView;
import com.freightos.fms.application.freight.FreightView;

import java.math.BigDecimal;
import java.util.List;

/** Freight 탭 응답 DTO — 4개 B/L 도메인(House/Master/Truck/Non)이 공용으로 참조한다. */
public record FreightResponse(
        // 환율 헤더
        String sellRateDt,
        String sellRateCurrencyCode,
        BigDecimal sellRate,
        String buyRateDt,
        String buyRateCurrencyCode,
        BigDecimal buyRate,
        String usdRateDt,
        BigDecimal usdRate,
        // 라인
        List<FreightLineResponse> selling,
        List<FreightLineResponse> buying
) {
    public static FreightResponse from(FreightView v) {
        List<FreightLineResponse> selling = v.lines().stream()
                .filter(l -> "SELLING".equals(l.freightType()))
                .map(FreightLineResponse::from)
                .toList();
        List<FreightLineResponse> buying = v.lines().stream()
                .filter(l -> "BUYING".equals(l.freightType()))
                .map(FreightLineResponse::from)
                .toList();
        return new FreightResponse(
                v.sellRateDt(), v.sellRateCurrencyCode(), v.sellRate(),
                v.buyRateDt(), v.buyRateCurrencyCode(), v.buyRate(),
                v.usdRateDt(), v.usdRate(),
                selling, buying
        );
    }

    /** Freight 라인 1행 응답 DTO. */
    public record FreightLineResponse(
            Long id,
            String freightCode,
            String freightName,
            String per,
            BigDecimal qty,
            BigDecimal price,
            String currency,
            String customerCode,
            String customerName,
            String taxType,
            String performanceDt,
            // 계산값
            String financialDocType,
            BigDecimal exchangeRate,
            BigDecimal settleAmount,
            BigDecimal localAmount,
            BigDecimal settleTaxAmount,
            BigDecimal localTaxAmount,
            BigDecimal usdExchangeRate,
            BigDecimal usdAmount,
            String financialDocumentNo,
            // BMS amend 편집 진입용 — 발행 서류 PK
            Long financialDocumentId
    ) {
        public static FreightLineResponse from(FreightLineView l) {
            return new FreightLineResponse(
                    l.freightLineId(),
                    l.freightCode(),
                    l.freightName(),
                    l.per(),
                    l.unitQuantity(),
                    l.unitPrice(),
                    l.currency(),
                    l.customerCode(),
                    l.customerName(),
                    l.taxType(),
                    l.performanceDt(),
                    l.financialDocType(),
                    l.exchangeRate(),
                    l.settleAmount(),
                    l.localAmount(),
                    l.settleTaxAmount(),
                    l.localTaxAmount(),
                    l.usdExchangeRate(),
                    l.usdAmount(),
                    l.financialDocumentNo(),
                    l.financialDocumentId()
            );
        }
    }
}
