package com.freightos.fms.application.masterbl.projection;

import java.math.BigDecimal;

/**
 * Master B/L Dim(치수) 항목의 application-layer projection.
 * 도메인 엔티티 MasterBlDim을 Adapter(in) 경계에서 사용할 수 있도록 평탄화한다.
 */
public record DimProjection(
        BigDecimal lengthCm,
        BigDecimal widthCm,
        BigDecimal heightCm,
        Integer quantity,
        BigDecimal cbm,
        BigDecimal volumeWeightKg
) {}
