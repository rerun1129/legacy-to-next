package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import org.springframework.stereotype.Component;

/**
 * AIR 전용 로직.
 * Phase 4 Sea/Air House Entry 마이그레이션 시 AIR 필드 매핑 완성 예정.
 * 이번 이슈(12)에서는 remark 매핑만 포함한다 — 다른 AIR 필드는 Phase 4에서 채울 예정.
 */
@Component
class HouseBlAirSubFactory {

    void applyAirCreate(HouseBl entity, CreateHouseBlCommand cmd) {
        if (!(entity instanceof HouseBlAir air)) return;
        air.updateRemark(cmd.remark());
    }

    void applyAirUpdate(HouseBl entity, UpdateHouseBlCommand cmd) {
        if (!(entity instanceof HouseBlAir air)) return;
        air.updateRemark(cmd.remark());
    }
}
