package com.freightos.fms.application.housebl;

import com.freightos.fms.application.housebl.projection.AirDescProjection;
import com.freightos.fms.application.housebl.projection.AirDetailProjection;
import com.freightos.fms.application.housebl.projection.HouseBlDetailResult;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Per;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.vo.AirlineCode;
import com.freightos.fms.domain.common.vo.CurrencyCode;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlAirCharge;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlDim;
import com.freightos.fms.domain.housebl.entity.HouseBlScheduleLeg;
import com.freightos.fms.domain.housebl.enums.CargoType;
import com.freightos.fms.domain.housebl.enums.Fhd;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HouseBlFactory.toDetailResult — AIR 자식 컬렉션 및 17 확장 필드 projection 변환 검증.
 * HouseBlFactory는 순수 Spring Component이므로 @ExtendWith 없이 직접 생성.
 */
class HouseBlFactoryAirTest {

    private HouseBlFactory sut;

    @BeforeEach
    void setUp() {
        sut = new HouseBlFactory(
                new HouseBlSubFactory(),
                new HouseBlSeaSubFactory(),
                new HouseBlTruckSubFactory(),
                new HouseBlNonBlSubFactory(),
                new HouseBlAirSubFactory(),
                new HouseBlSeaDetailSubFactory(),
                new HouseBlAirDetailSubFactory()
        );
    }

