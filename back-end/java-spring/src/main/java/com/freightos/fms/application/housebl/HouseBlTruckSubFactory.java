package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.EmployeeCode;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.common.util.Nullables;
import org.springframework.stereotype.Component;

/**
 * Truck 전용 퍼포먼스 패널 필드 매핑 담당.
 * HouseBlFactory 크기 분리를 위해 Truck 전용 apply 메서드를 이 클래스에 위임한다.
 */
@Component
class HouseBlTruckSubFactory {

    void applyTruckCreate(HouseBl entity, CreateHouseBlCommand.TruckDetailCommand t) {
        if (t == null || !(entity instanceof HouseBlTruck truck)) return;
        truck.updateTruckFields(new HouseBlTruck.TruckFields(
                VesselVoyage.of(null, "TRUCK", t.voyageNo()),
                BlDate.of(t.pickupDate()), t.pickupTm(),
                t.etdTm(), t.etaTm(),
                Nullables.mapOrNull(t.loadType(), LoadType::valueOf),
                Nullables.mapOrNull(t.serviceTerm(), ServiceTerm::valueOf),
                CustomerCode.of(t.truckerCode()),
                EmployeeCode.of(t.truckerPic()),
                Weight.of(t.chargeWeightKg())));
    }

    void applyTruckUpdate(HouseBl entity, UpdateHouseBlCommand.TruckDetailCommand t) {
        if (t == null || !(entity instanceof HouseBlTruck truck)) return;
        truck.updateTruckFields(new HouseBlTruck.TruckFields(
                VesselVoyage.of(null, "TRUCK", t.voyageNo()),
                BlDate.of(t.pickupDate()), t.pickupTm(),
                t.etdTm(), t.etaTm(),
                Nullables.mapOrNull(t.loadType(), LoadType::valueOf),
                Nullables.mapOrNull(t.serviceTerm(), ServiceTerm::valueOf),
                CustomerCode.of(t.truckerCode()),
                EmployeeCode.of(t.truckerPic()),
                Weight.of(t.chargeWeightKg())));
    }
}
