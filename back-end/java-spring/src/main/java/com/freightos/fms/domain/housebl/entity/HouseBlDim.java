package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * E-12 House B/L 포장 치수 명세.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlDim extends BaseEntity {

    private Long houseBlDimId;
    private Long houseBlId;
    private BigDecimal lengthCm;
    private BigDecimal widthCm;
    private BigDecimal heightCm;
    private Integer quantity;
    private BigDecimal cbm;
    private BigDecimal volumeWeightKg;
    private int seq;

    public static HouseBlDim create(Long houseBlId, BigDecimal lengthCm, BigDecimal widthCm,
                                    BigDecimal heightCm, Integer quantity, BigDecimal cbm,
                                    BigDecimal volumeWeightKg, int seq) {
        HouseBlDim d = new HouseBlDim();
        d.houseBlId      = houseBlId;
        d.lengthCm       = lengthCm;
        d.widthCm        = widthCm;
        d.heightCm       = heightCm;
        d.quantity       = quantity;
        d.cbm            = cbm;
        d.volumeWeightKg = volumeWeightKg;
        d.seq            = seq;
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
