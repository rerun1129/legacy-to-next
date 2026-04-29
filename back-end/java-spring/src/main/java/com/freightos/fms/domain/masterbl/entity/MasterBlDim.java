package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.common.vo.Quantity;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * E-05 Master B/L 치수 (Dimension 그리드 행).
 * 항공 Master B/L에서만 사용되는 부피 실측 내역.
 * MasterBl과 1:N 관계 — 각 행이 하나의 Dimension 레코드.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlDim extends BaseEntity {

    private Long masterBlId;

    // 실측 치수 (단위: cm 또는 inch — divisor 선택으로 구분)
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Quantity qty;
    private Volume cbm;
    private Weight volumeWt;

    // IATA 볼륨중량 계산 제수 (예: "CM/6000", "INCH/366")
    private String divisor;

    private Integer seq;

    private MasterBlDim(Long masterBlId) {
        this.masterBlId = masterBlId;
    }

    public static MasterBlDim create(Long masterBlId) {
        return new MasterBlDim(masterBlId);
    }

    public void updateFields(BigDecimal length, BigDecimal width, BigDecimal height,
                             Quantity qty, Volume cbm, Weight volumeWt,
                             String divisor, Integer seq) {
        this.length    = length;
        this.width     = width;
        this.height    = height;
        this.qty       = qty;
        this.cbm       = cbm;
        this.volumeWt  = volumeWt;
        this.divisor   = divisor;
        this.seq       = seq;
    }
}
