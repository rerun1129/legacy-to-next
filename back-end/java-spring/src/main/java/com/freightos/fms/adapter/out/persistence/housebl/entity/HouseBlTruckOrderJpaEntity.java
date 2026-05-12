package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.TruckType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — House B/L Truck Order 그리드 행.
 * TRUCK 모드 전용 1:N 자식. truck_type은 TruckType.code 문자열로 저장.
 */
@Entity
@Table(schema = "fms", name = "house_bl_truck_order")
@Getter
@NoArgsConstructor
public class HouseBlTruckOrderJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_truck_order_id", updatable = false, nullable = false)
    private Long houseBlTruckOrderId;

    @Column(name = "house_bl_truck_id", nullable = false, insertable = false, updatable = false)
    private Long houseBlTruckId;

    @Column(name = "truck_order_no", length = 30)
    private String truckOrderNo;

    @Column(name = "pkg_qty")
    private Integer pkgQty;

    @Column(name = "pkg_unit", length = 10)
    private String pkgUnit;

    @Column(name = "gross_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal grossWeightKg;

    @Column(name = "cbm", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal cbm;

    @Column(name = "truck_no", length = 20)
    private String truckNo;

    @Column(name = "truck_type", length = 10)
    @Enumerated(EnumType.STRING)
    private TruckType truckType;

    @Column(name = "driver", length = 50)
    private String driver;

    @Column(name = "mobile_no", length = 30)
    private String mobileNo;

    @Column(name = "container_no", length = 20)
    private String containerNo;

    @Column(name = "container_type", length = 10)
    @Enumerated(EnumType.STRING)
    private ContainerType containerType;

    @Column(name = "seal_no_1", length = 30)
    private String sealNo1;

    @Column(name = "seal_no_2", length = 30)
    private String sealNo2;

    @Column(name = "seal_no_3", length = 30)
    private String sealNo3;

    public void setHouseBlTruckOrderId(Long v)      { this.houseBlTruckOrderId = v; }
    public void setHouseBlTruckId(Long v)           { this.houseBlTruckId = v; }
    public void setTruckOrderNo(String v)          { this.truckOrderNo = v; }
    public void setPkgQty(Integer v)               { this.pkgQty       = v; }
    public void setPkgUnit(String v)               { this.pkgUnit      = v; }
    public void setGrossWeightKg(BigDecimal v)     { this.grossWeightKg = v; }
    public void setCbm(BigDecimal v)               { this.cbm          = v; }
    public void setTruckNo(String v)               { this.truckNo      = v; }
    public void setTruckType(TruckType v)           { this.truckType    = v; }
    public void setDriver(String v)                { this.driver       = v; }
    public void setMobileNo(String v)              { this.mobileNo     = v; }
    public void setContainerNo(String v)           { this.containerNo  = v; }
    public void setContainerType(ContainerType v)  { this.containerType = v; }
    public void setSealNo1(String v)               { this.sealNo1      = v; }
    public void setSealNo2(String v)               { this.sealNo2      = v; }
    public void setSealNo3(String v)               { this.sealNo3      = v; }
}
