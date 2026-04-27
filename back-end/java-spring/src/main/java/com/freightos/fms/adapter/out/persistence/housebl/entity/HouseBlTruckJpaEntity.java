package com.freightos.fms.adapter.out.persistence.housebl.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — House B/L 트럭 확장.
 * PRD §S-06: Vessel/Voyage는 "TRUCK" 고정값으로 저장.
 * @OneToOne 독립 엔티티로 HouseBlJpaEntity와 연관.
 */
@Entity
@Table(name = "house_bl_truck")
@Getter
@NoArgsConstructor
public class HouseBlTruckJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_truck_id", updatable = false, nullable = false)
    private Long houseBlTruckId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_bl_id", nullable = false, unique = true)
    private HouseBlJpaEntity houseBl;

    @Column(name = "vessel_name", length = 10, nullable = false)
    private String vesselName = "TRUCK";

    @Column(name = "pickup_date", length = 8)
    private String pickupDate;

    @Column(name = "trucker_code", length = 20)
    private String truckerCode;

    @Column(name = "trucker_pic", length = 100)
    private String truckerPic;

    @Column(name = "charge_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal chargeWeightKg;

    @Column(name = "incoterms", length = 10)
    private String incoterms;

    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setVesselName(String v) { this.vesselName = v; }
    public void setPickupDate(String v) { this.pickupDate = v; }
    public void setTruckerCode(String v) { this.truckerCode = v; }
    public void setTruckerPic(String v) { this.truckerPic = v; }
    public void setChargeWeightKg(BigDecimal v) { this.chargeWeightKg = v; }
    public void setIncoterms(String v) { this.incoterms = v; }
}
