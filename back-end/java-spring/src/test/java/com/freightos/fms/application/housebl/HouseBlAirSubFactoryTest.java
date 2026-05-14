package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.vo.AirlineCode;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.CurrencyCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.enums.CargoType;
import com.freightos.fms.domain.housebl.enums.Fhd;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HouseBlAirSubFactory — applyAirCreate(20필드) + applyAirUpdate(PATCH 의미론) 단위 테스트.
 */
class HouseBlAirSubFactoryTest {

    private HouseBlAirSubFactory sut;

    @BeforeEach
    void setUp() {
        sut = new HouseBlAirSubFactory();
    }

    // ── applyAirCreate ───────────────────────────────────────────────

    @Test
    @DisplayName("applyAirCreate: 17개 AIR 확장 필드 전부 매핑된다")
    void applyAirCreate_allFields_mappedCorrectly() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        CreateHouseBlCommand.AirDetailCommand detail = new CreateHouseBlCommand.AirDetailCommand(
                "KE", BigDecimal.valueOf(100), BigDecimal.valueOf(90),
                "Q", "USD",
                "N.V.D.", "N.C.V.", "NIL", "ACCT",
                "PREPAID",
                "20250101", "ICN", "PILOT",
                "F", "CARGO_TEXT",
                "KOREA", "DG"
        );

        sut.applyAirCreate(air, detail);

        assertThat(air.getAirlineCode()).isEqualTo(AirlineCode.of("KE"));
        assertThat(air.getChargeWeightKg()).isEqualTo(Weight.of(BigDecimal.valueOf(100)));
        assertThat(air.getVolumeWeightKg()).isEqualTo(Weight.of(BigDecimal.valueOf(90)));
        assertThat(air.getRateClass()).isEqualTo(RateClass.Q);
        assertThat(air.getCurrencyCode()).isEqualTo(CurrencyCode.of("USD"));
        assertThat(air.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(air.getDeclaredValueCustoms()).isEqualTo("N.C.V.");
        assertThat(air.getInsurance()).isEqualTo("NIL");
        assertThat(air.getAccountInformation()).isEqualTo("ACCT");
        assertThat(air.getOtherTerm()).isEqualTo(FreightTerm.PREPAID);
        assertThat(air.getIssueDate()).isEqualTo(BlDate.of("20250101"));
        assertThat(air.getIssuePlace()).isEqualTo(PortCode.of("ICN"));
        assertThat(air.getSignature()).isEqualTo("PILOT");
        assertThat(air.getFhd()).isEqualTo(Fhd.F);
        assertThat(air.getHandlingInformation()).isNotNull();
        assertThat(air.getHandlingInformation().description()).isEqualTo("CARGO_TEXT");
        assertThat(air.getOriginOfGoods()).isEqualTo("KOREA");
        assertThat(air.getCargoType()).isEqualTo(CargoType.DG);
    }

