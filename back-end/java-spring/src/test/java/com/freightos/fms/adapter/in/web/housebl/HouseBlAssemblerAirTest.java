package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.fms.application.housebl.command.CreateHouseBlCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HouseBlAssembler AIR 확장 필드 wiring 검증.
 * toCreateCommand / toUpdateCommand 호출 시 AirDetailRequest → AirDetailCommand 18개 필드 1:1 매핑 확인.
 */
class HouseBlAssemblerAirTest {

    private final HouseBlAssembler assembler = new HouseBlAssembler();

    // ── CREATE ────────────────────────────────────────────────────────

    @Test
    void toCreateCommand_airDetail_필드_18개_1대1_매핑() {
        CreateHouseBlRequest.AirDetailRequest airDetail = new CreateHouseBlRequest.AirDetailRequest(
                "KE",
                new BigDecimal("100.5"),
                new BigDecimal("95.0"),
                "Q",
                "USD",
                "NVD",
                "NVD",
                "XXX",
                "ACC-INFO",
                "OTHER",
                "20260514",
                "ICN",
                "SIGNATURE",
                "FHD-CODE",
                "A",
                "HANDLING INFO",
                "KR",
                "GEN"
        );
        CreateHouseBlRequest req = minimalCreateRequest(airDetail);

        CreateHouseBlCommand cmd = assembler.toCreateCommand(req);

        CreateHouseBlCommand.AirDetailCommand a = cmd.airDetail();
        assertThat(a).isNotNull();
        assertThat(a.airlineCode()).isEqualTo("KE");
        assertThat(a.chargeWeightKg()).isEqualByComparingTo("100.5");
        assertThat(a.volumeWeightKg()).isEqualByComparingTo("95.0");
        assertThat(a.rateClass()).isEqualTo("Q");
        assertThat(a.currencyCode()).isEqualTo("USD");
        assertThat(a.declaredValueCarriage()).isEqualTo("NVD");
        assertThat(a.declaredValueCustoms()).isEqualTo("NVD");
        assertThat(a.insurance()).isEqualTo("XXX");
        assertThat(a.accountInformation()).isEqualTo("ACC-INFO");
        assertThat(a.otherTerm()).isEqualTo("OTHER");
        assertThat(a.issueDate()).isEqualTo("20260514");
        assertThat(a.issuePlace()).isEqualTo("ICN");
        assertThat(a.signature()).isEqualTo("SIGNATURE");
        assertThat(a.fhd()).isEqualTo("FHD-CODE");
        assertThat(a.handlingInformationCode()).isEqualTo("A");
        assertThat(a.handlingInformationDesc()).isEqualTo("HANDLING INFO");
        assertThat(a.originOfGoods()).isEqualTo("KR");
        assertThat(a.cargoType()).isEqualTo("GEN");
    }

    @Test
    void toCreateCommand_airDetail_null_이면_command_airDetail_null() {
        CreateHouseBlRequest req = minimalCreateRequest(null);

        CreateHouseBlCommand cmd = assembler.toCreateCommand(req);

        assertThat(cmd.airDetail()).isNull();
    }

    // ── UPDATE ────────────────────────────────────────────────────────

    @Test
    void toUpdateCommand_airDetail_필드_18개_1대1_매핑() {
        UpdateHouseBlRequest.AirDetailRequest airDetail = new UpdateHouseBlRequest.AirDetailRequest(
                "OZ",
                new BigDecimal("200.0"),
                new BigDecimal("180.0"),
                "M",
                "KRW",
                "DECLARED-C",
                "DECLARED-CU",
                "INS",
                "ACCT",
                "OT",
                "20260601",
                "GMP",
                "SIG2",
                "FHD2",
                "A",
                "HAND2",
                "US",
                "DGR"
        );
        UpdateHouseBlRequest req = minimalUpdateRequest(airDetail);

        UpdateHouseBlCommand cmd = assembler.toUpdateCommand(req);

        UpdateHouseBlCommand.AirDetailCommand a = cmd.airDetail();
        assertThat(a).isNotNull();
        assertThat(a.airlineCode()).isEqualTo("OZ");
        assertThat(a.chargeWeightKg()).isEqualByComparingTo("200.0");
        assertThat(a.volumeWeightKg()).isEqualByComparingTo("180.0");
        assertThat(a.rateClass()).isEqualTo("M");
        assertThat(a.currencyCode()).isEqualTo("KRW");
        assertThat(a.declaredValueCarriage()).isEqualTo("DECLARED-C");
        assertThat(a.declaredValueCustoms()).isEqualTo("DECLARED-CU");
        assertThat(a.insurance()).isEqualTo("INS");
        assertThat(a.accountInformation()).isEqualTo("ACCT");
        assertThat(a.otherTerm()).isEqualTo("OT");
        assertThat(a.issueDate()).isEqualTo("20260601");
        assertThat(a.issuePlace()).isEqualTo("GMP");
        assertThat(a.signature()).isEqualTo("SIG2");
        assertThat(a.fhd()).isEqualTo("FHD2");
        assertThat(a.handlingInformationCode()).isEqualTo("A");
        assertThat(a.handlingInformationDesc()).isEqualTo("HAND2");
        assertThat(a.originOfGoods()).isEqualTo("US");
        assertThat(a.cargoType()).isEqualTo("DGR");
    }

