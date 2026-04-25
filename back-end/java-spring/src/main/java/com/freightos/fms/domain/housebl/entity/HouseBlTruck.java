package com.freightos.fms.domain.housebl.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * E-20 House B/L 트럭 확장.
 * house_bl + house_bl_truck JOIN.
 * PRD §S-06: Vessel/Voyage는 "TRUCK" 고정값으로 저장, 사용자 입력 불가.
 */
@Entity
@Table(name = "house_bl_truck")
@DiscriminatorValue("TRUCK")
@PrimaryKeyJoinColumn(name = "house_bl_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlTruck extends HouseBl {

    /** 고정값 "TRUCK". 사용자 입력 없이 시스템 설정. */
    @Column(name = "vessel_name", length = 10, nullable = false)
    private String vesselName = "TRUCK";

    @Column(name = "pickup_date")
    private LocalDate pickupDate;

    @Column(name = "trucker_code", length = 20)
    private String truckerCode;

    @Column(name = "trucker_pic", length = 100)
    private String truckerPic;

    /** 트럭 전용: 청구중량 */
    @Column(name = "charge_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal chargeWeightKg;

    @Column(name = "incoterms", length = 10)
    private String incoterms;

    public static HouseBlTruck create() {
        HouseBlTruck entity = new HouseBlTruck();
        entity.vesselName = "TRUCK";
        return entity;
    }
}
