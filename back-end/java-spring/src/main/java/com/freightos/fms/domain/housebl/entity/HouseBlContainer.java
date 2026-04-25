package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * E-14b House B/L ↔ Container 배정.
 * house_bl_container (junction + cargo 데이터).
 * PRD §2.2: TEU = length_feet / 20 (산식 기반, 별도 factor 테이블 없음)
 */
@Entity
@Table(name = "house_bl_container")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlContainer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false)
    private HouseBl houseBl;

    @Column(name = "container_no", nullable = false, length = 20)
    private String containerNo;

    @Column(name = "container_type", length = 10)
    private String containerType;      // 20GP / 40GP / 40HQ / RF / OT 등

    /** PRD §1.7: TEU 환산 기준. 20 / 40 / 45 정수. TEU = length_feet / 20 */
    @Column(name = "length_feet", nullable = false)
    private Integer lengthFeet;

    @Column(name = "seal_no_1", length = 30)
    private String sealNo1;

    @Column(name = "seal_no_2", length = 30)
    private String sealNo2;

    @Column(name = "pkg_qty")
    private Integer pkgQty;

    @Column(name = "pkg_unit", length = 10)
    private String pkgUnit;

    @Column(name = "gross_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal grossWeightKg;

    @Column(name = "net_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal netWeightKg;

    @Column(name = "cbm", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal cbm;

    /** SOLAS VGM (Verified Gross Mass) */
    @Column(name = "vgm_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal vgmKg;

    @Column(name = "soc", nullable = false)
    private boolean soc = false;       // Shipper's Own Container

    @Column(name = "seq", nullable = false)
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

    /** TEU 환산 산식: length_feet / 20 */
    public BigDecimal teu() {
        return BigDecimal.valueOf(lengthFeet).divide(BigDecimal.valueOf(20));
    }
}
