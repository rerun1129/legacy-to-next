package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlNonBl;
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

    // ── JPA 엔티티 직접 구조 검증 ──────────────────────────────────

    @Test
    @DisplayName("HouseBlJpaEntity: String 타입 날짜 필드와 Long PK 설정이 동작한다")
    void houseBlJpa_stringDateAndLongPk_work() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setHouseBlId(1L);
        jpa.setBound(Bound.EXP);
        jpa.setJobDiv(JobDiv.SEA);
        jpa.setHblNo("HBL-SEA-001");
        jpa.setShipperCode("SHIPPER02");
        jpa.setEtd("20240301");
        jpa.setEta("20240310");
        jpa.setMasterBlId(100L);

        assertThat(jpa.getHouseBlId()).isEqualTo(1L);
        assertThat(jpa.getEtd()).isEqualTo("20240301");
        assertThat(jpa.getEta()).isEqualTo("20240310");
        assertThat(jpa.getMasterBlId()).isEqualTo(100L);
        assertThat(jpa.getJobDiv()).isEqualTo(JobDiv.SEA);
    }

    @Test
    @DisplayName("HouseBlSeaJpaEntity: @OneToOne houseBl 참조와 isTriangle/isCoLoad 게터가 동작한다")
    void houseBlSeaJpa_oneToOneRefAndBooleanFlags_work() {
        HouseBlJpaEntity base = new HouseBlJpaEntity();
        base.setBound(Bound.EXP);
        base.setJobDiv(JobDiv.SEA);

        HouseBlSeaJpaEntity seaExt = new HouseBlSeaJpaEntity();
        seaExt.setHouseBl(base);
        seaExt.setLinerCode("MSC");
        seaExt.setVesselName("EVER GIVEN");
        seaExt.setVoyageNo("0001E");
        seaExt.setMblNo("MBLNO-001");
        seaExt.setIsTriangle(true);
        seaExt.setIsCoLoad(false);
        seaExt.setIssueDate("20240301");
        seaExt.setDoDate("20240320");

        assertThat(seaExt.getHouseBl()).isSameAs(base);
        assertThat(seaExt.isTriangle()).isTrue();
        assertThat(seaExt.isCoLoad()).isFalse();
        assertThat(seaExt.getIssueDate()).isEqualTo("20240301");
        assertThat(seaExt.getDoDate()).isEqualTo("20240320");
    }

    @Test
    @DisplayName("HouseBlAirJpaEntity: @OneToOne houseBl 참조와 String 날짜 필드가 동작한다")
    void houseBlAirJpa_oneToOneRefAndStringDate_work() {
        HouseBlJpaEntity base = new HouseBlJpaEntity();
        base.setBound(Bound.EXP);
        base.setJobDiv(JobDiv.AIR);

        HouseBlAirJpaEntity airExt = new HouseBlAirJpaEntity();
        airExt.setHouseBl(base);
        airExt.setAirlineCode("KE");
        airExt.setMawbNo("180-12345678");
        airExt.setChargeWeightKg(BigDecimal.valueOf(100.5));
        airExt.setDeclaredValueCarriage("N.V.D.");
        airExt.setInsurance("NIL");
        airExt.setIssueDate("20240115");
        airExt.setIssuePlace("Seoul");

        assertThat(airExt.getHouseBl()).isSameAs(base);
        assertThat(airExt.getAirlineCode()).isEqualTo("KE");
        assertThat(airExt.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(airExt.getInsurance()).isEqualTo("NIL");
        assertThat(airExt.getIssueDate()).isEqualTo("20240115");
    }

    @Test
    @DisplayName("HouseBlTruckJpaEntity: vesselName 기본값이 TRUCK이며 String 날짜 필드가 동작한다")
    void houseBlTruckJpa_defaultVesselNameAndStringPickupDate_work() {
        HouseBlJpaEntity base = new HouseBlJpaEntity();
        base.setBound(Bound.EXP);
        base.setJobDiv(JobDiv.TRUCK);

        HouseBlTruckJpaEntity truckExt = new HouseBlTruckJpaEntity();
        truckExt.setHouseBl(base);
        truckExt.setPickupDate("20240301");
        truckExt.setTruckerCode("TRUCKER01");

        assertThat(truckExt.getVesselName()).isEqualTo("TRUCK");
        assertThat(truckExt.getPickupDate()).isEqualTo("20240301");
        assertThat(truckExt.getHouseBl()).isSameAs(base);
    }

    @Test
    @DisplayName("HouseBlNonBlJpaEntity: @OneToOne houseBl 참조와 workDivision 이 동작한다")
    void houseBlNonBlJpa_oneToOneRefAndWorkDivision_work() {
        HouseBlJpaEntity base = new HouseBlJpaEntity();
        base.setBound(Bound.EXP);
        base.setJobDiv(JobDiv.NON_BL);

        HouseBlNonBlJpaEntity nonBlExt = new HouseBlNonBlJpaEntity();
        nonBlExt.setHouseBl(base);
        nonBlExt.setWorkDivision(HouseBlNonBl.WorkDivision.SEA);
        nonBlExt.setSettlePartnerCode("PARTNER01");

        assertThat(nonBlExt.getHouseBl()).isSameAs(base);
        assertThat(nonBlExt.getWorkDivision()).isEqualTo(HouseBlNonBl.WorkDivision.SEA);
        assertThat(nonBlExt.getSettlePartnerCode()).isEqualTo("PARTNER01");
    }
}
