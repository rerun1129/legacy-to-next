package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HouseBlTruckSubFactoryTest {

    private HouseBlTruckSubFactory sut;

    @BeforeEach
    void setUp() {
        sut = new HouseBlTruckSubFactory();
    }

    // ── applyTruckCreate ──────────────────────────────────────────────

    @Test
    @DisplayName("applyTruckCreate: TruckDetailCommand null이면 truck 필드 미변경")
    void applyTruckCreate_nullCommand_noChange() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        sut.applyTruckCreate(truck, null);
        assertThat(truck.getTruckerCode()).isNull();
        assertThat(truck.getPickupDate()).isNull();
    }

    @Test
    @DisplayName("applyTruckCreate: 엔티티가 HouseBlTruck이 아니면 무시")
    void applyTruckCreate_nonTruckEntity_noChange() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        CreateHouseBlCommand.TruckDetailCommand cmd = createTruckDetailCommand();
        sut.applyTruckCreate(sea, cmd);
        // sea에는 truckerCode 없음 — 단순히 예외 없이 종료 확인
    }

    @Test
    @DisplayName("applyTruckCreate: truckerCode, pickupDate, voyageNo 정상 매핑")
    void applyTruckCreate_fieldsCorrectlyMapped() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        CreateHouseBlCommand.TruckDetailCommand cmd = createTruckDetailCommand();
        sut.applyTruckCreate(truck, cmd);
        assertThat(truck.getTruckerCode()).isNotNull();
        assertThat(truck.getTruckerCode().value()).isEqualTo("TRUCKER01");
        assertThat(truck.getPickupDate()).isNotNull();
        assertThat(truck.getVesselVoyage()).isNotNull();
        // PRD §S-06: vesselName은 항상 "TRUCK"으로 고정
        assertThat(truck.getVesselName()).isEqualTo("TRUCK");
        assertThat(truck.getVesselVoyage().voyageNo()).isEqualTo("V100");
    }

    @Test
    @DisplayName("applyTruckCreate: chargeWeightKg 정상 매핑")
    void applyTruckCreate_chargeWeightMapped() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        CreateHouseBlCommand.TruckDetailCommand cmd = createTruckDetailCommand();
        sut.applyTruckCreate(truck, cmd);
        assertThat(truck.getChargeWeightKg()).isNotNull();
        assertThat(truck.getChargeWeightKg().kg()).isEqualByComparingTo(BigDecimal.valueOf(500.0));
    }

    // ── applyTruckUpdate ──────────────────────────────────────────────

    @Test
    @DisplayName("applyTruckUpdate: TruckDetailCommand null이면 truck 필드 미변경")
    void applyTruckUpdate_nullCommand_noChange() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        sut.applyTruckUpdate(truck, null);
        assertThat(truck.getTruckerCode()).isNull();
    }

    @Test
    @DisplayName("applyTruckUpdate: 엔티티가 HouseBlTruck이 아니면 무시")
    void applyTruckUpdate_nonTruckEntity_noChange() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        UpdateHouseBlCommand.TruckDetailCommand cmd = createUpdateTruckDetailCommand();
        sut.applyTruckUpdate(sea, cmd);
        // 예외 없이 종료 확인
    }

    @Test
    @DisplayName("applyTruckUpdate: loadType, serviceTerm 정상 매핑")
    void applyTruckUpdate_loadTypeAndServiceTermMapped() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        UpdateHouseBlCommand.TruckDetailCommand cmd = createUpdateTruckDetailCommand();
        sut.applyTruckUpdate(truck, cmd);
        assertThat(truck.getLoadType()).isNotNull();
        assertThat(truck.getServiceTerm()).isNotNull();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────

    private CreateHouseBlCommand.TruckDetailCommand createTruckDetailCommand() {
        return new CreateHouseBlCommand.TruckDetailCommand(
                "TRUCKER01",              // truckerCode
                "EMP001",                 // truckerPic
                BigDecimal.valueOf(500.0), // chargeWeightKg
                "20250101",               // pickupDate
                "09:00",                  // pickupTm
                "10:00",                  // etdTm
                "18:00",                  // etaTm
                "LCL",                    // loadType
                "CY_CY",                  // serviceTerm
                "V100"                    // voyageNo
        );
    }

    private UpdateHouseBlCommand.TruckDetailCommand createUpdateTruckDetailCommand() {
        return new UpdateHouseBlCommand.TruckDetailCommand(
                "TRUCKER02",              // truckerCode
                "EMP002",                 // truckerPic
                BigDecimal.valueOf(600.0), // chargeWeightKg
                "20250201",               // pickupDate
                "10:00",                  // pickupTm
                "11:00",                  // etdTm
                "19:00",                  // etaTm
                "LCL",                    // loadType
                "CY_CY",                  // serviceTerm
                "V200"                    // voyageNo
        );
    }
}
