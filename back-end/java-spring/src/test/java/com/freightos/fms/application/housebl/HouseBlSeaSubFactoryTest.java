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

class HouseBlSeaSubFactoryTest {

    private HouseBlSeaSubFactory sut;

    @BeforeEach
    void setUp() {
        sut = new HouseBlSeaSubFactory();
    }

    // ── applySeaCreate ────────────────────────────────────────────────

    @Test
    @DisplayName("applySeaCreate: SeaDetailCommand null이면 sea 필드 미변경")
    void applySeaCreate_nullCommand_noChange() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sut.applySeaCreate(sea, null);
        assertThat(sea.getLinerCode()).isNull();
        assertThat(sea.getVesselVoyage()).isNull();
    }

    @Test
    @DisplayName("applySeaCreate: 엔티티가 HouseBlSea가 아니면 무시")
    void applySeaCreate_nonSeaEntity_noChange() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        CreateHouseBlCommand.SeaDetailCommand cmd = createSeaDetailCommand();
        sut.applySeaCreate(truck, cmd);
        // truck에는 linerCode 없음 — 단순히 예외 없이 종료 확인
    }

    @Test
    @DisplayName("applySeaCreate: linerCode, vesselVoyage, onboardDate 정상 매핑")
    void applySeaCreate_scheduleFields_mapped() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        CreateHouseBlCommand.SeaDetailCommand cmd = createSeaDetailCommand();
        sut.applySeaCreate(sea, cmd);
        assertThat(sea.getLinerCode()).isNotNull();
        assertThat(sea.getLinerCode().value()).isEqualTo("LINER01");
        assertThat(sea.getVesselVoyage()).isNotNull();
        assertThat(sea.getVesselVoyage().vesselName()).isEqualTo("EVER GREEN");
        assertThat(sea.getVesselVoyage().voyageNo()).isEqualTo("V001");
    }

    @Test
    @DisplayName("applySeaCreate: blType, vesselNationality 정상 매핑")
    void applySeaCreate_blTypeAndNationality_mapped() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        CreateHouseBlCommand.SeaDetailCommand cmd = createSeaDetailCommand();
        sut.applySeaCreate(sea, cmd);
        assertThat(sea.getBlType()).isNotNull();
        assertThat(sea.getVesselNationality()).isEqualTo("KR");
    }

    @Test
    @DisplayName("applySeaCreate: SeaCargoTerms (serviceTerm) 정상 매핑")
    void applySeaCreate_cargoTerms_mapped() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        CreateHouseBlCommand.SeaDetailCommand cmd = createSeaDetailCommand();
        sut.applySeaCreate(sea, cmd);
        assertThat(sea.getSayInformation()).isEqualTo("SAY 10 PKGS");
        assertThat(sea.getNoOfContainerOrPackages()).isEqualTo("10");
    }

    // ── applySeaUpdate ────────────────────────────────────────────────

    @Test
    @DisplayName("applySeaUpdate: SeaDetailCommand null이면 sea 필드 미변경")
    void applySeaUpdate_nullCommand_noChange() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sut.applySeaUpdate(sea, null);
        assertThat(sea.getLinerCode()).isNull();
    }

    @Test
    @DisplayName("applySeaUpdate: 엔티티가 HouseBlSea가 아니면 무시")
    void applySeaUpdate_nonSeaEntity_noChange() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        UpdateHouseBlCommand.SeaDetailCommand cmd = createUpdateSeaDetailCommand();
        sut.applySeaUpdate(truck, cmd);
        // 예외 없이 종료 확인
    }

    @Test
    @DisplayName("applySeaUpdate: linerCode 업데이트 시 반영")
    void applySeaUpdate_linerCode_updated() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        UpdateHouseBlCommand.SeaDetailCommand cmd = createUpdateSeaDetailCommand();
        sut.applySeaUpdate(sea, cmd);
        assertThat(sea.getLinerCode()).isNotNull();
        assertThat(sea.getLinerCode().value()).isEqualTo("LINER02");
    }

    @Test
    @DisplayName("applySeaUpdate: blType, vesselNationality null이면 미반영")
    void applySeaUpdate_blTypeNull_notUpdated() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        UpdateHouseBlCommand.SeaDetailCommand cmd = new UpdateHouseBlCommand.SeaDetailCommand(
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null);
        sut.applySeaUpdate(sea, cmd);
        assertThat(sea.getBlType()).isNull();
        assertThat(sea.getVesselNationality()).isNull();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────

    private CreateHouseBlCommand.SeaDetailCommand createSeaDetailCommand() {
        return new CreateHouseBlCommand.SeaDetailCommand(
                "FCL",          // loadType
                "LINER01",      // linerCode
                "EV01",         // vesselCode
                "EVER GREEN",   // vesselName
                "V001",         // voyageNo
                "20250101",     // onboardDate
                "KRPUS",        // porCode
                "USNYC",        // finalDestCode
                "20250102",     // issueDate
                3,              // noOfBl
                "KRPUS",        // issuePlace
                "20250103",     // doDate
                "USNYC",        // payableAt
                false,          // triangle
                "CY/CY",        // serviceTerm
                null,           // vesselCode2
                "KR",           // vesselNationality
                BigDecimal.TEN, // rton
                "SAY 10 PKGS",  // sayInformation
                "10",           // noOfContainerOrPackages
                "ORIGINAL",     // blType
                "KRPUS"         // deliveryCode
        );
    }

    private UpdateHouseBlCommand.SeaDetailCommand createUpdateSeaDetailCommand() {
        return new UpdateHouseBlCommand.SeaDetailCommand(
                null,           // loadType
                "LINER02",      // linerCode
                null,           // vesselCode
                "EVER GREEN 2", // vesselName
                "V002",         // voyageNo
                null,           // onboardDate
                null,           // porCode
                null,           // finalDestCode
                null,           // issueDate
                null,           // noOfBl
                null,           // issuePlace
                null,           // doDate
                null,           // payableAt
                null,           // triangle
                null,           // serviceTerm
                null,           // vesselNationality
                null,           // rton
                null,           // sayInformation
                null,           // noOfContainerOrPackages
                null,           // blType
                null            // deliveryCode
        );
    }
}
