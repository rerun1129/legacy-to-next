package com.freightos.fms.application.masterbl.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Master B/L 단건 조회·수정 결과 Projection.
 * UseCase(Application) → Adapter(in) 경계를 넘을 때 domain entity 대신 이 record를 반환한다.
 * application 경계에서 enum은 String으로 통일됨.
 */
public record MasterBlDetailResult(
        Long id,
        String mblNo,
        String masterRefNo,
        String jobDiv,
        String bound,
        String shipmentType,
        String shipperCode,
        String consigneeCode,
        String notifyCode,
        String polCode,
        String podCode,
        String etd,
        String eta,
        String freightTerm,
        String operatorCode,
        String teamCode,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ConsoledHouseBlSummaryView> consolidatedHouseBls
) {}
