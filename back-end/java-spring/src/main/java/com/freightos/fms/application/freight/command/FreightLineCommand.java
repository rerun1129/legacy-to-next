package com.freightos.fms.application.freight.command;

import java.math.BigDecimal;

/**
 * 운임 라인 저장 커맨드 — 입력값만 포함, 계산값 미포함.
 */
public record FreightLineCommand(
    /** 매출/매입 구분 (name() 문자열) */
    String freightType,
    String freightCode,
    /** Per 기준 — Per 코드 또는 컨테이너 타입 코드 혼재(§6.6). */
    String per,
    BigDecimal unitQuantity,
    BigDecimal unitPrice,
    String currency,
    String customerCode,
    /** 세금 유형 (name() 문자열) */
    String taxType,
    /** 실적 인정 일자 (yyyyMMdd) */
    String performanceDt
) {}
