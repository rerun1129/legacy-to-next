package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    // 비즈니스 날짜
    private BlDate pickupDate;
    private CustomerCode truckerCode;
    private EmployeeCode truckerPic;
    /** 트럭 전용: 청구중량 */
    private Weight chargeWeightKg;

    protected HouseBlTruck(Bound bound) {
        super(JobDiv.TRUCK, bound);
    }

    public static HouseBlTruck create(Bound bound) {
        HouseBlTruck entity = new HouseBlTruck(bound);
        entity.vesselName = "TRUCK";
        return entity;
    }

    public void updateTruckFields(BlDate pickupDate, CustomerCode truckerCode, EmployeeCode truckerPic,
                                  Weight chargeWeightKg) {
        this.pickupDate     = pickupDate;
        this.truckerCode    = truckerCode;
        this.truckerPic     = truckerPic;
        this.chargeWeightKg = chargeWeightKg;
    }
}
