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

/**
 * MasterBl JPA 엔티티 구조 검증 테스트.
 * - JOINED 상속에서 @OneToOne 독립 엔티티 구조로 변경됨.
 * - Mapper(toDomain/toJpa) 검증은 Coder-4의 Mapper 리팩토링 완료 후 별도 갱신.
 */
class MasterBlMapperTest {

    // ── JPA 엔티티 직접 구조 검증 ──────────────────────────────────

    @Test
    @DisplayName("MasterBlJpaEntity: String 타입 날짜 필드 설정 및 조회가 동작한다")
    void masterBlJpa_stringDateFields_setAndGetWork() {
        MasterBlJpaEntity jpa = new MasterBlJpaEntity();
        jpa.setBound(Bound.EXP);
        jpa.setMblNo("MAWB-001");
        jpa.setMasterRefNo("REF-001");
        jpa.setShipperCode("SHIPPER01");
        jpa.setConsigneeCode("CONSIGNEE01");
        jpa.setPolCode("ICN");
        jpa.setPodCode("LAX");
        jpa.setEtd("20240301");
        jpa.setEta("20240310");
        jpa.setFreightTerm(FreightTerm.PREPAID);
        jpa.setPkgQty(5);
        jpa.setGrossWeightKg(BigDecimal.valueOf(120.0));
        jpa.setJobDiv("AIR");

        assertThat(jpa.getEtd()).isEqualTo("20240301");
        assertThat(jpa.getEta()).isEqualTo("20240310");
        assertThat(jpa.getJobDiv()).isEqualTo("AIR");
        assertThat(jpa.getMblNo()).isEqualTo("MAWB-001");
    }

    @Test
    @DisplayName("MasterBlAirJpaEntity: String 타입 issueDate 설정 및 기본값 필드가 정상 동작한다")
    void masterBlAirJpa_stringIssueDate_setAndGetWork() {
        MasterBlAirJpaEntity jpa = new MasterBlAirJpaEntity();
        jpa.setAirlineCode("KE");
        jpa.setMawbNo("180-12345678");
        jpa.setDeclaredValueCarriage("N.V.D.");
        jpa.setInsurance("NIL");
        jpa.setIssueDate("20240301");
        jpa.setIssuePlace("Seoul");

        assertThat(jpa.getAirlineCode()).isEqualTo("KE");
        assertThat(jpa.getMawbNo()).isEqualTo("180-12345678");
        assertThat(jpa.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(jpa.getInsurance()).isEqualTo("NIL");
        assertThat(jpa.getIssueDate()).isEqualTo("20240301");
        assertThat(jpa.getIssuePlace()).isEqualTo("Seoul");
    }

    @Test
    @DisplayName("MasterBlSeaJpaEntity: String 타입 날짜 필드와 @OneToOne 연관이 설정된다")
    void masterBlSeaJpa_stringDatesAndOneToOneRef_work() {
        MasterBlJpaEntity base = new MasterBlJpaEntity();
        base.setBound(Bound.EXP);
        base.setMblNo("MBLNO-SEA-001");
        base.setShipperCode("SHIPPER-SEA");
        base.setPolCode("PUS");
        base.setPodCode("SYD");
        base.setPkgQty(20);
        base.setGrossWeightKg(BigDecimal.valueOf(5000.0));
        base.setJobDiv("SEA");

        MasterBlSeaJpaEntity seaExt = new MasterBlSeaJpaEntity();
        seaExt.setMasterBl(base);
        seaExt.setLinerCode("MSC");
        seaExt.setVesselName("MSC OSCAR");
        seaExt.setVoyageNo("0012W");
        seaExt.setLineBkgNo("BKG-123");
        seaExt.setIssueDate("20240405");
        seaExt.setOnboardDate("20240330");

        assertThat(seaExt.getMasterBl()).isSameAs(base);
        assertThat(seaExt.getLinerCode()).isEqualTo("MSC");
        assertThat(seaExt.getVesselName()).isEqualTo("MSC OSCAR");
        assertThat(seaExt.getIssueDate()).isEqualTo("20240405");
        assertThat(seaExt.getOnboardDate()).isEqualTo("20240330");
    }

    @Test
    @DisplayName("MasterBlAirJpaEntity: @OneToOne 참조(masterBl)가 설정된다")
    void masterBlAirJpa_oneToOneRef_isSet() {
        MasterBlJpaEntity base = new MasterBlJpaEntity();
        base.setBound(Bound.EXP);
        base.setJobDiv("AIR");

        MasterBlAirJpaEntity airExt = new MasterBlAirJpaEntity();
        airExt.setMasterBl(base);
        airExt.setAirlineCode("KE");

        assertThat(airExt.getMasterBl()).isSameAs(base);
        assertThat(airExt.getAirlineCode()).isEqualTo("KE");
    }
}
