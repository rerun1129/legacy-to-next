package com.freightos.fms.domain.housebl.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * E-20 House B/L 트럭 확장.
 * PRD §S-06: Vessel/Voyage는 "TRUCK" 고정값으로 저장, 사용자 입력 불가.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlTruck extends HouseBl {

    /** 고정값 "TRUCK". 사용자 입력 없이 시스템 설정. */
    private String vesselName = "TRUCK";
    private LocalDate pickupDate;
    private String truckerCode;
    private String truckerPic;
    /** 트럭 전용: 청구중량 */
    private BigDecimal chargeWeightKg;
    private String incoterms;

    public static HouseBlTruck create() {
        HouseBlTruck entity = new HouseBlTruck();
        entity.vesselName = "TRUCK";
        return entity;
    }
}
