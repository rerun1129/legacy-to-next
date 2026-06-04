package com.freightos.bms.application.financialdocument.port.out;

import java.math.BigDecimal;

/**
 * 서류 발행 시 운임 라인 정보를 서비스로 전달하는 내부 스냅샷.
 * 영속화 계층에서 로드한 라인의 핵심 필드만 포함한다.
 */
public record FreightLineSnapshot(
        Long freightLineId,
        Long freightHeaderId,
        String freightType,
        String financialDocType,
        String customerCode,
        BigDecimal settleAmount,
        BigDecimal localAmount,
        BigDecimal settleTaxAmount,
        BigDecimal localTaxAmount,
        BigDecimal usdAmount,
        Long financialDocumentId
) {}
