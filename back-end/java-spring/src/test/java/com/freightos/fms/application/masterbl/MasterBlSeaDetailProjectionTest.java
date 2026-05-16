package com.freightos.fms.application.masterbl;

import com.freightos.fms.application.masterbl.projection.DescProjection;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.application.masterbl.projection.SeaDetailProjection;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.common.vo.LinerCode;
import com.freightos.fms.domain.common.vo.PortCode;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MasterBlFactory.toDetailResult SEA 분기 — SeaDetailProjection·DescProjection 매핑 검증.
 * 격리 원칙: AIR 분기 seaDetail=null 포함 4 케이스.
 */
class MasterBlSeaDetailProjectionTest {

    private final MasterBlFactory factory = new MasterBlFactory(new MasterBlSubFactory(), new MasterBlSeaSubFactory(), new MasterBlAirSubFactory());

    // ── 케이스 1: SEA + desc 정상 detail happy path ───────────────────

    @Test
    @DisplayName("toDetailResult: SEA + desc 정상 → SeaDetailProjection 전 필드 매핑, root desc 노출")
    void toDetailResult_seaWithDesc_mapsAllSeaDetailFieldsAndRootDesc() {
        MasterBlSea sea = MasterBlSea.create(Bound.EXP);
        sea.updateSeaFields(
                LoadType.FCL,
                LinerCode.of("LINER01"),
                VesselVoyage.of("VC001", "VESSEL ONE", "VY001"),
                BlDate.of("20260101"),
                BlNumber.of("BKGNO001"),
                BlDate.of("20260105")
        );
        sea.updateVesselNationality("KR");
        sea.updateServiceTerm(ServiceTerm.CY_CY);
        sea.updateBlType(BlType.ORIGINAL);
        sea.updateRoute(PortCode.of("KRPUS"), PortCode.of("USNYC"));
        sea.updateRton(Rton.of(BigDecimal.valueOf(10.5)));
        sea.updateRemark("SEA REMARK");

        MasterBlDesc desc = MasterBlDesc.create(null);
        desc.updateContent("MARKS TEXT", "DESC TEXT", DescClause1.A, DescClause2.A);
        sea.initDesc(desc);

        MasterBlDetailResult result = factory.toDetailResult(sea, List.of(), List.of());

        SeaDetailProjection seaDetail = result.seaDetail();
        assertThat(seaDetail).isNotNull();
        assertThat(seaDetail.loadType()).isEqualTo("FCL");
        assertThat(seaDetail.linerCode()).isEqualTo("LINER01");
        assertThat(seaDetail.vesselCode()).isEqualTo("VC001");
        assertThat(seaDetail.vesselName()).isEqualTo("VESSEL ONE");
        assertThat(seaDetail.voyageNo()).isEqualTo("VY001");
        assertThat(seaDetail.onboardDate()).isEqualTo("20260101");
        assertThat(seaDetail.vesselNationality()).isEqualTo("KR");
        assertThat(seaDetail.serviceTerm()).isEqualTo("CY_CY");
        assertThat(seaDetail.blType()).isEqualTo("ORIGINAL");
        assertThat(seaDetail.porCode()).isEqualTo("KRPUS");
        assertThat(seaDetail.finalDestCode()).isEqualTo("USNYC");
        assertThat(seaDetail.rton()).isEqualByComparingTo(BigDecimal.valueOf(10.5));
        assertThat(seaDetail.lineBkgNo()).isEqualTo("BKGNO001");
        assertThat(seaDetail.issueDate()).isEqualTo("20260105");
        assertThat(seaDetail.remark()).isEqualTo("SEA REMARK");

        // desc는 root result에 노출된다 (seaDetail.desc 제거)
        DescProjection descProjection = result.desc();
        assertThat(descProjection).isNotNull();
        assertThat(descProjection.marks()).isEqualTo("MARKS TEXT");
        assertThat(descProjection.description()).isEqualTo("DESC TEXT");
        assertThat(descProjection.descClause1()).isEqualTo("A");
        assertThat(descProjection.descClause2()).isEqualTo("A");
    }

