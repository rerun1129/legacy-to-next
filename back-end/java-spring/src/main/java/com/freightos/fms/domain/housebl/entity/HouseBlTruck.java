package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.VolumeDivisor;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.common.util.Nullables;
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

    private VesselVoyage vesselVoyage;
    private BlDate pickupDate;
    private String pickupTm;
    private String etdTm;
    private String etaTm;
    private LoadType loadType;
    private ServiceTerm serviceTerm;
    private CustomerCode truckerCode;
    private EmployeeCode truckerPic;
    /** 트럭 전용: 청구중량 */
    private Weight chargeWeightKg;
    private VolumeDivisor volumeDivisor;
    private String remark;

    protected HouseBlTruck(Bound bound) {
        super(JobDiv.TRUCK, bound);
    }

    public static HouseBlTruck create(Bound bound) {
        HouseBlTruck entity = new HouseBlTruck(bound);
        entity.vesselVoyage = VesselVoyage.of(null, "TRUCK", null);
        return entity;
    }

    public String getVesselName() {
        return Nullables.mapOrElse(vesselVoyage, VesselVoyage::vesselName, () -> "TRUCK");
    }

    public void assignVolumeDivisor(VolumeDivisor v) { this.volumeDivisor = v; }

    public void updateRemark(String remark) { this.remark = remark; }

    public record TruckFields(
            VesselVoyage vesselVoyage,
            BlDate pickupDate, String pickupTm,
            String etdTm, String etaTm,
            LoadType loadType, ServiceTerm serviceTerm,
            CustomerCode truckerCode, EmployeeCode truckerPic,
            Weight chargeWeightKg) {}

    public void updateTruckFields(TruckFields f) {
        this.vesselVoyage   = VesselVoyage.of(null, "TRUCK",
                Nullables.mapOrNull(f.vesselVoyage, VesselVoyage::voyageNo));
        this.pickupDate     = f.pickupDate;
        this.pickupTm       = f.pickupTm;
        this.etdTm          = f.etdTm;
        this.etaTm          = f.etaTm;
        this.loadType       = f.loadType;
        this.serviceTerm    = f.serviceTerm;
        this.truckerCode    = f.truckerCode;
        this.truckerPic     = f.truckerPic;
        this.chargeWeightKg = f.chargeWeightKg;
    }
}
