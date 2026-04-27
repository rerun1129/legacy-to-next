package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

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
        HouseBlTruck domain = HouseBlTruck.create();
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();

        mapper.applyCommonFields(domain, jpa);

        assertThat(jpa.getJobDiv()).isEqualTo(JobDiv.TRUCK);
    }

    @Test
    @DisplayName("applyCommonFields: NON_BL domain → HouseBlJpaEntity에 jobDiv=NON_BL이 세팅된다")
    void applyCommonFields_nonBlDomain_setsJobDivNonBl() {
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA);
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
        HouseBlTruck domain = HouseBlTruck.create();
        HouseBlTruckJpaEntity jpa = new HouseBlTruckJpaEntity();

        mapper.applyTruckFields(domain, jpa);

        assertThat(jpa.getVesselName()).isEqualTo("TRUCK");
    }

    // ── applyNonBlFields ─────────────────────────────────────────────

    @Test
    @DisplayName("applyNonBlFields: workDivision이 매핑된다")
    void applyNonBlFields_workDivisionIsMapped() {
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA);
        HouseBlNonBlJpaEntity jpa = new HouseBlNonBlJpaEntity();

        mapper.applyNonBlFields(domain, jpa);

        assertThat(jpa.getWorkDivision()).isEqualTo(HouseBlNonBl.WorkDivision.SEA);
        assertThat(jpa.getStatus()).isEqualTo("접수");
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
        // airExt null — lazy 미로드 상태 시뮬레이션

        HouseBlAir domain = (HouseBlAir) mapper.toDomain(parentJpa);

        assertThat(domain).isInstanceOf(HouseBlAir.class);
        assertThat(domain.getShipperCode()).isEqualTo("SHIPPER01");
        assertThat(domain.getConsigneeCode()).isEqualTo("CONSIGNEE01");
        assertThat(domain.getPkgQty()).isEqualTo(10);
        assertThat(domain.getGrossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(250.0));
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

        HouseBlSea domain = (HouseBlSea) mapper.toDomain(parentJpa);

        assertThat(domain).isInstanceOf(HouseBlSea.class);
        assertThat(domain.getHblNo()).isEqualTo("HBL-SEA-001");
        assertThat(domain.getShipperCode()).isEqualTo("SHIPPER02");
        assertThat(domain.getPkgQty()).isEqualTo(5);
        assertThat(domain.getCbm()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
        assertThat(domain.getContainers()).isEmpty();
    }

    @Test
    @DisplayName("toDomain: TRUCK JPA → HouseBlTruck 반환된다")
    void toDomain_truckJpa_producesTruckDomain() {
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setJobDiv(JobDiv.TRUCK);
        parentJpa.setBound(Bound.EXP);

        HouseBl domain = mapper.toDomain(parentJpa);

        assertThat(domain).isInstanceOf(HouseBlTruck.class);
    }

    @Test
    @DisplayName("toDomain: NON_BL JPA + nonBlExt null → workDivision null 허용")
    void toDomain_nonBlJpa_withNullExt_workDivisionIsNull() {
        HouseBlJpaEntity parentJpa = new HouseBlJpaEntity();
        parentJpa.setJobDiv(JobDiv.NON_BL);
        parentJpa.setBound(Bound.EXP);

        HouseBl domain = mapper.toDomain(parentJpa);

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

        HouseBlAir domain = (HouseBlAir) mapper.toDomain(parentJpa);

        assertThat(domain.getId()).isEqualTo(42L);
    }
}
