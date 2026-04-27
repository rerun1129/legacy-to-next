package com.freightos.fms.adapter.out.persistence.housebl.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA ORM 엔티티 — House B/L 트럭 확장.
 * PRD §S-06: Vessel/Voyage는 "TRUCK" 고정값으로 저장.
 */
@Entity
@Table(name = "house_bl_truck")
@DiscriminatorValue("TRUCK")
@PrimaryKeyJoinColumn(name = "house_bl_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlTruckJpaEntity extends HouseBlJpaEntity {

    @Column(name = "vessel_name", length = 10, nullable = false)
    private String vesselName = "TRUCK";

    @Column(name = "pickup_date")
    private LocalDate pickupDate;

    @Column(name = "trucker_code", length = 20)
    private String truckerCode;

    @Column(name = "trucker_pic", length = 100)
    private String truckerPic;

    @Column(name = "charge_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal chargeWeightKg;

    @Column(name = "incoterms", length = 10)
    private String incoterms;

    public void setVesselName(String v) { this.vesselName = v; }
    public void setPickupDate(LocalDate v) { this.pickupDate = v; }
    public void setTruckerCode(String v) { this.truckerCode = v; }
    public void setTruckerPic(String v) { this.truckerPic = v; }
    public void setChargeWeightKg(BigDecimal v) { this.chargeWeightKg = v; }
    public void setIncoterms(String v) { this.incoterms = v; }
}
