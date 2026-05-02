package com.freightos.fms.domain.masterbl.entity;

import com.freightos.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * E-05 Master B/L Dim (치수) 항목.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlDim extends BaseEntity {

    private Long masterBlDimId;
    private Long masterBlId;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private Integer quantity;
    private BigDecimal cbm;
    private BigDecimal volumeWeightKg;

    public static MasterBlDim create(Long masterBlId, BigDecimal lengthCm, BigDecimal widthCm,
                                     BigDecimal heightCm, Integer quantity, BigDecimal cbm,
                                     BigDecimal volumeWeightKg) {
        MasterBlDim d = new MasterBlDim();
        d.masterBlId     = masterBlId;
        d.lengthCm       = lengthCm;
        d.widthCm        = widthCm;
        d.heightCm       = heightCm;
        d.quantity       = quantity;
        d.cbm            = cbm;
        d.volumeWeightKg = volumeWeightKg;
        return d;
    }

    public void updateDetails(BigDecimal lengthCm, BigDecimal widthCm, BigDecimal heightCm,
                              Integer quantity, BigDecimal cbm, BigDecimal volumeWeightKg) {
        this.lengthCm       = lengthCm;
        this.widthCm        = widthCm;
        this.heightCm       = heightCm;
        this.quantity       = quantity;
        this.cbm            = cbm;
        this.volumeWeightKg = volumeWeightKg;
    }
}
