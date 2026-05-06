package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import com.freightos.fms.domain.common.vo.VesselVoyage;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HouseBl JPA 엔티티 구조 검증 테스트.
 * - JOINED 상속에서 @OneToOne 독립 엔티티 구조로 변경됨.
 * - Mapper(toDomain/toJpa) 검증은 Coder-4의 Mapper 리팩토링 완료 후 별도 갱신.
 */
class HouseBlMapperTest {

    private final HouseBlCargoMapper cargoMapper = new HouseBlCargoMapper();
    private final HouseBlDocMapper docMapper = new HouseBlDocMapper();
    private final HouseBlJpaToDomainMapper jpaToDomainMapper = new HouseBlJpaToDomainMapper(cargoMapper, docMapper);
    private final HouseBlDomainToJpaMapper domainToJpaMapper = new HouseBlDomainToJpaMapper();

    // ── applyCommonFields ────────────────────────────────────────────

    @Test
    @DisplayName("applyCommonFields: AIR domain → HouseBlJpaEntity에 jobDiv=AIR, 기본값 필드 복사된다")
    void applyCommonFields_airDomain_setsJobDivAir() {
        HouseBlAir domain = HouseBlAir.create(Bound.EXP);
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();

        domainToJpaMapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo(JobDiv.AIR);
        assertThat(jpa.getBound()).isEqualTo(Bound.EXP);
    }

    @Test
    @DisplayName("applyCommonFields: SEA domain → HouseBlJpaEntity에 jobDiv=SEA가 세팅된다")
    void applyCommonFields_seaDomain_setsJobDivSea() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();

        domainToJpaMapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo(JobDiv.SEA);
    }

    @Test
    @DisplayName("applyCommonFields: TRUCK domain → HouseBlJpaEntity에 jobDiv=TRUCK이 세팅된다")
    void applyCommonFields_truckDomain_setsJobDivTruck() {
        HouseBlTruck domain = HouseBlTruck.create(Bound.EXP);
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();

        domainToJpaMapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo(JobDiv.TRUCK);
    }

    @Test
    @DisplayName("applyCommonFields: NON_BL domain → HouseBlJpaEntity에 jobDiv=NON_BL이 세팅된다")
    void applyCommonFields_nonBlDomain_setsJobDivNonBl() {
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();

        domainToJpaMapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo(JobDiv.NON_BL);
    }

    // ── applySeaFields ───────────────────────────────────────────────

    @Test
    @DisplayName("applySeaFields: isTriangle 게터가 정상 동작한다 (회귀 검출)")
    void applySeaFields_triangleFlagsAreMapped() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);
        HouseBlSeaJpaEntity jpa = new HouseBlSeaJpaEntity();

        domainToJpaMapper.applySeaFields(domain, jpa);

        assertThat(jpa.isTriangle()).isFalse();
    }

    // ── applyTruckFields ─────────────────────────────────────────────

    @Test
    @DisplayName("applyTruckFields: vesselName이 TRUCK으로 복사된다")
    void applyTruckFields_vesselNameIsTruck() {
        HouseBlTruck domain = HouseBlTruck.create(Bound.EXP);
        HouseBlTruckJpaEntity jpa = new HouseBlTruckJpaEntity();

        domainToJpaMapper.applyTruckFields(domain, jpa);

        assertThat(jpa.getVesselName()).isEqualTo("TRUCK");
    }

    // ── applyNonBlFields ─────────────────────────────────────────────

    @Test
    @DisplayName("applyNonBlFields: workDivision이 매핑된다")
    void applyNonBlFields_workDivisionIsMapped() {
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        HouseBlNonBlJpaEntity jpa = new HouseBlNonBlJpaEntity();

        domainToJpaMapper.applyNonBlFields(domain, jpa);

        assertThat(jpa.getWorkDivision()).isEqualTo(HouseBlNonBl.WorkDivision.SEA);
    }

    // ── JPA → Domain ────────────────────────────────────────────────

    @Test
    @DisplayName("toDomain: AIR JPA → 핵심 항공 필드가 도메인으로 복사된다")
    void toDomain_airJpa_coreAirFieldsAreMapped() {
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setJobDiv(JobDiv.AIR);
        parentJpa.setBound(Bound.EXP);
        parentJpa.setShipperCode("SHIPPER01");
        parentJpa.setConsigneeCode("CONSIGNEE01");
        parentJpa.setPkgQty(10);
        parentJpa.setGrossWeightKg(BigDecimal.valueOf(250.0));
        HouseBlAir domain = jpaToDomainMapper.toAirDomain(parentJpa, null);

        assertThat(domain).isInstanceOf(HouseBlAir.class);
        assertThat(domain.getShipperCode().value()).isEqualTo("SHIPPER01");
        assertThat(domain.getConsigneeCode().value()).isEqualTo("CONSIGNEE01");
        assertThat(domain.getPkgQty().count()).isEqualTo(10);
        assertThat(domain.getGrossWeightKg().kg()).isEqualByComparingTo(BigDecimal.valueOf(250.0));
    }

    @Test
    @DisplayName("toDomain: SEA JPA + seaExt null → 해상 공통 필드가 복사된다")
    void toDomain_seaJpa_coreSeaFieldsAreMapped() {
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setJobDiv(JobDiv.SEA);
        parentJpa.setBound(Bound.EXP);
        parentJpa.setHblNo("HBL-SEA-001");
        parentJpa.setShipperCode("SHIPPER02");
        parentJpa.setPkgQty(5);
        parentJpa.setCbm(BigDecimal.valueOf(30.0));

        HouseBlSea domain = jpaToDomainMapper.toSeaDomain(parentJpa, null);

        assertThat(domain).isInstanceOf(HouseBlSea.class);
        assertThat(domain.getHblNo().value()).isEqualTo("HBL-SEA-001");
        assertThat(domain.getShipperCode().value()).isEqualTo("SHIPPER02");
        assertThat(domain.getPkgQty().count()).isEqualTo(5);
        assertThat(domain.getCbm().cbm()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
        assertThat(domain.getContainers()).isEmpty();
    }

    @Test
    @DisplayName("toDomain: TRUCK JPA → HouseBlTruck 반환된다")
    void toDomain_truckJpa_producesTruckDomain() {
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setJobDiv(JobDiv.TRUCK);
        parentJpa.setBound(Bound.EXP);

        HouseBl domain = jpaToDomainMapper.toTruckDomain(parentJpa, null);

        assertThat(domain).isInstanceOf(HouseBlTruck.class);
    }

    @Test
    @DisplayName("toDomain: NON_BL JPA + nonBlExt null → workDivision null 허용")
    void toDomain_nonBlJpa_withNullExt_workDivisionIsNull() {
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setJobDiv(JobDiv.NON_BL);
        parentJpa.setBound(Bound.EXP);

        HouseBl domain = jpaToDomainMapper.toNonBlDomain(parentJpa, null);

        assertThat(domain).isInstanceOf(HouseBlNonBl.class);
        assertThat(((HouseBlNonBl) domain).getWorkDivision()).isNull();
    }

    @Test
    @DisplayName("toDomain: assignIdentity로 houseBlId가 도메인에 주입된다")
    void toDomain_airJpa_identityFieldIsAssigned() {
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setJobDiv(JobDiv.AIR);
        parentJpa.setBound(Bound.EXP);
        parentJpa.setHouseBlId(42L);

        HouseBlAir domain = jpaToDomainMapper.toAirDomain(parentJpa, null);

        assertThat(domain.getId()).isEqualTo(42L);
    }

    // ── E-12 DIM ────────────────────────────────────────────────────

    @Test
    @DisplayName("toDimDomain: 모든 치수 필드가 도메인으로 복사된다")
    void toDimDomain_mapsAllFields() {
        HouseBlDimJpaEntity dimJpa = new HouseBlDimJpaEntity();
        dimJpa.setHouseBlId(1L);
        dimJpa.setLengthCm(BigDecimal.valueOf(50.0));
        dimJpa.setWidthCm(BigDecimal.valueOf(40.0));
        dimJpa.setHeightCm(BigDecimal.valueOf(30.0));
        dimJpa.setQuantity(2);
        dimJpa.setCbm(BigDecimal.valueOf(0.06));
        dimJpa.setVolumeWeightKg(BigDecimal.valueOf(10.0));

        HouseBlDim domain = cargoMapper.toDimDomain(dimJpa);

        assertThat(domain.getHouseBlId()).isEqualTo(1L);
        assertThat(domain.getLengthCm()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(domain.getWidthCm()).isEqualByComparingTo(BigDecimal.valueOf(40.0));
        assertThat(domain.getHeightCm()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
        assertThat(domain.getQuantity()).isEqualTo(2);
        assertThat(domain.getCbm()).isEqualByComparingTo(BigDecimal.valueOf(0.06));
        assertThat(domain.getVolumeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
    }

    @Test
    @DisplayName("applyDimFields: 도메인 → JPA 모든 치수 필드가 세팅된다")
    void applyDimFields_setsAllFieldsToJpa() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlDim domain = HouseBlDim.create(1L,
                BigDecimal.valueOf(50.0), BigDecimal.valueOf(40.0), BigDecimal.valueOf(30.0),
                2, BigDecimal.valueOf(0.06), BigDecimal.valueOf(10.0));
        HouseBlDimJpaEntity jpa = new HouseBlDimJpaEntity();

        cargoMapper.applyDimFields(domain, jpa, houseBlJpa);

        assertThat(jpa.getLengthCm()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(jpa.getWidthCm()).isEqualByComparingTo(BigDecimal.valueOf(40.0));
        assertThat(jpa.getHeightCm()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
        assertThat(jpa.getQuantity()).isEqualTo(2);
        assertThat(jpa.getCbm()).isEqualByComparingTo(BigDecimal.valueOf(0.06));
        assertThat(jpa.getVolumeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
    }

    @Test
    @DisplayName("toDimDomainList: 2개 JPA 엔티티 → 크기 2인 도메인 리스트 반환")
    void toDimDomainList_returnsCorrectSize() {
        HouseBlDimJpaEntity dim1 = new HouseBlDimJpaEntity();
        dim1.setHouseBlId(1L);
        HouseBlDimJpaEntity dim2 = new HouseBlDimJpaEntity();
        dim2.setHouseBlId(1L);

        List<HouseBlDim> result = cargoMapper.toDimDomainList(List.of(dim1, dim2));

        assertThat(result).hasSize(2);
    }

    // ── E-13 DESC ───────────────────────────────────────────────────

    @Test
    @DisplayName("toDescDomain: marks/description/descClause/remark 필드가 도메인으로 복사된다")
    void toDescDomain_mapsAllTextFields() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlDescJpaEntity descJpa = new HouseBlDescJpaEntity();
        descJpa.setHouseBl(houseBlJpa);
        descJpa.setMarks("MARKS");
        descJpa.setDescription("DESCRIPTION");
        descJpa.setDescClause1(DescClause1.A);
        descJpa.setDescClause2(DescClause2.A);
        descJpa.setRemark("REMARK TEXT");

        HouseBlDesc domain = docMapper.toDescDomain(descJpa);

        assertThat(domain.getHouseBlId()).isEqualTo(1L);
        assertThat(domain.getMarks()).isEqualTo("MARKS");
        assertThat(domain.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(domain.getDescClause1()).isEqualTo(DescClause1.A);
        assertThat(domain.getRemark()).isEqualTo("REMARK TEXT");
    }

    @Test
    @DisplayName("applyDescFields: 도메인 → JPA 모든 텍스트 필드가 세팅된다")
    void applyDescFields_setsAllTextFieldsToJpa() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlDesc domain = HouseBlDesc.create(1L);
        domain.updateContent("MARKS", "DESCRIPTION", DescClause1.A, DescClause2.A, "REMARK TEXT");
        HouseBlDescJpaEntity jpa = new HouseBlDescJpaEntity();

        docMapper.applyDescFields(domain, jpa, houseBlJpa);

        assertThat(jpa.getMarks()).isEqualTo("MARKS");
        assertThat(jpa.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(jpa.getDescClause1()).isEqualTo(DescClause1.A);
        assertThat(jpa.getRemark()).isEqualTo("REMARK TEXT");
        assertThat(jpa.getHouseBl()).isEqualTo(houseBlJpa);
    }

    @Test
    @DisplayName("toDescDomain(Optional.empty): Optional이 비어있으면 empty Optional 반환")
    void toDescDomain_optionalEmpty_returnsEmpty() {
        Optional<HouseBlDesc> result = docMapper.toDescDomain(Optional.empty());

        assertThat(result.isEmpty()).isTrue();
    }

    // ── E-19 SCHEDULE LEG ───────────────────────────────────────────

    @Test
    @DisplayName("toScheduleLegDomain: 항공 스케줄 레그 모든 필드가 도메인으로 복사된다")
    void toScheduleLegDomain_mapsAllFields() {
        HouseBlScheduleLegJpaEntity legJpa = new HouseBlScheduleLegJpaEntity();
        legJpa.setHouseBlId(1L);
        legJpa.setToCode("NRT");
        legJpa.setByCarrier("OZ");
        legJpa.setFlightNo("OZ102");
        legJpa.setOnBoardDt("20240315");
        legJpa.setOnBoardTm("1800");
        legJpa.setArrivalDt("20240316");
        legJpa.setArrivalTm("0700");

        HouseBlScheduleLeg domain = docMapper.toScheduleLegDomain(legJpa);

        assertThat(domain.getToCode()).isEqualTo("NRT");
        assertThat(domain.getByCarrier()).isEqualTo("OZ");
        assertThat(domain.getFlightNo()).isEqualTo("OZ102");
        assertThat(domain.getOnBoardDt()).isEqualTo("20240315");
        assertThat(domain.getOnBoardTm()).isEqualTo("1800");
        assertThat(domain.getArrivalDt()).isEqualTo("20240316");
        assertThat(domain.getArrivalTm()).isEqualTo("0700");
    }

    @Test
    @DisplayName("applyScheduleLegFields: 도메인 → JPA 모든 스케줄 레그 필드가 세팅된다")
    void applyScheduleLegFields_setsAllFieldsToJpa() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlScheduleLeg domain = HouseBlScheduleLeg.create(1L, "NRT", "20240315", "20240316");
        domain.updateDetails("NRT", "OZ", "OZ102", "20240315", "1800", "20240316", "0700");
        HouseBlScheduleLegJpaEntity jpa = new HouseBlScheduleLegJpaEntity();

        docMapper.applyScheduleLegFields(domain, jpa, houseBlJpa);

        assertThat(jpa.getToCode()).isEqualTo("NRT");
        assertThat(jpa.getByCarrier()).isEqualTo("OZ");
        assertThat(jpa.getFlightNo()).isEqualTo("OZ102");
        assertThat(jpa.getOnBoardDt()).isEqualTo("20240315");
        assertThat(jpa.getOnBoardTm()).isEqualTo("1800");
        assertThat(jpa.getArrivalDt()).isEqualTo("20240316");
        assertThat(jpa.getArrivalTm()).isEqualTo("0700");
    }

    @Test
    @DisplayName("toScheduleLegDomainList: 2개 JPA 엔티티 → 크기 2인 도메인 리스트 반환")
    void toScheduleLegDomainList_returnsCorrectSize() {
        HouseBlScheduleLegJpaEntity leg1 = new HouseBlScheduleLegJpaEntity();
        leg1.setHouseBlId(1L);
        leg1.setToCode("NRT");
        leg1.setOnBoardDt("20240315");
        leg1.setArrivalDt("20240316");
        HouseBlScheduleLegJpaEntity leg2 = new HouseBlScheduleLegJpaEntity();
        leg2.setHouseBlId(1L);
        leg2.setToCode("LAX");
        leg2.setOnBoardDt("20240317");
        leg2.setArrivalDt("20240318");

        List<HouseBlScheduleLeg> result = docMapper.toScheduleLegDomainList(List.of(leg1, leg2));

        assertThat(result).hasSize(2);
    }

    // ── applySeaFields vesselVoyage 분기 ────────────────────────────

    @Test
    @DisplayName("applySeaFields: vesselVoyage==null 이면 JPA vesselName/voyageNo를 세팅하지 않는다")
    void applySeaFields_vesselVoyageNull_doesNotSetVesselFields() {
        // HouseBlSea.create() 후 vesselVoyage를 null 상태로 둔다
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);
        HouseBlSeaJpaEntity jpa = new HouseBlSeaJpaEntity();

        domainToJpaMapper.applySeaFields(domain, jpa);

        // vesselVoyage == null 이므로 if 블록 미진입 → JPA 필드는 초기값(null) 유지
        assertThat(jpa.getVesselName()).isNull();
        assertThat(jpa.getVoyageNo()).isNull();
    }

    @Test
    @DisplayName("applySeaFields: vesselVoyage != null 이면 JPA vesselName/voyageNo가 세팅된다")
    void applySeaFields_vesselVoyageNonNull_setsVesselFields() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);
        domain.updateSeaSchedule(null,
                VesselVoyage.of(null, "MSC OSCAR", "V001W"),
                null);
        HouseBlSeaJpaEntity jpa = new HouseBlSeaJpaEntity();

        domainToJpaMapper.applySeaFields(domain, jpa);

        assertThat(jpa.getVesselName()).isEqualTo("MSC OSCAR");
        assertThat(jpa.getVoyageNo()).isEqualTo("V001W");
    }

    // ── applyAirFields handlingInformation 분기 ──────────────────────

    @Test
    @DisplayName("applyAirFields: handlingInformation==null 이면 JPA handlingInfoCode/Text를 null로 세팅한다")
    void applyAirFields_handlingInformationNull_setsNullOnJpa() {
        // HouseBlAir.create() 기본값: handlingInformation == null
        HouseBlAir domain = HouseBlAir.create(Bound.EXP);
        HouseBlAirJpaEntity jpa = new HouseBlAirJpaEntity();

        domainToJpaMapper.applyAirFields(domain, jpa);

        assertThat(jpa.getHandlingInfoCode()).isNull();
        assertThat(jpa.getHandlingInfoText()).isNull();
    }

    // ── applyTruckFields vesselVoyage 분기 ───────────────────────────

    @Test
    @DisplayName("applyTruckFields: create() 기본 상태(vesselVoyage.vesselName=TRUCK)에서 JPA vesselName은 TRUCK으로 세팅된다")
    void applyTruckFields_vesselVoyageNull_setsTruckAsVesselName() {
        HouseBlTruck domain = HouseBlTruck.create(Bound.EXP);
        // create() 시 vesselVoyage = VesselVoyage.of(null, "TRUCK", null) 이므로
        // mapper의 vv != null 분기를 통해 vesselName = "TRUCK" 이 세팅된다
        HouseBlTruckJpaEntity jpa = new HouseBlTruckJpaEntity();

        domainToJpaMapper.applyTruckFields(domain, jpa);

        assertThat(jpa.getVesselName()).isEqualTo("TRUCK");
        assertThat(jpa.getVoyageNo()).isNull();
    }

    @Test
    @DisplayName("applyTruckFields: updateTruckFields 호출 시 vesselName은 도메인 정책에 따라 항상 TRUCK으로 강제된다")
    void applyTruckFields_vesselVoyageNonNull_overridesVesselNameToTruck() {
        HouseBlTruck domain = HouseBlTruck.create(Bound.EXP);
        // updateTruckFields는 내부적으로 VesselVoyage.of(null, "TRUCK", voyageNo) 로 고정 세팅
        domain.updateTruckFields(new HouseBlTruck.TruckFields(
                VesselVoyage.of(null, "IGNORED", "T-001"),
                null, null, null, null, null, null, null, null, null));
        HouseBlTruckJpaEntity jpa = new HouseBlTruckJpaEntity();

        domainToJpaMapper.applyTruckFields(domain, jpa);

        // updateTruckFields가 vesselName을 "TRUCK"으로 강제하므로 JPA도 "TRUCK"
        assertThat(jpa.getVesselName()).isEqualTo("TRUCK");
        assertThat(jpa.getVoyageNo()).isEqualTo("T-001");
    }

    // ── applyCommonFields masterBlId 분기 ────────────────────────────

    @Test
    @DisplayName("copyBaseFields: masterBlId==null 이면 JPA masterBlId를 세팅하지 않는다")
    void copyBaseFields_masterBlIdNull_doesNotSetMasterBlId() {
        HouseBlAir domain = HouseBlAir.create(Bound.EXP);
        // domain.getMasterBlId() == null (linkToMaster 미호출)
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();

        domainToJpaMapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getMasterBlId()).isNull();
    }

    // ── toNonBlDomain round-trip NON_BL 특화 필드 ───────────────────

    @Test
    @DisplayName("toNonBlDomain: originalBlRef/rton/volumeWtKg 필드가 round-trip으로 복원된다")
    void toNonBlDomain_roundTrip_preservesOriginalBlRefRtonVolumeWtKg() {
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setJobDiv(JobDiv.NON_BL);
        parentJpa.setBound(Bound.EXP);

        HouseBlNonBlJpaEntity nonBlJpa = new HouseBlNonBlJpaEntity();
        nonBlJpa.setWorkDivision(HouseBlNonBl.WorkDivision.SEA);
        nonBlJpa.setOriginalBlRef("REF-001");
        nonBlJpa.setRton(BigDecimal.valueOf(12.5));
        nonBlJpa.setVolumeWtKg(BigDecimal.valueOf(100.0));

        HouseBlNonBl domain = jpaToDomainMapper.toNonBlDomain(parentJpa, nonBlJpa);

        assertThat(domain.getOriginalBlRef().value()).isEqualTo("REF-001");
        assertThat(domain.getRton().ton()).isEqualByComparingTo(BigDecimal.valueOf(12.5));
        assertThat(domain.getVolumeWtKg().kg()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
    }
}
