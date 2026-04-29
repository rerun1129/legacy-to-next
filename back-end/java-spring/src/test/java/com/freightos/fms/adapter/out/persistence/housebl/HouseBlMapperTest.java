package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.Bound;
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

    private final HouseBlMapper mapper = new HouseBlMapper();

    // ── applyCommonFields ────────────────────────────────────────────

    @Test
    @DisplayName("applyCommonFields: AIR domain → HouseBlJpaEntity에 jobDiv=AIR, 기본값 필드 복사된다")
    void applyCommonFields_airDomain_setsJobDivAir() {
        HouseBlAir domain = HouseBlAir.create(Bound.EXP);
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();

        mapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo(JobDiv.AIR);
        assertThat(jpa.getBound()).isEqualTo(Bound.EXP);
    }

    @Test
    @DisplayName("applyCommonFields: SEA domain → HouseBlJpaEntity에 jobDiv=SEA가 세팅된다")
    void applyCommonFields_seaDomain_setsJobDivSea() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();

        mapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo(JobDiv.SEA);
    }

    @Test
    @DisplayName("applyCommonFields: TRUCK domain → HouseBlJpaEntity에 jobDiv=TRUCK이 세팅된다")
    void applyCommonFields_truckDomain_setsJobDivTruck() {
        HouseBlTruck domain = HouseBlTruck.create(Bound.EXP);
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();

        mapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo(JobDiv.TRUCK);
    }

    @Test
    @DisplayName("applyCommonFields: NON_BL domain → HouseBlJpaEntity에 jobDiv=NON_BL이 세팅된다")
    void applyCommonFields_nonBlDomain_setsJobDivNonBl() {
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();

        mapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo(JobDiv.NON_BL);
    }

    // ── applySeaFields ───────────────────────────────────────────────

    @Test
    @DisplayName("applySeaFields: isTriangle/isCoLoad 게터가 정상 동작한다 (회귀 검출)")
    void applySeaFields_triangleFlagsAreMapped() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);
        HouseBlSeaJpaEntity jpa = new HouseBlSeaJpaEntity();

        mapper.applySeaFields(domain, jpa);

        assertThat(jpa.isTriangle()).isFalse();
        assertThat(jpa.isCoLoad()).isFalse();
    }

    // ── applyTruckFields ─────────────────────────────────────────────

    @Test
    @DisplayName("applyTruckFields: vesselName이 TRUCK으로 복사된다")
    void applyTruckFields_vesselNameIsTruck() {
        HouseBlTruck domain = HouseBlTruck.create(Bound.EXP);
        HouseBlTruckJpaEntity jpa = new HouseBlTruckJpaEntity();

        mapper.applyTruckFields(domain, jpa);

        assertThat(jpa.getVesselName()).isEqualTo("TRUCK");
    }

    // ── applyNonBlFields ─────────────────────────────────────────────

    @Test
    @DisplayName("applyNonBlFields: workDivision이 매핑된다")
    void applyNonBlFields_workDivisionIsMapped() {
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        HouseBlNonBlJpaEntity jpa = new HouseBlNonBlJpaEntity();

        mapper.applyNonBlFields(domain, jpa);

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
        HouseBlAir domain = mapper.toAirDomain(parentJpa, null);

        assertThat(domain).isInstanceOf(HouseBlAir.class);
        assertThat(domain.getShipperCode().value()).isEqualTo("SHIPPER01");
        assertThat(domain.getConsigneeCode().value()).isEqualTo("CONSIGNEE01");
        assertThat(domain.getPkgQty().count()).isEqualTo(10);
        assertThat(domain.getGrossWeightKg().kg()).isEqualByComparingTo(BigDecimal.valueOf(250.0));
        // create() 기본값 유지
        assertThat(domain.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(domain.getInsurance()).isEqualTo("NIL");
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

        HouseBlSea domain = mapper.toSeaDomain(parentJpa, null);

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

        HouseBl domain = mapper.toTruckDomain(parentJpa, null);

        assertThat(domain).isInstanceOf(HouseBlTruck.class);
    }

    @Test
    @DisplayName("toDomain: NON_BL JPA + nonBlExt null → workDivision null 허용")
    void toDomain_nonBlJpa_withNullExt_workDivisionIsNull() {
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setJobDiv(JobDiv.NON_BL);
        parentJpa.setBound(Bound.EXP);

        HouseBl domain = mapper.toNonBlDomain(parentJpa, null);

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

        HouseBlAir domain = mapper.toAirDomain(parentJpa, null);

        assertThat(domain.getId()).isEqualTo(42L);
    }

    // ── E-12 DIM ────────────────────────────────────────────────────

    @Test
    @DisplayName("toDimDomain: 모든 치수 필드가 도메인으로 복사된다")
    void toDimDomain_mapsAllFields() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlDimJpaEntity dimJpa = new HouseBlDimJpaEntity();
        dimJpa.setHouseBl(houseBlJpa);
        dimJpa.setLengthCm(BigDecimal.valueOf(50.0));
        dimJpa.setWidthCm(BigDecimal.valueOf(40.0));
        dimJpa.setHeightCm(BigDecimal.valueOf(30.0));
        dimJpa.setQuantity(2);
        dimJpa.setCbm(BigDecimal.valueOf(0.06));
        dimJpa.setVolumeWeightKg(BigDecimal.valueOf(10.0));
        dimJpa.setSeq(1);

        HouseBlDim domain = mapper.toDimDomain(dimJpa);

        assertThat(domain.getHouseBlId()).isEqualTo(1L);
        assertThat(domain.getLengthCm()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(domain.getWidthCm()).isEqualByComparingTo(BigDecimal.valueOf(40.0));
        assertThat(domain.getHeightCm()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
        assertThat(domain.getQuantity()).isEqualTo(2);
        assertThat(domain.getCbm()).isEqualByComparingTo(BigDecimal.valueOf(0.06));
        assertThat(domain.getVolumeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
        assertThat(domain.getSeq()).isEqualTo(1);
    }

    @Test
    @DisplayName("applyDimFields: 도메인 → JPA 모든 치수 필드가 세팅된다")
    void applyDimFields_setsAllFieldsToJpa() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlDim domain = HouseBlDim.create(1L,
                BigDecimal.valueOf(50.0), BigDecimal.valueOf(40.0), BigDecimal.valueOf(30.0),
                2, BigDecimal.valueOf(0.06), BigDecimal.valueOf(10.0), 1);
        HouseBlDimJpaEntity jpa = new HouseBlDimJpaEntity();

        mapper.applyDimFields(domain, jpa, houseBlJpa);

        assertThat(jpa.getLengthCm()).isEqualByComparingTo(BigDecimal.valueOf(50.0));
        assertThat(jpa.getWidthCm()).isEqualByComparingTo(BigDecimal.valueOf(40.0));
        assertThat(jpa.getHeightCm()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
        assertThat(jpa.getQuantity()).isEqualTo(2);
        assertThat(jpa.getCbm()).isEqualByComparingTo(BigDecimal.valueOf(0.06));
        assertThat(jpa.getVolumeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(10.0));
        assertThat(jpa.getSeq()).isEqualTo(1);
        assertThat(jpa.getHouseBl()).isEqualTo(houseBlJpa);
    }

    @Test
    @DisplayName("toDimDomainList: 2개 JPA 엔티티 → 크기 2인 도메인 리스트 반환")
    void toDimDomainList_returnsCorrectSize() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlDimJpaEntity dim1 = new HouseBlDimJpaEntity();
        dim1.setHouseBl(houseBlJpa);
        dim1.setSeq(1);
        HouseBlDimJpaEntity dim2 = new HouseBlDimJpaEntity();
        dim2.setHouseBl(houseBlJpa);
        dim2.setSeq(2);

        List<HouseBlDim> result = mapper.toDimDomainList(List.of(dim1, dim2));

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
        descJpa.setMarksLeft("MARKS-LEFT");
        descJpa.setMarksRight("MARKS-RIGHT");
        descJpa.setDescriptionLeft("DESC-LEFT");
        descJpa.setDescriptionRight("DESC-RIGHT");
        descJpa.setDescClause1("CLAUSE1");
        descJpa.setDescClause2("CLAUSE2");
        descJpa.setRemark("REMARK TEXT");

        HouseBlDesc domain = mapper.toDescDomain(descJpa);

        assertThat(domain.getHouseBlId()).isEqualTo(1L);
        assertThat(domain.getMarksLeft()).isEqualTo("MARKS-LEFT");
        assertThat(domain.getDescriptionLeft()).isEqualTo("DESC-LEFT");
        assertThat(domain.getDescClause1()).isEqualTo("CLAUSE1");
        assertThat(domain.getRemark()).isEqualTo("REMARK TEXT");
    }

    @Test
    @DisplayName("applyDescFields: 도메인 → JPA 모든 텍스트 필드가 세팅된다")
    void applyDescFields_setsAllTextFieldsToJpa() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlDesc domain = HouseBlDesc.create(1L);
        domain.updateContent("MARKS-LEFT", "MARKS-RIGHT", "DESC-LEFT", "DESC-RIGHT",
                "CLAUSE1", "CLAUSE2", "REMARK TEXT");
        HouseBlDescJpaEntity jpa = new HouseBlDescJpaEntity();

        mapper.applyDescFields(domain, jpa, houseBlJpa);

        assertThat(jpa.getMarksLeft()).isEqualTo("MARKS-LEFT");
        assertThat(jpa.getDescriptionLeft()).isEqualTo("DESC-LEFT");
        assertThat(jpa.getDescClause1()).isEqualTo("CLAUSE1");
        assertThat(jpa.getRemark()).isEqualTo("REMARK TEXT");
        assertThat(jpa.getHouseBl()).isEqualTo(houseBlJpa);
    }

    @Test
    @DisplayName("toDescDomain(Optional.empty): Optional이 비어있으면 empty Optional 반환")
    void toDescDomain_optionalEmpty_returnsEmpty() {
        Optional<HouseBlDesc> result = mapper.toDescDomain(Optional.empty());

        assertThat(result.isEmpty()).isTrue();
    }

    // ── E-17 LICENSE ────────────────────────────────────────────────

    @Test
    @DisplayName("toLicenseDomain: 수출면장 모든 필드가 도메인으로 복사된다")
    void toLicenseDomain_mapsAllFields() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlLicenseJpaEntity licJpa = new HouseBlLicenseJpaEntity();
        licJpa.setHouseBl(houseBlJpa);
        licJpa.setLicenseNo("LC-001");
        licJpa.setPkgQty(10);
        licJpa.setPkgUnit("CTN");
        licJpa.setGrossWeightKg(BigDecimal.valueOf(100.0));
        licJpa.setPartialShipment(true);
        licJpa.setPartialShipmentSeq(2);
        licJpa.setHsnNo("1234.56");
        licJpa.setSeq(1);

        HouseBlLicense domain = mapper.toLicenseDomain(licJpa);

        assertThat(domain.getLicenseNo()).isEqualTo("LC-001");
        assertThat(domain.getPkgQty()).isEqualTo(10);
        assertThat(domain.isPartialShipment()).isTrue();
        assertThat(domain.getPartialShipmentSeq()).isEqualTo(2);
        assertThat(domain.getSeq()).isEqualTo(1);
    }

    @Test
    @DisplayName("applyLicenseFields: 도메인 → JPA 모든 수출면장 필드가 세팅된다")
    void applyLicenseFields_setsAllFieldsToJpa() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlLicense domain = HouseBlLicense.create(1L, 1);
        domain.updateDetails("LC-001", 10, "CTN", BigDecimal.valueOf(100.0),
                null, null, null, true, 2, "1234.56");
        HouseBlLicenseJpaEntity jpa = new HouseBlLicenseJpaEntity();

        mapper.applyLicenseFields(domain, jpa, houseBlJpa);

        assertThat(jpa.getLicenseNo()).isEqualTo("LC-001");
        assertThat(jpa.getPkgQty()).isEqualTo(10);
        assertThat(jpa.getPkgUnit()).isEqualTo("CTN");
        assertThat(jpa.getGrossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
        assertThat(jpa.isPartialShipment()).isTrue();
        assertThat(jpa.getPartialShipmentSeq()).isEqualTo(2);
        assertThat(jpa.getHsnNo()).isEqualTo("1234.56");
        assertThat(jpa.getSeq()).isEqualTo(1);
        assertThat(jpa.getHouseBl()).isEqualTo(houseBlJpa);
    }

    @Test
    @DisplayName("toLicenseDomainList: 3개 JPA 엔티티 → 크기 3인 도메인 리스트 반환")
    void toLicenseDomainList_returnsCorrectSize() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlLicenseJpaEntity lic1 = new HouseBlLicenseJpaEntity();
        lic1.setHouseBl(houseBlJpa);
        lic1.setSeq(1);
        HouseBlLicenseJpaEntity lic2 = new HouseBlLicenseJpaEntity();
        lic2.setHouseBl(houseBlJpa);
        lic2.setSeq(2);
        HouseBlLicenseJpaEntity lic3 = new HouseBlLicenseJpaEntity();
        lic3.setHouseBl(houseBlJpa);
        lic3.setSeq(3);

        List<HouseBlLicense> result = mapper.toLicenseDomainList(List.of(lic1, lic2, lic3));

        assertThat(result).hasSize(3);
    }

    // ── E-18 REFERENCE ──────────────────────────────────────────────

    @Test
    @DisplayName("toReferenceDomain: referenceType / referenceNo가 도메인으로 복사된다")
    void toReferenceDomain_mapsTypeAndNo() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlReferenceJpaEntity refJpa = new HouseBlReferenceJpaEntity();
        refJpa.setHouseBl(houseBlJpa);
        refJpa.setReferenceType("PO");
        refJpa.setReferenceNo("PO-20240310-001");
        refJpa.setSeq(1);

        HouseBlReference domain = mapper.toReferenceDomain(refJpa);

        assertThat(domain.getReferenceType()).isEqualTo("PO");
        assertThat(domain.getReferenceNo()).isEqualTo("PO-20240310-001");
    }

    @Test
    @DisplayName("applyReferenceFields: 도메인 → JPA referenceType / referenceNo가 세팅된다")
    void applyReferenceFields_setsTypeAndNoToJpa() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlReference domain = HouseBlReference.create(1L, "PO", "PO-001", 1);
        HouseBlReferenceJpaEntity jpa = new HouseBlReferenceJpaEntity();

        mapper.applyReferenceFields(domain, jpa, houseBlJpa);

        assertThat(jpa.getReferenceType()).isEqualTo("PO");
        assertThat(jpa.getReferenceNo()).isEqualTo("PO-001");
        assertThat(jpa.getHouseBl()).isEqualTo(houseBlJpa);
    }

    // ── E-19 SCHEDULE LEG ───────────────────────────────────────────

    @Test
    @DisplayName("toScheduleLegDomain: 항공 스케줄 레그 모든 필드가 도메인으로 복사된다")
    void toScheduleLegDomain_mapsAllFields() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlScheduleLegJpaEntity legJpa = new HouseBlScheduleLegJpaEntity();
        legJpa.setHouseBl(houseBlJpa);
        legJpa.setToCode("NRT");
        legJpa.setByCarrier("OZ");
        legJpa.setFlightNo("OZ102");
        legJpa.setOnBoardDt("20240315");
        legJpa.setOnBoardTm("1800");
        legJpa.setArrivalDt("20240316");
        legJpa.setArrivalTm("0700");
        legJpa.setSeq(2);

        HouseBlScheduleLeg domain = mapper.toScheduleLegDomain(legJpa);

        assertThat(domain.getToCode()).isEqualTo("NRT");
        assertThat(domain.getByCarrier()).isEqualTo("OZ");
        assertThat(domain.getFlightNo()).isEqualTo("OZ102");
        assertThat(domain.getOnBoardDt()).isEqualTo("20240315");
        assertThat(domain.getOnBoardTm()).isEqualTo("1800");
        assertThat(domain.getArrivalDt()).isEqualTo("20240316");
        assertThat(domain.getArrivalTm()).isEqualTo("0700");
        assertThat(domain.getSeq()).isEqualTo(2);
    }

    @Test
    @DisplayName("applyScheduleLegFields: 도메인 → JPA 모든 스케줄 레그 필드가 세팅된다")
    void applyScheduleLegFields_setsAllFieldsToJpa() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlScheduleLeg domain = HouseBlScheduleLeg.create(1L, "NRT", "20240315", "20240316", 2);
        domain.updateDetails("NRT", "OZ", "OZ102", "20240315", "1800", "20240316", "0700");
        HouseBlScheduleLegJpaEntity jpa = new HouseBlScheduleLegJpaEntity();

        mapper.applyScheduleLegFields(domain, jpa, houseBlJpa);

        assertThat(jpa.getToCode()).isEqualTo("NRT");
        assertThat(jpa.getByCarrier()).isEqualTo("OZ");
        assertThat(jpa.getFlightNo()).isEqualTo("OZ102");
        assertThat(jpa.getOnBoardDt()).isEqualTo("20240315");
        assertThat(jpa.getOnBoardTm()).isEqualTo("1800");
        assertThat(jpa.getArrivalDt()).isEqualTo("20240316");
        assertThat(jpa.getArrivalTm()).isEqualTo("0700");
        assertThat(jpa.getSeq()).isEqualTo(2);
        assertThat(jpa.getHouseBl()).isEqualTo(houseBlJpa);
    }

    @Test
    @DisplayName("toScheduleLegDomainList: 2개 JPA 엔티티 → 크기 2인 도메인 리스트 반환")
    void toScheduleLegDomainList_returnsCorrectSize() {
        HouseBlJpaEntity houseBlJpa = new HouseBlJpaEntity();
        houseBlJpa.setHouseBlId(1L);

        HouseBlScheduleLegJpaEntity leg1 = new HouseBlScheduleLegJpaEntity();
        leg1.setHouseBl(houseBlJpa);
        leg1.setSeq(1);
        HouseBlScheduleLegJpaEntity leg2 = new HouseBlScheduleLegJpaEntity();
        leg2.setHouseBl(houseBlJpa);
        leg2.setSeq(2);

        List<HouseBlScheduleLeg> result = mapper.toScheduleLegDomainList(List.of(leg1, leg2));

        assertThat(result).hasSize(2);
    }
}
