package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — House B/L Container.
 * PRD §2.2: TEU = length_feet / 20
 */
@Entity
@Table(schema = "fms", name = "house_bl_container")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlContainerJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_container_id", updatable = false, nullable = false)
    private Long houseBlContainerId;

    @Column(name = "house_bl_id", nullable = false, insertable = false, updatable = false)
    private Long houseBlId;

    @Column(name = "container_no", length = 20)
    private String containerNo;

    @Column(name = "container_type", length = 10)
    @Enumerated(EnumType.STRING)
    private ContainerType containerType;

    @Column(name = "length_feet", nullable = false)
    private Integer lengthFeet;

    @Column(name = "seal_no_1", length = 30)
    private String sealNo1;

    @Column(name = "seal_no_2", length = 30)
    private String sealNo2;

    @Column(name = "seal_no_3", length = 30)
    private String sealNo3;

    @Column(name = "seal_no_4", length = 30)
    private String sealNo4;

    @Column(name = "seal_no_5", length = 30)
    private String sealNo5;

    @Column(name = "seal_no_6", length = 30)
    private String sealNo6;

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

    @Column(name = "vgm_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal vgmKg;

    @Column(name = "soc", nullable = false)
    private boolean isSoc = false;

    @Column(name = "seq", nullable = false)
    private int seq = 1;

    public static HouseBlContainerJpaEntity of(String containerNo,
                                               ContainerType containerType, int lengthFeet) {
        HouseBlContainerJpaEntity c = new HouseBlContainerJpaEntity();
        c.containerNo   = containerNo;
        c.containerType = containerType;
        c.lengthFeet    = lengthFeet;
        return c;
    }

    public void setHouseBlContainerId(Long v) { this.houseBlContainerId = v; }
    public void setContainerNo(String v) { this.containerNo = v; }
    public void setContainerType(ContainerType v) { this.containerType = v; }
    public void setLengthFeet(Integer v) { this.lengthFeet = v; }
    public void setSealNo1(String v) { this.sealNo1 = v; }
    public void setSealNo2(String v) { this.sealNo2 = v; }
    public void setSealNo3(String v) { this.sealNo3 = v; }
    public void setSealNo4(String v) { this.sealNo4 = v; }
    public void setSealNo5(String v) { this.sealNo5 = v; }
    public void setSealNo6(String v) { this.sealNo6 = v; }
    public void setPkgQty(Integer v) { this.pkgQty = v; }
    public void setPkgUnit(String v) { this.pkgUnit = v; }
    public void setGrossWeightKg(BigDecimal v) { this.grossWeightKg = v; }
    public void setNetWeightKg(BigDecimal v) { this.netWeightKg = v; }
    public void setCbm(BigDecimal v) { this.cbm = v; }
    public void setVgmKg(BigDecimal v) { this.vgmKg = v; }
    public void setIsSoc(boolean v) { this.isSoc = v; }
    public void setSeq(int v) { this.seq = v; }
}
