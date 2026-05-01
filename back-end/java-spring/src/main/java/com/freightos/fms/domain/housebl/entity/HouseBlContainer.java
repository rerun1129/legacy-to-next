package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * E-14b House B/L ↔ Container 배정.
 * PRD §2.2: TEU = length_feet / 20 (산식 기반, 별도 factor 테이블 없음)
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlContainer extends BaseEntity {

    private HouseBl houseBl;
    private ContainerNumber containerNo;
    private ContainerType containerType;

    /** PRD §1.7: TEU 환산 기준. 20 / 40 / 45 정수. TEU = length_feet / 20 */
    private Integer lengthFeet;
    private SealNumber sealNo1;
    private SealNumber sealNo2;
    private SealNumber sealNo3;
    private SealNumber sealNo4;
    private SealNumber sealNo5;
    private SealNumber sealNo6;
    private Quantity pkgQty;
    private WeightUnit pkgUnit;
    private Weight grossWeightKg;
    private Weight netWeightKg;
    private Volume cbm;

    /** SOLAS VGM (Verified Gross Mass) */
    private Weight vgmKg;
    private boolean isSoc = false;      // Shipper's Own Container
    private int seq = 1;                // 정렬 순서 (Drag&Drop)

    public static HouseBlContainer of(HouseBl houseBl, ContainerNumber containerNo,
                                      ContainerType containerType, int lengthFeet) {
        HouseBlContainer c = new HouseBlContainer();
        c.houseBl       = houseBl;
        c.containerNo   = containerNo;
        c.containerType = containerType;
        c.lengthFeet    = lengthFeet;
        return c;
    }

    public static record Details(
            SealNumber sealNo1, SealNumber sealNo2, SealNumber sealNo3, SealNumber sealNo4,
            SealNumber sealNo5, SealNumber sealNo6, Quantity pkgQty, WeightUnit pkgUnit,
            Weight grossWeightKg, Weight netWeightKg, Volume cbm,
            Weight vgmKg, boolean isSoc, int seq) {}

    public void updateDetails(Details d) {
        this.sealNo1       = d.sealNo1();
        this.sealNo2       = d.sealNo2();
        this.sealNo3       = d.sealNo3();
        this.sealNo4       = d.sealNo4();
        this.sealNo5       = d.sealNo5();
        this.sealNo6       = d.sealNo6();
        this.pkgQty        = d.pkgQty();
        this.pkgUnit       = d.pkgUnit();
        this.grossWeightKg = d.grossWeightKg();
        this.netWeightKg   = d.netWeightKg();
        this.cbm           = d.cbm();
        this.vgmKg         = d.vgmKg();
        this.isSoc         = d.isSoc();
        this.seq           = d.seq();
    }

    /** TEU 환산 산식: length_feet / 20 */
    public BigDecimal teu() {
        if (lengthFeet == null) throw new IllegalStateException("lengthFeet is not set for container: " + (containerNo != null ? containerNo.value() : "null"));
        return BigDecimal.valueOf(lengthFeet).divide(BigDecimal.valueOf(20));
    }
}