    @Test
    @DisplayName("applyAirCreate: airDetail null이면 아무것도 변경하지 않는다")
    void applyAirCreate_nullDetail_noChange() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);

        sut.applyAirCreate(air, null);

        assertThat(air.getAirlineCode()).isNull();
        assertThat(air.getRateClass()).isNull();
    }

    // ── applyAirUpdate PATCH 의미론 ──────────────────────────────────

    @Test
    @DisplayName("applyAirUpdate: 17개 AIR 확장 필드 전부 매핑된다")
    void applyAirUpdate_allFields_mappedCorrectly() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        UpdateHouseBlCommand.AirDetailCommand detail = new UpdateHouseBlCommand.AirDetailCommand(
                "OZ", BigDecimal.valueOf(200), BigDecimal.valueOf(180),
                "N", "KRW",
                "DECLARED", "CUSTOMS_VAL", "INSURANCE_VAL", "ACCT2",
                "COLLECT",
                "20250201", "GMP", "CAPTAIN",
                "D", "HANDLING_DESC",
                "JAPAN", "KC"
        );

        sut.applyAirUpdate(air, detail);

        assertThat(air.getAirlineCode()).isEqualTo(AirlineCode.of("OZ"));
        assertThat(air.getChargeWeightKg()).isEqualTo(Weight.of(BigDecimal.valueOf(200)));
        assertThat(air.getVolumeWeightKg()).isEqualTo(Weight.of(BigDecimal.valueOf(180)));
        assertThat(air.getRateClass()).isEqualTo(RateClass.N);
        assertThat(air.getCurrencyCode()).isEqualTo(CurrencyCode.of("KRW"));
        assertThat(air.getDeclaredValueCarriage()).isEqualTo("DECLARED");
        assertThat(air.getDeclaredValueCustoms()).isEqualTo("CUSTOMS_VAL");
        assertThat(air.getInsurance()).isEqualTo("INSURANCE_VAL");
        assertThat(air.getAccountInformation()).isEqualTo("ACCT2");
        assertThat(air.getOtherTerm()).isEqualTo(FreightTerm.COLLECT);
        assertThat(air.getIssueDate()).isEqualTo(BlDate.of("20250201"));
        assertThat(air.getIssuePlace()).isEqualTo(PortCode.of("GMP"));
        assertThat(air.getSignature()).isEqualTo("CAPTAIN");
        assertThat(air.getFhd()).isEqualTo(Fhd.D);
        assertThat(air.getHandlingInformation().description()).isEqualTo("HANDLING_DESC");
        assertThat(air.getOriginOfGoods()).isEqualTo("JAPAN");
        assertThat(air.getCargoType()).isEqualTo(CargoType.KC);
    }

    @Test
    @DisplayName("applyAirUpdate PATCH: null 필드는 기존 값을 유지한다")
    void applyAirUpdate_nullFields_preservesExistingValues() {
        // 초기 상태 세팅
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        air.updateAirFields(new HouseBlAir.AirFields(
                AirlineCode.of("KE"),
                Weight.of(BigDecimal.valueOf(100)),
                Weight.of(BigDecimal.valueOf(90)),
                RateClass.Q,
                CurrencyCode.of("USD"),
                "N.V.D.", "N.C.V.", "NIL", "ACCT",
                FreightTerm.PREPAID,
                BlDate.of("20250101"), PortCode.of("ICN"), "PILOT",
                Fhd.F, null, "KOREA", CargoType.DG
        ));

        // airlineCode만 변경, 나머지 null (기존 값 유지)
        UpdateHouseBlCommand.AirDetailCommand partial = new UpdateHouseBlCommand.AirDetailCommand(
                "OZ", null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        sut.applyAirUpdate(air, partial);

        assertThat(air.getAirlineCode()).isEqualTo(AirlineCode.of("OZ"));
        // 나머지는 기존 값 유지
        assertThat(air.getChargeWeightKg()).isEqualTo(Weight.of(BigDecimal.valueOf(100)));
        assertThat(air.getVolumeWeightKg()).isEqualTo(Weight.of(BigDecimal.valueOf(90)));
        assertThat(air.getRateClass()).isEqualTo(RateClass.Q);
        assertThat(air.getCurrencyCode()).isEqualTo(CurrencyCode.of("USD"));
        assertThat(air.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(air.getInsurance()).isEqualTo("NIL");
        assertThat(air.getOtherTerm()).isEqualTo(FreightTerm.PREPAID);
        assertThat(air.getIssueDate()).isEqualTo(BlDate.of("20250101"));
        assertThat(air.getIssuePlace()).isEqualTo(PortCode.of("ICN"));
        assertThat(air.getSignature()).isEqualTo("PILOT");
        assertThat(air.getFhd()).isEqualTo(Fhd.F);
        assertThat(air.getOriginOfGoods()).isEqualTo("KOREA");
        assertThat(air.getCargoType()).isEqualTo(CargoType.DG);
    }

    @Test
    @DisplayName("applyAirUpdate: airDetail null이면 아무것도 변경하지 않는다")
    void applyAirUpdate_nullDetail_noChange() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        air.updateAirFields(new HouseBlAir.AirFields(
                AirlineCode.of("KE"), null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null, null, null
        ));

        sut.applyAirUpdate(air, null);

        assertThat(air.getAirlineCode()).isEqualTo(AirlineCode.of("KE"));
    }

    // ── applyAirRemark ───────────────────────────────────────────────

    @Test
    @DisplayName("applyAirRemark: remark 필드 갱신")
    void applyAirRemark_setsRemark() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);

        sut.applyAirRemark(air, "TEST-REMARK");

        assertThat(air.getRemark()).isEqualTo("TEST-REMARK");
    }
}
