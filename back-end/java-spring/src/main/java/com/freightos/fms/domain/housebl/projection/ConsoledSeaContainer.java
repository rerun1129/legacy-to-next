package com.freightos.fms.domain.housebl.projection;

import java.math.BigDecimal;

/** Master B/L에 콘솔된 House B/L들의 SEA 컨테이너 row — join raw list (집계 없음). */
public record ConsoledSeaContainer(
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