    @Test
    void toUpdateCommand_airDetail_null_이면_command_airDetail_null() {
        UpdateHouseBlRequest req = minimalUpdateRequest(null);

        UpdateHouseBlCommand cmd = assembler.toUpdateCommand(req);

        assertThat(cmd.airDetail()).isNull();
    }

    // ── fixtures ─────────────────────────────────────────────────────

    private CreateHouseBlRequest minimalCreateRequest(CreateHouseBlRequest.AirDetailRequest airDetail) {
        // 54개 필드 순서: jobDiv(1)~masterRefNo(34) + Non B/L(35~45) + remark(46) + seaDetail(47) + airDetail(48) + sub(49~54)
        return new CreateHouseBlRequest(
                "AIR",  // 1 jobDiv
                null,   // 2 bound
                null,   // 3 hblNo
                null,   // 4 shipmentType
                null,   // 5 freightTerm
                null,   // 6 shipperCode
                null,   // 7 shipperAddress
                null,   // 8 consigneeCode
                null,   // 9 consigneeAddress
                null,   // 10 notifyCode
                null,   // 11 notifyAddress
                null,   // 12 docPartnerCode
                null,   // 13 docPartnerAddress
                null,   // 14 settlePartnerCode
                null,   // 15 polCode
                null,   // 16 podCode
                null,   // 17 etd
                null,   // 18 eta
                null,   // 19 pkgQty
                null,   // 20 pkgUnit
                null,   // 21 weightUnit
                null,   // 22 grossWeightKg
                null,   // 23 cbm
                null,   // 24 actualCustomerCode
                null,   // 25 operatorCode
                null,   // 26 teamCode
                null,   // 27 salesManCode
                null,   // 28 masterBlId
                null,   // 29 incoterms
                null,   // 30 salesClass
                null,   // 31 mainItemName
                null,   // 32 hsCode
                null,   // 33 mblNo
                null,   // 34 masterRefNo
                null,   // 35 workDivision
                null,   // 36 originalBlRef
                null,   // 37 linerCode
                null,   // 38 linerName
                null,   // 39 vesselName
                null,   // 40 voyageNo
                null,   // 41 finalDestCode
                null,   // 42 finalDestName
                null,   // 43 finalEta
                null,   // 44 volumeWeightKg
                null,   // 45 rton
                null,   // 46 remark
                null,   // 47 seaDetail
                airDetail, // 48 airDetail
                null,   // 49 desc
                null,   // 50 dims
                null,   // 51 containers
                null,   // 52 scheduleLegs
                null,   // 53 truckOrders
                null    // 54 airCharges
        );
    }

    private UpdateHouseBlRequest minimalUpdateRequest(UpdateHouseBlRequest.AirDetailRequest airDetail) {
        // 53개 필드 순서: jobDiv(1)~masterRefNo(33) + Non B/L(34~44) + remark(45) + seaDetail(46) + airDetail(47) + sub(48~53)
        return new UpdateHouseBlRequest(
                "AIR",  // 1 jobDiv
                null,   // 2 bound
                null,   // 3 shipmentType
                null,   // 4 freightTerm
                null,   // 5 shipperCode
                null,   // 6 shipperAddress
                null,   // 7 consigneeCode
                null,   // 8 consigneeAddress
                null,   // 9 notifyCode
                null,   // 10 notifyAddress
                null,   // 11 docPartnerCode
                null,   // 12 docPartnerAddress
                null,   // 13 settlePartnerCode
                null,   // 14 polCode
                null,   // 15 podCode
                null,   // 16 etd
                null,   // 17 eta
                null,   // 18 pkgQty
                null,   // 19 pkgUnit
                null,   // 20 weightUnit
                null,   // 21 grossWeightKg
                null,   // 22 cbm
                null,   // 23 actualCustomerCode
                null,   // 24 operatorCode
                null,   // 25 teamCode
                null,   // 26 salesManCode
                null,   // 27 masterBlId
                null,   // 28 incoterms
                null,   // 29 salesClass
                null,   // 30 mainItemName
                null,   // 31 hsCode
                null,   // 32 mblNo
                null,   // 33 masterRefNo
                null,   // 34 workDivision
                null,   // 35 originalBlRef
                null,   // 36 linerCode
                null,   // 37 linerName
                null,   // 38 vesselName
                null,   // 39 voyageNo
                null,   // 40 finalDestCode
                null,   // 41 finalDestName
                null,   // 42 finalEta
                null,   // 43 volumeWeightKg
                null,   // 44 rton
                null,   // 45 remark
                null,   // 46 seaDetail
                airDetail, // 47 airDetail
                null,   // 48 desc
                null,   // 49 dims
                null,   // 50 containers
                null,   // 51 scheduleLegs
                null,   // 52 truckOrders
                null    // 53 airCharges
        );
    }
}