    @Test
    @DisplayName("AIR 엔티티 → airDetail 비null, seaDetail null")
    void toDetailResult_airEntity_airDetailNotNullSeaDetailNull() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);

        HouseBlDetailResult result = sut.toDetailResult(air);

        assertThat(result.airDetail()).isNotNull();
        assertThat(result.seaDetail()).isNull();
    }

    @Test
    @DisplayName("AIR 엔티티의 17개 확장 필드가 airDetail에 정확히 매핑된다")
    void toDetailResult_airWithExtendedFields_allFieldsMapped() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlAir.AirFields fields = new HouseBlAir.AirFields(
                AirlineCode.of("KE"),
                Weight.of(BigDecimal.valueOf(100)),
                Weight.of(BigDecimal.valueOf(90)),
                RateClass.Q,
                CurrencyCode.of("USD"),
                "N.V.D.", "N.C.V.", "NIL", "ACCT-001",
                FreightTerm.PREPAID,
                null, null, "PILOT",
                Fhd.F,
                null,
                "KOREA",
                CargoType.DG
        );
        air.updateAirFields(fields);

        HouseBlDetailResult result = sut.toDetailResult(air);

        AirDetailProjection ad = result.airDetail();
        assertThat(ad.airlineCode()).isEqualTo("KE");
        assertThat(ad.chargeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(ad.volumeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(90));
        assertThat(ad.rateClass()).isEqualTo(RateClass.Q.name());
        assertThat(ad.currencyCode()).isEqualTo("USD");
        assertThat(ad.declaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(ad.declaredValueCustoms()).isEqualTo("N.C.V.");
        assertThat(ad.insurance()).isEqualTo("NIL");
        assertThat(ad.accountInformation()).isEqualTo("ACCT-001");
        assertThat(ad.otherTerm()).isEqualTo(FreightTerm.PREPAID.name());
        assertThat(ad.issueDate()).isNull();
        assertThat(ad.issuePlace()).isNull();
        assertThat(ad.signature()).isEqualTo("PILOT");
        assertThat(ad.fhd()).isEqualTo(Fhd.F.name());
        assertThat(ad.handlingInformationCode()).isNull();
        assertThat(ad.handlingInformationDesc()).isNull();
        assertThat(ad.originOfGoods()).isEqualTo("KOREA");
        assertThat(ad.cargoType()).isEqualTo(CargoType.DG.getCode());
    }

    @Test
    @DisplayName("AIR 엔티티에 scheduleLegs 1건 있을 때 airDetail.scheduleLegs size=1")
    void toDetailResult_airWithOneScheduleLeg_scheduleLegsExposed() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlScheduleLeg leg = HouseBlScheduleLeg.create(null, "JPNRT", "2025-01-01", "2025-01-02");
        leg.updateDetails("JPNRT", "KE", "KE001", "2025-01-01", "1000", "2025-01-02", "1200");
        air.initScheduleLegs(List.of(leg));

        HouseBlDetailResult result = sut.toDetailResult(air);

        assertThat(result.airDetail().scheduleLegs()).hasSize(1);
        assertThat(result.airDetail().scheduleLegs().get(0).toCode()).isEqualTo("JPNRT");
        assertThat(result.airDetail().scheduleLegs().get(0).byCarrier()).isEqualTo("KE");
        assertThat(result.airDetail().scheduleLegs().get(0).flightNo()).isEqualTo("KE001");
    }

    @Test
    @DisplayName("AIR 엔티티에 airCharges 1건 있을 때 airDetail.airCharges size=1, 필드 일치")
    void toDetailResult_airWithOneCharge_airChargesExposed() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlAirCharge charge = HouseBlAirCharge.create(null);
        charge.updateDetails(new HouseBlAirCharge.Details(
                "AIR-FR",
                CurrencyCode.of("USD"),
                Per.CW,
                FreightTerm.PREPAID,
                Weight.of(BigDecimal.valueOf(50)),
                RateClass.N,
                Weight.of(BigDecimal.valueOf(55)),
                BigDecimal.valueOf(3.5)
        ));
        air.initAirCharges(List.of(charge));

        HouseBlDetailResult result = sut.toDetailResult(air);

        assertThat(result.airDetail().airCharges()).hasSize(1);
        assertThat(result.airDetail().airCharges().get(0).freightCode()).isEqualTo("AIR-FR");
        assertThat(result.airDetail().airCharges().get(0).currencyCode()).isEqualTo("USD");
        assertThat(result.airDetail().airCharges().get(0).per()).isEqualTo(Per.CW.getCode());
        assertThat(result.airDetail().airCharges().get(0).freightTerm()).isEqualTo(FreightTerm.PREPAID.name());
        assertThat(result.airDetail().airCharges().get(0).grossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(result.airDetail().airCharges().get(0).rateClass()).isEqualTo(RateClass.N.name());
        assertThat(result.airDetail().airCharges().get(0).chargeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(55));
        assertThat(result.airDetail().airCharges().get(0).rate()).isEqualByComparingTo(BigDecimal.valueOf(3.5));
    }

    @Test
    @DisplayName("AIR 엔티티에 dims 1건 있을 때 airDetail.dims size=1, 필드 일치")
    void toDetailResult_airWithOneDim_dimsExposed() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlDim dim = HouseBlDim.create(null,
                BigDecimal.valueOf(100), BigDecimal.valueOf(80), BigDecimal.valueOf(60),
                2, BigDecimal.valueOf(0.48), BigDecimal.valueOf(32));
        air.initDims(List.of(dim));

        HouseBlDetailResult result = sut.toDetailResult(air);

        assertThat(result.airDetail().dims()).hasSize(1);
        assertThat(result.airDetail().dims().get(0).lengthCm()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(result.airDetail().dims().get(0).widthCm()).isEqualByComparingTo(BigDecimal.valueOf(80));
        assertThat(result.airDetail().dims().get(0).heightCm()).isEqualByComparingTo(BigDecimal.valueOf(60));
        assertThat(result.airDetail().dims().get(0).quantity()).isEqualTo(2);
        assertThat(result.airDetail().dims().get(0).cbm()).isEqualByComparingTo(BigDecimal.valueOf(0.48));
        assertThat(result.airDetail().dims().get(0).volumeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(32));
    }

    @Test
    @DisplayName("AIR desc가 있을 때 airDetail.desc 필드 일치")
    void toDetailResult_airWithDesc_descExposed() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlDesc desc = HouseBlDesc.create(null);
        desc.updateContent("AIR-MARK", "AIR-DESC", DescClause1.A, DescClause2.B);
        air.initDesc(desc);

        HouseBlDetailResult result = sut.toDetailResult(air);

        AirDescProjection d = result.airDetail().desc();
        assertThat(d).isNotNull();
        assertThat(d.marks()).isEqualTo("AIR-MARK");
        assertThat(d.description()).isEqualTo("AIR-DESC");
        assertThat(d.descClause1()).isEqualTo(DescClause1.A.name());
        assertThat(d.descClause2()).isEqualTo(DescClause2.B.name());
    }

    @Test
    @DisplayName("AIR desc null 시 airDetail.desc는 AirDescProjection.empty() 반환 (§6.55 SSOT)")
    void toDetailResult_airWithNullDesc_descIsEmpty() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);

        HouseBlDetailResult result = sut.toDetailResult(air);

        AirDescProjection d = result.airDetail().desc();
        assertThat(d).isEqualTo(AirDescProjection.empty());
        assertThat(d.marks()).isNull();
        assertThat(d.description()).isNull();
        assertThat(d.descClause1()).isNull();
        assertThat(d.descClause2()).isNull();
    }

    @Test
    @DisplayName("AIR scheduleLegs null → airDetail.scheduleLegs 빈 리스트")
    void toDetailResult_airWithNullScheduleLegs_emptyList() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        // scheduleLegs 초기화하지 않음 (HouseBl 기본 빈 ArrayList)

        HouseBlDetailResult result = sut.toDetailResult(air);

        assertThat(result.airDetail().scheduleLegs()).isEmpty();
    }

    @Test
    @DisplayName("AIR airCharges 빈 리스트 → airDetail.airCharges 빈 리스트")
    void toDetailResult_airWithEmptyAirCharges_emptyList() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        air.initAirCharges(List.of());

        HouseBlDetailResult result = sut.toDetailResult(air);

        assertThat(result.airDetail().airCharges()).isEmpty();
    }
}