    // ── 케이스 2: desc null → DescProjection.empty() 분기 ────────────

    @Test
    @DisplayName("toDetailResult: SEA + desc=null → root DescProjection.empty() 반환")
    void toDetailResult_seaWithNullDesc_returnsEmptyDescProjectionAtRoot() {
        MasterBlSea sea = MasterBlSea.create(Bound.EXP);
        // desc 미설정 — sea.getDesc() == null

        MasterBlDetailResult result = factory.toDetailResult(sea, List.of(), List.of());

        SeaDetailProjection seaDetail = result.seaDetail();
        assertThat(seaDetail).isNotNull();

        // desc는 root result에서 확인
        DescProjection descProjection = result.desc();
        assertThat(descProjection).isNotNull();
        assertThat(descProjection.marks()).isNull();
        assertThat(descProjection.description()).isNull();
        assertThat(descProjection.descClause1()).isNull();
        assertThat(descProjection.descClause2()).isNull();
    }

    // ── 케이스 3: 본체 address 3 필드 노출 검증 ──────────────────────

    @Test
    @DisplayName("toDetailResult: SEA + CustomerCode(value, address) → address 3 필드 노출")
    void toDetailResult_seaWithCustomerAddress_exposesAddressFields() {
        MasterBlSea sea = MasterBlSea.create(Bound.EXP);
        sea.assignParties(
                CustomerCode.of("SHIP01", "SHIPPER ADDRESS LINE"),
                CustomerCode.of("CONS01", "CONSIGNEE ADDRESS LINE"),
                CustomerCode.of("NOTI01", "NOTIFY ADDRESS LINE")
        );

        MasterBlDetailResult result = factory.toDetailResult(sea, List.of(), List.of());

        assertThat(result.shipperCode()).isEqualTo("SHIP01");
        assertThat(result.shipperAddress()).isEqualTo("SHIPPER ADDRESS LINE");
        assertThat(result.consigneeCode()).isEqualTo("CONS01");
        assertThat(result.consigneeAddress()).isEqualTo("CONSIGNEE ADDRESS LINE");
        assertThat(result.notifyCode()).isEqualTo("NOTI01");
        assertThat(result.notifyAddress()).isEqualTo("NOTIFY ADDRESS LINE");
    }

    // ── 케이스 4: AIR Master → seaDetail=null 격리 보장 ─────────────

    @Test
    @DisplayName("toDetailResult: AIR Master 진입 시 seaDetail=null (SEA 격리 원칙)")
    void toDetailResult_airMasterBl_hasNullSeaDetail() {
        MasterBlAir air = MasterBlAir.create(Bound.EXP);

        MasterBlDetailResult result = factory.toDetailResult(air, List.of(), List.of());

        assertThat(result.seaDetail()).isNull();
    }

    // ── 케이스 5: mainItemName / hsCode / settlePartnerCode root 매핑 ─

    @Test
    @DisplayName("toDetailResult: mainItemName·hsCode·settlePartnerCode root 필드 매핑")
    void toDetailResult_tradeInfoAndSettlePartner_mappedToRoot() {
        MasterBlSea sea = MasterBlSea.create(Bound.EXP);
        sea.updateTradeInfo("ELECTRONIC PARTS", "8542.31");
        sea.assignSettlePartner(com.freightos.fms.domain.common.vo.CustomerCode.of("SETTLE01"));

        MasterBlDetailResult result = factory.toDetailResult(sea, List.of(), List.of());

        assertThat(result.mainItemName()).isEqualTo("ELECTRONIC PARTS");
        assertThat(result.hsCode()).isEqualTo("8542.31");
        assertThat(result.settlePartnerCode()).isEqualTo("SETTLE01");
    }
}
