package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
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
        domain.updateSeaFields(null, "MSC", "MSC OSCAR", "0012W", "2024-03-10", "BKG-001", "2024-03-01");
        MasterBlSeaJpaEntity jpa = new MasterBlSeaJpaEntity();

        mapper.applySeaFields(domain, jpa);

        assertThat(jpa.getLinerCode()).isEqualTo("MSC");
        assertThat(jpa.getVesselName()).isEqualTo("MSC OSCAR");
        assertThat(jpa.getVoyageNo()).isEqualTo("0012W");
        assertThat(jpa.getOnboardDate()).isEqualTo("2024-03-10");
        assertThat(jpa.getIssueDate()).isEqualTo("2024-03-01");
    }

    @Test
    @DisplayName("applyAirFields: AIR 도메인 필드가 MasterBlAirJpaEntity에 복사된다")
    void applyAirFields_copiesAirFieldsToJpa() {
        MasterBlAir domain = MasterBlAir.create(Bound.EXP);
        domain.updateAirFields("KE", "ICN", "180-12345678",
                BigDecimal.valueOf(100.5), BigDecimal.valueOf(90.0),
                "Q", "KRW", "N.V.D.", null, "NIL", null, null, null,
                "2024-03-01", "Seoul", "Signature");
        MasterBlAirJpaEntity jpa = new MasterBlAirJpaEntity();

        mapper.applyAirFields(domain, jpa);

        assertThat(jpa.getAirlineCode()).isEqualTo("KE");
        assertThat(jpa.getMawbNo()).isEqualTo("180-12345678");
        assertThat(jpa.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(jpa.getInsurance()).isEqualTo("NIL");
        assertThat(jpa.getIssueDate()).isEqualTo("2024-03-01");
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
        assertThat(domain.getMblNo()).isEqualTo("MBLNO-SEA-001");
        assertThat(domain.getShipperCode()).isEqualTo("SHIPPER-SEA");
        assertThat(domain.getPolCode()).isEqualTo("PUS");
        assertThat(domain.getPkgQty()).isEqualTo(20);
        assertThat(domain.getGrossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(5000.0));
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
        assertThat(domain.getMblNo()).isEqualTo("MAWB-001");
        assertThat(domain.getShipperCode()).isEqualTo("SHIPPER01");
        assertThat(domain.getPolCode()).isEqualTo("ICN");
        assertThat(domain.getFreightTerm()).isEqualTo(FreightTerm.PREPAID);
        assertThat(domain.getPkgQty()).isEqualTo(5);
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
}
