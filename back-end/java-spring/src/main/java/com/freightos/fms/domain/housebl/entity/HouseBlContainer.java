package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
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
    private String containerNo;
    private String containerType;      // 20GP / 40GP / 40HQ / RF / OT 등

    /** PRD §1.7: TEU 환산 기준. 20 / 40 / 45 정수. TEU = length_feet / 20 */
    private Integer lengthFeet;
    private String sealNo1;
    private String sealNo2;
    private Integer pkgQty;
    private String pkgUnit;
    private BigDecimal grossWeightKg;
    private BigDecimal netWeightKg;
    private BigDecimal cbm;

    /** SOLAS VGM (Verified Gross Mass) */
    private BigDecimal vgmKg;
    private boolean isSoc = false;     // Shipper's Own Container
    private int seq = 1;               // 정렬 순서 (Drag&Drop)

    public static HouseBlContainer of(HouseBl houseBl, String containerNo,
                                      String containerType, int lengthFeet) {
        HouseBlContainer c = new HouseBlContainer();
        c.houseBl       = houseBl;
        c.containerNo   = containerNo;
        c.containerType = containerType;
        c.lengthFeet    = lengthFeet;
        return c;
    }

    public static record Details(
            String sealNo1, String sealNo2, Integer pkgQty, String pkgUnit,
            BigDecimal grossWeightKg, BigDecimal netWeightKg, BigDecimal cbm,
            BigDecimal vgmKg, boolean isSoc, int seq) {}

    public void updateDetails(Details d) {
        this.sealNo1       = d.sealNo1();
        this.sealNo2       = d.sealNo2();
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
        if (lengthFeet == null) throw new IllegalStateException("lengthFeet is not set for container: " + containerNo);
        return BigDecimal.valueOf(lengthFeet).divide(BigDecimal.valueOf(20));
    }
}
