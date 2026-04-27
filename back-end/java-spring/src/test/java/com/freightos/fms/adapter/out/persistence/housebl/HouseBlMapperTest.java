package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlNonBl;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HouseBlMapperTest {

    private final HouseBlMapper mapper = new HouseBlMapper();

    // ── Domain → JPA ────────────────────────────────────────────────

    @Test
    @DisplayName("항공 Domain→JPA 변환: HouseBlAirJpaEntity 타입이며 기본값 필드가 복사된다")
    void toJpa_airDomain_producesAirJpaEntity() {
        HouseBlAir domain = HouseBlAir.create(Bound.EXP);

        HouseBlAirJpaEntity jpa = (HouseBlAirJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(HouseBlAirJpaEntity.class);
        assertThat(jpa.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(jpa.getInsurance()).isEqualTo("NIL");
    }

    @Test
    @DisplayName("해상 Domain→JPA 변환: isTriangle/isCoLoad 게터가 정상 동작한다 (회귀 검출)")
    void toJpa_seaDomain_triangleFlagsAreMapped() {
        HouseBlSea domain = HouseBlSea.create(Bound.EXP);

        HouseBlSeaJpaEntity jpa = (HouseBlSeaJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(HouseBlSeaJpaEntity.class);
        assertThat(jpa.isTriangle()).isFalse();
        assertThat(jpa.isCoLoad()).isFalse();
    }

    @Test
    @DisplayName("트럭 Domain→JPA 변환: vesselName 이 TRUCK 으로 고정된다")
    void toJpa_truckDomain_vesselNameIsTruck() {
        HouseBlTruck domain = HouseBlTruck.create();

        HouseBlTruckJpaEntity jpa = (HouseBlTruckJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(HouseBlTruckJpaEntity.class);
        assertThat(jpa.getVesselName()).isEqualTo("TRUCK");
    }

    @Test
    @DisplayName("Non-B/L Domain→JPA 변환: workDivision 이 매핑된다")
    void toJpa_nonBlDomain_workDivisionIsMapped() {
        HouseBlNonBl domain = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA);

        HouseBlNonBlJpaEntity jpa = (HouseBlNonBlJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(HouseBlNonBlJpaEntity.class);
        assertThat(jpa.getWorkDivision()).isEqualTo(HouseBlNonBl.WorkDivision.SEA);
    }

    // ── JPA → Domain ────────────────────────────────────────────────

    @Test
    @DisplayName("항공 JPA→Domain 변환: 핵심 항공 필드가 도메인으로 복사된다")
    void toDomain_airJpa_coreAirFieldsAreMapped() {
        HouseBlAirJpaEntity jpa = new HouseBlAirJpaEntity();
        jpa.setBound(Bound.EXP);
        jpa.setAirlineCode("KE");
        jpa.setMawbNo("180-12345678");
        jpa.setChargeWeightKg(BigDecimal.valueOf(100.5));
        jpa.setDeclaredValueCarriage("N.V.D.");
        jpa.setInsurance("NIL");
        jpa.setIssueDate(LocalDate.of(2024, 1, 15));
        jpa.setIssuePlace("Seoul");

        HouseBlAir domain = (HouseBlAir) mapper.toDomain(jpa);

        assertThat(domain).isInstanceOf(HouseBlAir.class);
        assertThat(domain.getAirlineCode()).isEqualTo("KE");
        assertThat(domain.getMawbNo()).isEqualTo("180-12345678");
        assertThat(domain.getChargeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(100.5));
        assertThat(domain.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(domain.getInsurance()).isEqualTo("NIL");
        assertThat(domain.getIssueDate()).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(domain.getIssuePlace()).isEqualTo("Seoul");
    }

    @Test
    @DisplayName("항공 JPA→Domain 변환: 공통 기본 필드(shipperCode, pkgQty 등)도 복사된다")
    void toDomain_airJpa_baseFieldsAreMapped() {
        UUID expectedId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2024, 6, 1, 12, 0);

        HouseBlAirJpaEntity jpa = new HouseBlAirJpaEntity();
        jpa.setId(expectedId);
        jpa.setBound(Bound.IMP);
        jpa.setHblNo("HBL-001");
        jpa.setShipperCode("SHIPPER01");
        jpa.setConsigneeCode("CONSIGNEE01");
        jpa.setPkgQty(10);
        jpa.setGrossWeightKg(BigDecimal.valueOf(250.0));

        HouseBlAir domain = (HouseBlAir) mapper.toDomain(jpa);

        assertThat(domain.getId()).isEqualTo(expectedId);
        assertThat(domain.getHblNo()).isEqualTo("HBL-001");
        assertThat(domain.getShipperCode()).isEqualTo("SHIPPER01");
        assertThat(domain.getConsigneeCode()).isEqualTo("CONSIGNEE01");
        assertThat(domain.getPkgQty()).isEqualTo(10);
        assertThat(domain.getGrossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(250.0));
    }

    @Test
    @DisplayName("항공 JPA→Domain 변환: assignIdentity로 id/createdAt/updatedBy 가 도메인에 주입된다")
    void toDomain_airJpa_identityFieldsAreAssigned() {
        UUID expectedId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 6, 1, 12, 0);

        HouseBlAirJpaEntity jpa = new HouseBlAirJpaEntity();
        jpa.setId(expectedId);
        jpa.setBound(Bound.EXP);

        HouseBlAir domain = (HouseBlAir) mapper.toDomain(jpa);

        assertThat(domain.getId()).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("해상 JPA→Domain 변환: 해상 전용 필드와 공통 필드가 복사된다")
    void toDomain_seaJpa_coreSeaFieldsAreMapped() {
        HouseBlSeaJpaEntity jpa = new HouseBlSeaJpaEntity();
        jpa.setBound(Bound.EXP);
        jpa.setHblNo("HBL-SEA-001");
        jpa.setShipperCode("SHIPPER02");
        jpa.setLinerCode("MSC");
        jpa.setVesselName("EVER GIVEN");
        jpa.setVoyageNo("0001E");
        jpa.setMblNo("MBLNO-001");
        jpa.setIsTriangle(true);
        jpa.setPkgQty(5);
        jpa.setCbm(BigDecimal.valueOf(30.0));

        HouseBlSea domain = (HouseBlSea) mapper.toDomain(jpa);

        assertThat(domain).isInstanceOf(HouseBlSea.class);
        assertThat(domain.getHblNo()).isEqualTo("HBL-SEA-001");
        assertThat(domain.getShipperCode()).isEqualTo("SHIPPER02");
        assertThat(domain.getLinerCode()).isEqualTo("MSC");
        assertThat(domain.getVesselName()).isEqualTo("EVER GIVEN");
        assertThat(domain.getVoyageNo()).isEqualTo("0001E");
        assertThat(domain.getMblNo()).isEqualTo("MBLNO-001");
        assertThat(domain.isTriangle()).isTrue();
        assertThat(domain.getPkgQty()).isEqualTo(5);
        assertThat(domain.getCbm()).isEqualByComparingTo(BigDecimal.valueOf(30.0));
    }
}
