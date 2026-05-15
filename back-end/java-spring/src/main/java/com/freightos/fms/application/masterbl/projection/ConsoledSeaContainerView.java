package com.freightos.fms.application.masterbl.projection;

import java.math.BigDecimal;

/**
 * application 경계에서 domain projection(ConsoledSeaContainer)을 대체하는 view record.
 * Master B/L 상세 응답에 포함되는 콘솔 House B/L SEA 컨테이너 목록용.
 */
public record ConsoledSeaContainerView(
        Long houseBlId,
        String containerNo,
        String containerType,
        String sealNo1,
        String sealNo2,
        String sealNo3,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal cbm,
        BigDecimal vgmKg
) {}
