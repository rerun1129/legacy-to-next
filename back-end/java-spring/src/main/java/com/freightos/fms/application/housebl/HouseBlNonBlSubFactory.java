package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.common.enums.VolumeDivisor;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl.WorkDivision;
import com.freightos.common.util.Nullables;
import org.springframework.stereotype.Component;

/**
 * Non B/L 확장 필드 매핑 담당.
 * HouseBlFactory 크기 분리를 위해 NonBl 전용 apply 메서드를 이 클래스에 위임한다.
 */
@Component
class HouseBlNonBlSubFactory {

    void applyNonBlCreate(HouseBl entity, CreateHouseBlCommand cmd) {
        if (!(entity instanceof HouseBlNonBl nonBl)) return;
        nonBl.updateNonBlFields(BlNumber.of(cmd.originalBlRef()), Rton.of(cmd.rton()), Weight.of(cmd.volumeWeightKg()));
        nonBl.updateScheduleFields(cmd.linerCode(), cmd.linerName(), cmd.vesselName(), cmd.voyageNo(),
                cmd.finalDestCode(), cmd.finalDestName(), cmd.finalEta());
        nonBl.assignVolumeDivisor(Nullables.mapOrNull(cmd.volumeDivisor(), VolumeDivisor::valueOf));
        nonBl.updateRemark(cmd.remark());
    }

    void applyNonBlUpdate(HouseBl entity, UpdateHouseBlCommand cmd) {
        if (!(entity instanceof HouseBlNonBl nonBl)) return;
        if (cmd.workDivision() != null) nonBl.updateWorkDivision(WorkDivision.valueOf(cmd.workDivision()));
        nonBl.updateNonBlFields(BlNumber.of(cmd.originalBlRef()), Rton.of(cmd.rton()), Weight.of(cmd.volumeWeightKg()));
        nonBl.updateScheduleFields(cmd.linerCode(), cmd.linerName(), cmd.vesselName(), cmd.voyageNo(),
                cmd.finalDestCode(), cmd.finalDestName(), cmd.finalEta());
        nonBl.assignVolumeDivisor(Nullables.mapOrNull(cmd.volumeDivisor(), VolumeDivisor::valueOf));
        nonBl.updateRemark(cmd.remark());
    }
}
