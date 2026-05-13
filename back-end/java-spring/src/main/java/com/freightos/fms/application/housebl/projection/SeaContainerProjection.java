package com.freightos.fms.application.housebl.projection;

import java.math.BigDecimal;

/**
 * SEA 컨테이너(house_bl_sea_container) application-layer projection.
 * enum/VO는 String/primitive로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 */
public record SeaContainerProjection(
        Long id,
        String containerNo,
        String containerType,
        Integer lengthFeet,
        String sealNo1,
        String sealNo2,
        String sealNo3,
        String sealNo4,
        String sealNo5,
        String sealNo6,
        Integer pkgQty,
        String pkgUnit,
        BigDecimal grossWeightKg,
        BigDecimal netWeightKg,
        BigDecimal cbm,
        BigDecimal vgmKg,
        boolean soc,
        int seq
) {}
