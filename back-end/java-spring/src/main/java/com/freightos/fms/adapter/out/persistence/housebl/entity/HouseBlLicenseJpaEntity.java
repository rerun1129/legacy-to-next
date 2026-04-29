package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — House B/L 라이선스 / 패킹 명세 (E-17).
 */
@Entity
@Table(name = "house_bl_license")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlLicenseJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_license_id", updatable = false, nullable = false)
    private Long houseBlLicenseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false)
    private HouseBlJpaEntity houseBl;

    @Column(name = "license_no", length = 50)
    private String licenseNo;

    @Column(name = "pkg_qty")
    private Integer pkgQty;

    @Column(name = "pkg_unit", length = 10)
    private String pkgUnit;

    @Column(name = "gross_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal grossWeightKg;

    @Column(name = "combined_packing_mark", length = 50)
    private String combinedPackingMark;

    @Column(name = "combined_packing_qty")
    private Integer combinedPackingQty;

    @Column(name = "combined_packing_unit", length = 10)
    private String combinedPackingUnit;

    @Column(name = "partial_shipment", nullable = false)
    private boolean partialShipment = false;

    @Column(name = "partial_shipment_seq")
    private Integer partialShipmentSeq;

    @Column(name = "hsn_no", length = 30)
    private String hsnNo;

    @Column(name = "seq", nullable = false)
    private int seq;

    public void setHouseBlLicenseId(Long v) { this.houseBlLicenseId = v; }
    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setLicenseNo(String v) { this.licenseNo = v; }
    public void setPkgQty(Integer v) { this.pkgQty = v; }
    public void setPkgUnit(String v) { this.pkgUnit = v; }
    public void setGrossWeightKg(BigDecimal v) { this.grossWeightKg = v; }
    public void setCombinedPackingMark(String v) { this.combinedPackingMark = v; }
    public void setCombinedPackingQty(Integer v) { this.combinedPackingQty = v; }
    public void setCombinedPackingUnit(String v) { this.combinedPackingUnit = v; }
    public void setPartialShipment(boolean v) { this.partialShipment = v; }
    public void setPartialShipmentSeq(Integer v) { this.partialShipmentSeq = v; }
    public void setHsnNo(String v) { this.hsnNo = v; }
    public void setSeq(int v) { this.seq = v; }
}
