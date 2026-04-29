package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-12 House B/L 치수 (항공 전용 Dimension 그리드).
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlDim extends BaseEntity {

    private Long houseBlId;
    private Double lengthCm;
    private Double widthCm;
    private Double heightCm;
    private Integer quantity;
    private Double cbm;
    private Double volumeWeightKg;
    private int seq;

    public static HouseBlDim create(Long houseBlId, Double lengthCm, Double widthCm,
                                    Double heightCm, Integer quantity, Double cbm,
                                    Double volumeWeightKg, int seq) {
        HouseBlDim dim = new HouseBlDim();
        dim.houseBlId      = houseBlId;
        dim.lengthCm       = lengthCm;
        dim.widthCm        = widthCm;
        dim.heightCm       = heightCm;
        dim.quantity       = quantity;
        dim.cbm            = cbm;
        dim.volumeWeightKg = volumeWeightKg;
        dim.seq            = seq;
        return dim;
    }
}
