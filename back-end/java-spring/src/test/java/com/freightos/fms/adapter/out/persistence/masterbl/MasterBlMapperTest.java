package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class MasterBlMapperTest {

    private final MasterBlMapper mapper = new MasterBlMapper();

    // ── Domain → JPA ────────────────────────────────────────────────

    @Test
    @DisplayName("항공 Master B/L Domain→JPA 변환: MasterBlAirJpaEntity 타입이며 기본값 필드가 복사된다")
    void toJpa_airDomain_producesAirJpaEntity() {
        MasterBlAir domain = MasterBlAir.create(Bound.EXP);

        MasterBlAirJpaEntity jpa = (MasterBlAirJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(MasterBlAirJpaEntity.class);
        assertThat(jpa.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(jpa.getInsurance()).isEqualTo("NIL");
    }

    @Test
    @DisplayName("해상 Master B/L Domain→JPA 변환: MasterBlSeaJpaEntity 타입이다")
    void toJpa_seaDomain_producesSeaJpaEntity() {
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);

        MasterBlSeaJpaEntity jpa = (MasterBlSeaJpaEntity) mapper.toJpa(domain);

        assertThat(jpa).isInstanceOf(MasterBlSeaJpaEntity.class);
    }

    // ── JPA → Domain ────────────────────────────────────────────────

    @Test
    @DisplayName("항공 Master B/L JPA→Domain 변환: 공통 및 항공 전용 핵심 필드가 복사된다")
    void toDomain_airJpa_coreFieldsAreMapped() {
        MasterBlAirJpaEntity jpa = new MasterBlAirJpaEntity();
        jpa.setBound(Bound.EXP);
        jpa.setMblNo("MAWB-001");
        jpa.setMasterRefNo("REF-001");
        jpa.setShipperCode("SHIPPER01");
        jpa.setConsigneeCode("CONSIGNEE01");
        jpa.setNotifyCode("NOTIFY01");
        jpa.setPolCode("ICN");
        jpa.setPodCode("LAX");
        jpa.setEtd(LocalDate.of(2024, 3, 1));
        jpa.setEta(LocalDate.of(2024, 3, 10));
        jpa.setFreightTerm(FreightTerm.PREPAID);
        jpa.setOperatorCode("OP01");
        jpa.setTeamCode("TEAM01");
        jpa.setPkgQty(5);
        jpa.setPkgUnit("BOX");
        jpa.setGrossWeightKg(BigDecimal.valueOf(120.0));
        jpa.setCbm(BigDecimal.valueOf(2.5));
        jpa.setAirlineCode("KE");
        jpa.setMawbNo("180-12345678");
        jpa.setDeclaredValueCarriage("N.V.D.");
        jpa.setInsurance("NIL");
        jpa.setIssueDate(LocalDate.of(2024, 3, 1));
        jpa.setIssuePlace("Seoul");

        MasterBlAir domain = (MasterBlAir) mapper.toDomain(jpa);

        assertThat(domain).isInstanceOf(MasterBlAir.class);
        assertThat(domain.getMblNo()).isEqualTo("MAWB-001");
        assertThat(domain.getMasterRefNo()).isEqualTo("REF-001");
        assertThat(domain.getShipperCode()).isEqualTo("SHIPPER01");
        assertThat(domain.getConsigneeCode()).isEqualTo("CONSIGNEE01");
        assertThat(domain.getPolCode()).isEqualTo("ICN");
        assertThat(domain.getPodCode()).isEqualTo("LAX");
        assertThat(domain.getFreightTerm()).isEqualTo(FreightTerm.PREPAID);
        assertThat(domain.getPkgQty()).isEqualTo(5);
        assertThat(domain.getGrossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(120.0));
        assertThat(domain.getAirlineCode()).isEqualTo("KE");
        assertThat(domain.getMawbNo()).isEqualTo("180-12345678");
        assertThat(domain.getDeclaredValueCarriage()).isEqualTo("N.V.D.");
        assertThat(domain.getInsurance()).isEqualTo("NIL");
        assertThat(domain.getIssueDate()).isEqualTo(LocalDate.of(2024, 3, 1));
    }

    @Test
    @DisplayName("해상 Master B/L JPA→Domain 변환: 공통 및 해상 전용 핵심 필드가 복사된다")
    void toDomain_seaJpa_coreFieldsAreMapped() {
        MasterBlSeaJpaEntity jpa = new MasterBlSeaJpaEntity();
        jpa.setBound(Bound.EXP);
        jpa.setMblNo("MBLNO-SEA-001");
        jpa.setShipperCode("SHIPPER-SEA");
        jpa.setPolCode("PUS");
        jpa.setPodCode("SYD");
        jpa.setPkgQty(20);
        jpa.setGrossWeightKg(BigDecimal.valueOf(5000.0));
        jpa.setCbm(BigDecimal.valueOf(25.0));
        jpa.setLinerCode("MSC");
        jpa.setVesselName("MSC OSCAR");
        jpa.setVoyageNo("0012W");
        jpa.setLineBkgNo("BKG-123");
        jpa.setIssueDate(LocalDate.of(2024, 4, 5));

        MasterBlSea domain = (MasterBlSea) mapper.toDomain(jpa);

        assertThat(domain).isInstanceOf(MasterBlSea.class);
        assertThat(domain.getMblNo()).isEqualTo("MBLNO-SEA-001");
        assertThat(domain.getShipperCode()).isEqualTo("SHIPPER-SEA");
        assertThat(domain.getPolCode()).isEqualTo("PUS");
        assertThat(domain.getPkgQty()).isEqualTo(20);
        assertThat(domain.getGrossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(5000.0));
        assertThat(domain.getLinerCode()).isEqualTo("MSC");
        assertThat(domain.getVesselName()).isEqualTo("MSC OSCAR");
        assertThat(domain.getVoyageNo()).isEqualTo("0012W");
        assertThat(domain.getLineBkgNo()).isEqualTo("BKG-123");
        assertThat(domain.getIssueDate()).isEqualTo(LocalDate.of(2024, 4, 5));
    }
}
