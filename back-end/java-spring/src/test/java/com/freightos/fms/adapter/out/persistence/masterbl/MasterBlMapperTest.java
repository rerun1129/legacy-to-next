package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlScheduleLegJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FlightType;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.SecurityStatus;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
import com.freightos.fms.domain.masterbl.entity.MasterBlDim;
import com.freightos.fms.domain.masterbl.entity.MasterBlScheduleLeg;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MasterBl JPA 엔티티 구조 검증 테스트.
 * - JOINED 상속에서 @OneToOne 독립 엔티티 구조로 변경됨.
 * - Mapper(toDomain/toJpa) 검증은 Coder-4의 Mapper 리팩토링 완료 후 별도 갱신.
 */
class MasterBlMapperTest {

    private final MasterBlMapper mapper = new MasterBlMapper();

    // ── applyCommonFields / applySeaFields / applyAirFields ─────────

    @Test
    @DisplayName("applyCommonFields: SEA domain → parent JPA 에 jobDiv=SEA 가 세팅된다")
    void applyCommonFields_seaDomain_setsJobDivSea() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);
        MasterBlJpaEntity jpa = new MasterBlJpaEntity();

        mapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo("SEA");
        assertThat(jpa.getBound()).isEqualTo(Bound.EXP);
    }

    @Test
    @DisplayName("applyCommonFields: AIR domain → parent JPA 에 jobDiv=AIR 가 세팅된다")
    void applyCommonFields_airDomain_setsJobDivAir() {
        MasterBlAir domain = MasterBlAir.create(Bound.IMP);
        MasterBlJpaEntity jpa = new MasterBlJpaEntity();

        mapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo("AIR");
        assertThat(jpa.getBound()).isEqualTo(Bound.IMP);
    }

    @Test
    @DisplayName("applySeaFields: SEA 도메인 필드가 MasterBlSeaJpaEntity에 복사된다")
    void applySeaFields_copiesSeaFieldsToJpa() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);
        domain.updateSeaFields(null, LinerCode.of("MSC"), VesselVoyage.of("MSC OSCAR", "0012W"),
                BlDate.of("20240310"), BlNumber.of("BKG-001"), BlDate.of("20240301"));
        MasterBlSeaJpaEntity jpa = new MasterBlSeaJpaEntity();

        mapper.applySeaFields(domain, jpa);

        assertThat(jpa.getLinerCode()).isEqualTo("MSC");
        assertThat(jpa.getVesselName()).isEqualTo("MSC OSCAR");
        assertThat(jpa.getVoyageNo()).isEqualTo("0012W");
        assertThat(jpa.getOnboardDate()).isEqualTo("20240310");
        assertThat(jpa.getIssueDate()).isEqualTo("20240301");
    }

    @Test
    @DisplayName("applyAirFields: AIR 도메인 필드가 MasterBlAirJpaEntity에 복사된다")
    void applyAirFields_copiesAirFieldsToJpa() {
        MasterBlAir domain = MasterBlAir.create(Bound.EXP);
        domain.updateAirFields(new MasterBlAir.AirFields(
                AirlineCode.of("KE"), AirportCode.of("ICN"), BlNumber.of("180-12345678"),
                Weight.of(BigDecimal.valueOf(100.5)), Weight.of(BigDecimal.valueOf(90.0)),
                RateClass.Q, CurrencyCode.of("KRW"), "N.V.D.", null, "NIL", null, null, null,
                BlDate.of("20240301"), PortCode.of("Seoul"), "Signature"));
        MasterBlAirJpaEntity jpa = new MasterBlAirJpaEntity();

        mapper.applyAirFields(domain, jpa);

        assertThat(jpa.getAirlineCode()).isEqualTo("KE");
        assertThat(jpa.getMawbNo()).isEqualTo("180-12345678");
        assertThat(jpa.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(jpa.getInsurance()).isEqualTo("NIL");
        assertThat(jpa.getIssueDate()).isEqualTo("20240301");
    }

    // ── JPA → Domain ────────────────────────────────────────────────

    @Test
    @DisplayName("toDomain: jobDiv=SEA → MasterBlSea 반환, 공통 필드가 복사된다")
    void toDomain_seaJobDiv_producesSeaDomain() {
        MasterBlJpaEntity parentJpa = new MasterBlJpaEntity();
        parentJpa.setJobDiv("SEA");
        parentJpa.setBound(Bound.EXP);
        parentJpa.setMblNo("MBLNO-SEA-001");
        parentJpa.setShipperCode("SHIPPER-SEA");
        parentJpa.setPolCode("PUS");
        parentJpa.setPodCode("SYD");
        parentJpa.setPkgQty(20);
        parentJpa.setGrossWeightKg(BigDecimal.valueOf(5000.0));
        parentJpa.setCbm(BigDecimal.valueOf(25.0));
        parentJpa.setFreightTerm(FreightTerm.PREPAID);

        MasterBlSea domain = (MasterBlSea) mapper.toDomain(parentJpa);

        assertThat(domain).isInstanceOf(MasterBlSea.class);
        assertThat(domain.getMblNo().value()).isEqualTo("MBLNO-SEA-001");
        assertThat(domain.getShipperCode().value()).isEqualTo("SHIPPER-SEA");
        assertThat(domain.getPolCode().value()).isEqualTo("PUS");
        assertThat(domain.getPkgQty().count()).isEqualTo(20);
        assertThat(domain.getGrossWeightKg().kg()).isEqualByComparingTo(BigDecimal.valueOf(5000.0));
        assertThat(domain.getFreightTerm()).isEqualTo(FreightTerm.PREPAID);
    }

    @Test
    @DisplayName("toDomain: jobDiv=AIR + seaExt null → MasterBlAir 반환, 항공 필드 복사된다")
    void toDomain_airJobDiv_producesAirDomain() {
        MasterBlJpaEntity parentJpa = new MasterBlJpaEntity();
        parentJpa.setJobDiv("AIR");
        parentJpa.setBound(Bound.EXP);
        parentJpa.setMblNo("MAWB-001");
        parentJpa.setShipperCode("SHIPPER01");
        parentJpa.setPolCode("ICN");
        parentJpa.setPodCode("LAX");
        parentJpa.setFreightTerm(FreightTerm.PREPAID);
        parentJpa.setPkgQty(5);
        parentJpa.setGrossWeightKg(BigDecimal.valueOf(120.0));
        parentJpa.setCbm(BigDecimal.valueOf(2.5));
        // airExt null — seaExt/airExt 는 @OneToOne lazy, 직접 조립 불가

        MasterBlAir domain = (MasterBlAir) mapper.toDomain(parentJpa);

        assertThat(domain).isInstanceOf(MasterBlAir.class);
        assertThat(domain.getMblNo().value()).isEqualTo("MAWB-001");
        assertThat(domain.getShipperCode().value()).isEqualTo("SHIPPER01");
        assertThat(domain.getPolCode().value()).isEqualTo("ICN");
        assertThat(domain.getFreightTerm()).isEqualTo(FreightTerm.PREPAID);
        assertThat(domain.getPkgQty().count()).isEqualTo(5);
        // create() 기본값 유지 (airExt null 이므로 copyAirFields 미호출)
        assertThat(domain.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(domain.getInsurance()).isEqualTo("NIL");
    }

    @Test
    @DisplayName("toDomain: 알 수 없는 jobDiv → IllegalArgumentException")
    void toDomain_unknownJobDiv_throwsException() {
        MasterBlJpaEntity parentJpa = new MasterBlJpaEntity();
        parentJpa.setJobDiv("UNKNOWN");
        parentJpa.setBound(Bound.EXP);

        assertThatThrownBy(() -> mapper.toDomain(parentJpa))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown jobDiv: UNKNOWN");
    }

    // ── E-05 DIM ────────────────────────────────────────────────────────

    @Test
    @DisplayName("toDimDomain: JPA → 도메인 전체 필드가 복사된다")
    void toDimDomain_mapsAllFields() {
        MasterBlJpaEntity masterBlJpa = new MasterBlJpaEntity();
        masterBlJpa.setMasterBlId(1L);

        MasterBlDimJpaEntity dimJpa = new MasterBlDimJpaEntity();
        dimJpa.setMasterBl(masterBlJpa);
        dimJpa.setLengthCm(BigDecimal.valueOf(100.5));
        dimJpa.setWidthCm(BigDecimal.valueOf(80.0));
        dimJpa.setHeightCm(BigDecimal.valueOf(60.0));
        dimJpa.setQuantity(3);
        dimJpa.setCbm(BigDecimal.valueOf(0.485));
        dimJpa.setVolumeWeightKg(BigDecimal.valueOf(80.8));
        dimJpa.setSeq(1);

        MasterBlDim domain = mapper.toDimDomain(dimJpa);

        assertThat(domain.getLengthCm()).isEqualByComparingTo(BigDecimal.valueOf(100.5));
        assertThat(domain.getWidthCm()).isEqualByComparingTo(BigDecimal.valueOf(80.0));
        assertThat(domain.getHeightCm()).isEqualByComparingTo(BigDecimal.valueOf(60.0));
        assertThat(domain.getQuantity()).isEqualTo(3);
        assertThat(domain.getCbm()).isEqualByComparingTo(BigDecimal.valueOf(0.485));
        assertThat(domain.getVolumeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(80.8));
        assertThat(domain.getSeq()).isEqualTo(1);
    }

    @Test
    @DisplayName("applyDimFields: 도메인 → JPA 전체 필드가 세팅된다")
    void applyDimFields_setsAllFieldsToJpa() {
        MasterBlJpaEntity masterBlJpa = new MasterBlJpaEntity();
        masterBlJpa.setMasterBlId(1L);

        MasterBlDim domain = MasterBlDim.create(1L,
                BigDecimal.valueOf(100.5), BigDecimal.valueOf(80.0), BigDecimal.valueOf(60.0),
                3, BigDecimal.valueOf(0.485), BigDecimal.valueOf(80.8), 1);
        MasterBlDimJpaEntity dimJpa = new MasterBlDimJpaEntity();

        mapper.applyDimFields(domain, dimJpa, masterBlJpa);

        assertThat(dimJpa.getLengthCm()).isEqualByComparingTo(BigDecimal.valueOf(100.5));
        assertThat(dimJpa.getWidthCm()).isEqualByComparingTo(BigDecimal.valueOf(80.0));
        assertThat(dimJpa.getHeightCm()).isEqualByComparingTo(BigDecimal.valueOf(60.0));
        assertThat(dimJpa.getQuantity()).isEqualTo(3);
        assertThat(dimJpa.getCbm()).isEqualByComparingTo(BigDecimal.valueOf(0.485));
        assertThat(dimJpa.getVolumeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(80.8));
        assertThat(dimJpa.getSeq()).isEqualTo(1);
        assertThat(dimJpa.getMasterBl()).isSameAs(masterBlJpa);
    }

    @Test
    @DisplayName("toDimDomainList: JPA 엔티티 리스트 → 도메인 리스트로 변환된다")
    void toDimDomainList_convertsMultipleEntities() {
        MasterBlJpaEntity masterBlJpa = new MasterBlJpaEntity();
        masterBlJpa.setMasterBlId(1L);

        MasterBlDimJpaEntity dim1 = new MasterBlDimJpaEntity();
        dim1.setMasterBl(masterBlJpa);
        dim1.setSeq(1);

        MasterBlDimJpaEntity dim2 = new MasterBlDimJpaEntity();
        dim2.setMasterBl(masterBlJpa);
        dim2.setSeq(2);

        List<MasterBlDim> result = mapper.toDimDomainList(List.of(dim1, dim2));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("applyDimFields: null 허용 필드가 null이어도 NPE 없이 동작한다")
    void applyDimFields_nullValues_doesNotThrow() {
        MasterBlJpaEntity masterBlJpa = new MasterBlJpaEntity();
        masterBlJpa.setMasterBlId(1L);

        MasterBlDim domain = MasterBlDim.create(1L, null, null, null, null, null, null, 1);
        MasterBlDimJpaEntity dimJpa = new MasterBlDimJpaEntity();

        assertThatCode(() -> mapper.applyDimFields(domain, dimJpa, masterBlJpa))
                .doesNotThrowAnyException();
    }

    // ── E-06 DESC ────────────────────────────────────────────────────────

    @Test
    @DisplayName("toDescDomain: JPA → 도메인 텍스트 필드 전체가 복사된다")
    void toDescDomain_mapsAllTextFields() {
        MasterBlJpaEntity masterBlJpa = new MasterBlJpaEntity();
        masterBlJpa.setMasterBlId(2L);

        MasterBlDescJpaEntity descJpa = new MasterBlDescJpaEntity();
        descJpa.setMasterBl(masterBlJpa);
        descJpa.setMarks("MARKS");
        descJpa.setDescription("DESCRIPTION");
        descJpa.setDescClause1("CLAUSE1");
        descJpa.setDescClause2("CLAUSE2");
        descJpa.setRemark("REMARK");

        MasterBlDesc domain = mapper.toDescDomain(descJpa);

        assertThat(domain.getMarks()).isEqualTo("MARKS");
        assertThat(domain.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(domain.getDescClause1()).isEqualTo("CLAUSE1");
        assertThat(domain.getDescClause2()).isEqualTo("CLAUSE2");
        assertThat(domain.getRemark()).isEqualTo("REMARK");
    }

    @Test
    @DisplayName("applyDescFields: 도메인 → JPA 텍스트 필드 전체가 세팅된다")
    void applyDescFields_setsAllTextFieldsToJpa() {
        MasterBlJpaEntity masterBlJpa = new MasterBlJpaEntity();
        masterBlJpa.setMasterBlId(2L);

        MasterBlDesc domain = MasterBlDesc.create(2L);
        domain.updateContent("MARKS", "DESCRIPTION", "CLAUSE1", "CLAUSE2", "REMARK");
        MasterBlDescJpaEntity descJpa = new MasterBlDescJpaEntity();

        mapper.applyDescFields(domain, descJpa, masterBlJpa);

        assertThat(descJpa.getMarks()).isEqualTo("MARKS");
        assertThat(descJpa.getDescription()).isEqualTo("DESCRIPTION");
        assertThat(descJpa.getDescClause1()).isEqualTo("CLAUSE1");
        assertThat(descJpa.getRemark()).isEqualTo("REMARK");
        assertThat(descJpa.getMasterBl()).isSameAs(masterBlJpa);
    }

    @Test
    @DisplayName("toDescDomain(Optional.empty): 빈 Optional을 넘기면 빈 Optional이 반환된다")
    void toDescDomain_optionalEmpty_returnsEmpty() {
        Optional<MasterBlDesc> result = mapper.toDescDomain(Optional.empty());

        assertThat(result).isEmpty();
    }

    // ── E-07 SCHEDULE LEG ────────────────────────────────────────────────

    @Test
    @DisplayName("toScheduleLegDomain: JPA → 도메인 전체 필드가 복사된다")
    void toScheduleLegDomain_mapsAllFields() {
        MasterBlJpaEntity masterBlJpa = new MasterBlJpaEntity();
        masterBlJpa.setMasterBlId(3L);

        MasterBlScheduleLegJpaEntity legJpa = new MasterBlScheduleLegJpaEntity();
        legJpa.setMasterBl(masterBlJpa);
        legJpa.setToCode("NRT");
        legJpa.setByCarrier("KE");
        legJpa.setFlightNo("KE101");
        legJpa.setOnBoardDt("20240310");
        legJpa.setOnBoardTm("1430");
        legJpa.setArrivalDt("20240311");
        legJpa.setArrivalTm("0615");
        legJpa.setSeq(1);

        MasterBlScheduleLeg domain = mapper.toScheduleLegDomain(legJpa);

        assertThat(domain.getToCode()).isEqualTo("NRT");
        assertThat(domain.getByCarrier()).isEqualTo("KE");
        assertThat(domain.getFlightNo()).isEqualTo("KE101");
        assertThat(domain.getOnBoardDt()).isEqualTo("20240310");
        assertThat(domain.getOnBoardTm()).isEqualTo("1430");
        assertThat(domain.getArrivalDt()).isEqualTo("20240311");
        assertThat(domain.getArrivalTm()).isEqualTo("0615");
        assertThat(domain.getSeq()).isEqualTo(1);
    }

    @Test
    @DisplayName("applyScheduleLegFields: 도메인 → JPA 전체 필드가 세팅된다")
    void applyScheduleLegFields_setsAllFieldsToJpa() {
        MasterBlJpaEntity masterBlJpa = new MasterBlJpaEntity();
        masterBlJpa.setMasterBlId(3L);

        MasterBlScheduleLeg domain = MasterBlScheduleLeg.create(3L, "NRT", "20240310", "20240311", 1);
        domain.updateDetails("NRT", "KE", "KE101", "20240310", "1430", "20240311", "0615", 1);
        MasterBlScheduleLegJpaEntity legJpa = new MasterBlScheduleLegJpaEntity();

        mapper.applyScheduleLegFields(domain, legJpa, masterBlJpa);

        assertThat(legJpa.getToCode()).isEqualTo("NRT");
        assertThat(legJpa.getByCarrier()).isEqualTo("KE");
        assertThat(legJpa.getFlightNo()).isEqualTo("KE101");
        assertThat(legJpa.getOnBoardDt()).isEqualTo("20240310");
        assertThat(legJpa.getOnBoardTm()).isEqualTo("1430");
        assertThat(legJpa.getArrivalDt()).isEqualTo("20240311");
        assertThat(legJpa.getArrivalTm()).isEqualTo("0615");
        assertThat(legJpa.getSeq()).isEqualTo(1);
    }

    @Test
    @DisplayName("toScheduleLegDomainList: JPA 엔티티 3개 리스트 → 도메인 리스트로 변환된다")
    void toScheduleLegDomainList_convertsMultipleLegs() {
        MasterBlJpaEntity masterBlJpa = new MasterBlJpaEntity();
        masterBlJpa.setMasterBlId(3L);

        MasterBlScheduleLegJpaEntity leg1 = new MasterBlScheduleLegJpaEntity();
        leg1.setMasterBl(masterBlJpa);
        leg1.setToCode("HKG");
        leg1.setOnBoardDt("20240310");
        leg1.setArrivalDt("20240310");
        leg1.setSeq(1);

        MasterBlScheduleLegJpaEntity leg2 = new MasterBlScheduleLegJpaEntity();
        leg2.setMasterBl(masterBlJpa);
        leg2.setToCode("NRT");
        leg2.setOnBoardDt("20240311");
        leg2.setArrivalDt("20240311");
        leg2.setSeq(2);

        MasterBlScheduleLegJpaEntity leg3 = new MasterBlScheduleLegJpaEntity();
        leg3.setMasterBl(masterBlJpa);
        leg3.setToCode("LAX");
        leg3.setOnBoardDt("20240312");
        leg3.setArrivalDt("20240312");
        leg3.setSeq(3);

        List<MasterBlScheduleLeg> result = mapper.toScheduleLegDomainList(List.of(leg1, leg2, leg3));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getSeq()).isEqualTo(1);
        assertThat(result.get(1).getSeq()).isEqualTo(2);
        assertThat(result.get(2).getSeq()).isEqualTo(3);
    }

    @Test
    @DisplayName("applyScheduleLegFields: byCarrier와 flightNo가 null이어도 NPE 없이 동작한다")
    void applyScheduleLegFields_nullCarrierAndFlight_doesNotThrow() {
        MasterBlJpaEntity masterBlJpa = new MasterBlJpaEntity();
        masterBlJpa.setMasterBlId(3L);

        MasterBlScheduleLeg domain = MasterBlScheduleLeg.create(3L, "NRT", "20240310", "20240311", 1);
        domain.updateDetails("NRT", null, null, "20240310", "1430", "20240311", "0615", 1);
        MasterBlScheduleLegJpaEntity legJpa = new MasterBlScheduleLegJpaEntity();

        assertThatCode(() -> mapper.applyScheduleLegFields(domain, legJpa, masterBlJpa))
                .doesNotThrowAnyException();
    }
}
