package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl.WorkDivision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class HouseBlNonBlSubFactoryTest {

    private HouseBlNonBlSubFactory sut;

    @BeforeEach
    void setUp() {
        sut = new HouseBlNonBlSubFactory();
    }

    // ── applyNonBlCreate ──────────────────────────────────────────────

    @Test
    @DisplayName("applyNonBlCreate: 엔티티가 HouseBlNonBl이 아니면 무시")
    void applyNonBlCreate_nonNonBlEntity_noChange() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        CreateHouseBlCommand cmd = createCommand();
        sut.applyNonBlCreate(sea, cmd);
        // sea에는 nonBl 필드 없음 — 단순히 예외 없이 종료 확인
    }

    @Test
    @DisplayName("applyNonBlCreate: originalBlRef 정상 매핑")
    void applyNonBlCreate_originalBlRef_mapped() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(WorkDivision.SEA, Bound.EXP);
        CreateHouseBlCommand cmd = createCommand();
        sut.applyNonBlCreate(nonBl, cmd);
        assertThat(nonBl.getOriginalBlRef()).isNotNull();
        assertThat(nonBl.getOriginalBlRef().value()).isEqualTo("BL123456");
    }

    @Test
    @DisplayName("applyNonBlCreate: scheduleFields (linerCode, vesselName 등) 정상 매핑")
    void applyNonBlCreate_scheduleFields_mapped() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(WorkDivision.SEA, Bound.EXP);
        CreateHouseBlCommand cmd = createCommand();
        sut.applyNonBlCreate(nonBl, cmd);
        assertThat(nonBl.getLinerCode()).isEqualTo("LINER01");
        assertThat(nonBl.getVesselName()).isEqualTo("EVER GREEN");
        assertThat(nonBl.getVoyageNo()).isEqualTo("V001");
        assertThat(nonBl.getFinalDestCode()).isEqualTo("USNYC");
    }

    @Test
    @DisplayName("applyNonBlCreate: remark 정상 매핑")
    void applyNonBlCreate_remark_mapped() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(WorkDivision.SEA, Bound.EXP);
        CreateHouseBlCommand cmd = createCommand();
        sut.applyNonBlCreate(nonBl, cmd);
        assertThat(nonBl.getRemark()).isEqualTo("Test remark");
    }

    // ── applyNonBlUpdate ──────────────────────────────────────────────

    @Test
    @DisplayName("applyNonBlUpdate: 엔티티가 HouseBlNonBl이 아니면 무시")
    void applyNonBlUpdate_nonNonBlEntity_noChange() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        UpdateHouseBlCommand cmd = updateCommand();
        sut.applyNonBlUpdate(sea, cmd);
        // 예외 없이 종료 확인
    }

    @Test
    @DisplayName("applyNonBlUpdate: workDivision null이면 미변경")
    void applyNonBlUpdate_workDivisionNull_notUpdated() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(WorkDivision.SEA, Bound.EXP);
        UpdateHouseBlCommand cmd = updateCommandWithNullWorkDivision();
        sut.applyNonBlUpdate(nonBl, cmd);
        assertThat(nonBl.getWorkDivision()).isEqualTo(WorkDivision.SEA);
    }

    @Test
    @DisplayName("applyNonBlUpdate: workDivision 변경 반영")
    void applyNonBlUpdate_workDivision_updated() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(WorkDivision.SEA, Bound.EXP);
        UpdateHouseBlCommand cmd = updateCommand();
        sut.applyNonBlUpdate(nonBl, cmd);
        assertThat(nonBl.getWorkDivision()).isEqualTo(WorkDivision.AIR);
    }

    @Test
    @DisplayName("applyNonBlUpdate: scheduleFields 업데이트 반영")
    void applyNonBlUpdate_scheduleFields_updated() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(WorkDivision.SEA, Bound.EXP);
        UpdateHouseBlCommand cmd = updateCommand();
        sut.applyNonBlUpdate(nonBl, cmd);
        assertThat(nonBl.getLinerCode()).isEqualTo("LINER02");
        assertThat(nonBl.getVesselName()).isEqualTo("EVER GIVEN");
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────

    private CreateHouseBlCommand createCommand() {
        // jobDiv(1) bound(2) hblNo(3) shipmentType(4) freightTerm(5)
        // shipperCode(6) shipperAddress(7) consigneeCode(8) consigneeAddress(9) notifyCode(10)
        // notifyAddress(11) docPartnerCode(12) docPartnerAddress(13) settlePartnerCode(14)
        // polCode(15) podCode(16) etd(17) eta(18) pkgQty(19) pkgUnit(20)
        // weightUnit(21) grossWeightKg(22) cbm(23) actualCustomerCode(24) operatorCode(25)
        // teamCode(26) salesManCode(27) masterBlId(28) incoterms(29) salesClass(30)
        // mainItemName(31) hsCode(32) mblNo(33) masterRefNo(34)
        // workDivision(35) originalBlRef(36) volumeDivisor(37) linerCode(38) linerName(39)
        // vesselName(40) voyageNo(41) finalDestCode(42) finalDestName(43) finalEta(44)
        // volumeWeightKg(45) rton(46) remark(47) seaDetail(48) airDetail(49)
        // desc(50) dims(51) containers(52) scheduleLegs(53) truckOrders(54) airCharges(55) truckDetail(56)
        return new CreateHouseBlCommand(
                "NON_BL", "EXP",                                                // 1-2
                null, null, null,                                                // 3-5
                null, null, null, null, null,                                    // 6-10
                null, null, null, null,                                          // 11-14
                null, null, null, null,                                          // 15-18
                null, null, null, null, null,                                    // 19-23
                null, null, null, null, null, null, null, null, null, null, null, // 24-34 (actualCustomerCode~masterRefNo)
                "SEA", "BL123456", null,                                         // 35-37 workDivision, originalBlRef, volumeDivisor
                "LINER01", "Liner Name", "EVER GREEN", "V001",                   // 38-41 linerCode~voyageNo
                "USNYC", "New York", "20250301",                                 // 42-44 finalDestCode~finalEta
                BigDecimal.valueOf(1000.0), BigDecimal.valueOf(2.5),             // 45-46 volumeWeightKg, rton
                "Test remark",                                                   // 47 remark
                null,                                                            // 48 seaDetail
                null,                                                            // 49 airDetail
                null, null, null, null, null, null,                              // 50-55 sub 엔티티
                null                                                             // 56 truckDetail
        );
    }

    private UpdateHouseBlCommand updateCommand() {
        // UpdateHouseBlCommand: 55개 파라미터 (hblNo 없음)
        // jobDiv(1) bound(2) shipmentType(3) freightTerm(4)
        // shipperCode(5) shipperAddress(6) consigneeCode(7) consigneeAddress(8) notifyCode(9)
        // notifyAddress(10) docPartnerCode(11) docPartnerAddress(12) settlePartnerCode(13)
        // polCode(14) podCode(15) etd(16) eta(17) pkgQty(18) pkgUnit(19)
        // weightUnit(20) grossWeightKg(21) cbm(22) actualCustomerCode(23) operatorCode(24)
        // teamCode(25) salesManCode(26) masterBlId(27) incoterms(28) salesClass(29)
        // mainItemName(30) hsCode(31) mblNo(32) masterRefNo(33)
        // workDivision(34) originalBlRef(35) volumeDivisor(36) linerCode(37) linerName(38)
        // vesselName(39) voyageNo(40) finalDestCode(41) finalDestName(42) finalEta(43)
        // volumeWeightKg(44) rton(45) remark(46) seaDetail(47) airDetail(48)
        // desc(49) dims(50) containers(51) scheduleLegs(52) truckOrders(53) airCharges(54) truckDetail(55)
        return new UpdateHouseBlCommand(
                "NON_BL", "EXP", null, null,                             // 1-4
                null, null, null, null, null,                            // 5-9
                null, null, null, null,                                  // 10-13
                null, null, null, null,                                  // 14-17
                null, null, null, null, null,                            // 18-22
                null, null, null, null, null, null, null, null, null, null, null, // 23-33
                "AIR", "BL654321", null,                                 // 34-36 workDivision, originalBlRef, volumeDivisor
                "LINER02", "Liner Name 2", "EVER GIVEN", "V002",         // 37-40 linerCode~voyageNo
                "USLAX", "Los Angeles", "20250401",                      // 41-43 finalDestCode~finalEta
                BigDecimal.valueOf(1200.0), BigDecimal.valueOf(3.0),     // 44-45 volumeWeightKg, rton
                "Updated remark",                                        // 46 remark
                null,                                                    // 47 seaDetail
                null,                                                    // 48 airDetail
                null, null, null, null, null, null,                      // 49-54 sub 엔티티
                null                                                     // 55 truckDetail
        );
    }

    private UpdateHouseBlCommand updateCommandWithNullWorkDivision() {
        return new UpdateHouseBlCommand(
                "NON_BL", "EXP", null, null,                             // 1-4
                null, null, null, null, null,                            // 5-9
                null, null, null, null,                                  // 10-13
                null, null, null, null,                                  // 14-17
                null, null, null, null, null,                            // 18-22
                null, null, null, null, null, null, null, null, null, null, null, // 23-33
                null,                                                    // 34 workDivision — null이면 미변경
                null, null, null, null, null, null, null, null, null,   // 35-43
                null, null, null,                                        // 44-46
                null,                                                    // 47 seaDetail
                null,                                                    // 48 airDetail
                null, null, null, null, null, null,                      // 49-54 sub 엔티티
                null                                                     // 55 truckDetail
        );
    }
}
