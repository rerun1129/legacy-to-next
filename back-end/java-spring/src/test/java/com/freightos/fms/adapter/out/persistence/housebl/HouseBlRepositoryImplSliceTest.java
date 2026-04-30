package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.application.config.QueryDslConfig;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QueryDSL HouseBlSummary projection의 N+1 회피와 페이징·정렬 동작을 검증한다.
 * H2 in-memory + Hibernate Statistics로 실제 PreparedStatement 횟수를 측정.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
class HouseBlRepositoryImplSliceTest {

    @Autowired
    private HouseBlRepository houseBlRepository;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private EntityManagerFactory emf;

    private Statistics stats;

    @BeforeEach
    void enableStatistics() {
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();
    }

    @Test
    @DisplayName("findSummariesByJobDivAndBound: PreparedStatement 정확히 2회 (SELECT content + COUNT)")
    void findSummariesByJobDivAndBound_emitsExactlyTwoStatements() {
        persist(JobDiv.SEA, Bound.EXP, 2);
        em.flush();
        em.clear();
        stats.clear();

        houseBlRepository.findSummariesByJobDivAndBound(JobDiv.SEA, Bound.EXP, PageRequest.of(0, 10));

        assertThat(stats.getPrepareStatementCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("findSummariesByJobDivAndBound: jobDiv+bound 필터가 정확히 동작한다")
    void findSummariesByJobDivAndBound_filtersByJobDivAndBound() {
        persist(JobDiv.SEA, Bound.EXP, 2);
        persist(JobDiv.SEA, Bound.IMP, 1);
        persist(JobDiv.AIR, Bound.EXP, 1);
        em.flush();

        PagedResult<HouseBlSummary> result = houseBlRepository
                .findSummariesByJobDivAndBound(JobDiv.SEA, Bound.EXP, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        result.getContent().forEach(s -> {
            assertThat(s.jobDiv()).isEqualTo(JobDiv.SEA);
            assertThat(s.bound()).isEqualTo(Bound.EXP);
        });
    }

    @Test
    @DisplayName("findSummariesByJobDivAndBound: HouseBlSummary projection의 핵심 필드가 올바르게 채워진다")
    void findSummariesByJobDivAndBound_returnsCorrectProjection() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.SEA);
        jpa.setBound(Bound.EXP);
        jpa.setHblNo("HBL-TEST-001");
        jpa.setPolCode("KRPUS");
        jpa.setPodCode("USNYC");
        jpa.setShipperCode("SHIP01");
        jpa.setConsigneeCode("CONS01");
        jpa.setPkgQty(5);
        jpa.setPkgUnit("CTN");
        em.persist(jpa);
        em.flush();

        PagedResult<HouseBlSummary> result = houseBlRepository
                .findSummariesByJobDivAndBound(JobDiv.SEA, Bound.EXP, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        HouseBlSummary s = result.getContent().get(0);
        assertThat(s.houseBlId()).isNotNull();
        assertThat(s.hblNo()).isEqualTo("HBL-TEST-001");
        assertThat(s.jobDiv()).isEqualTo(JobDiv.SEA);
        assertThat(s.bound()).isEqualTo(Bound.EXP);
        assertThat(s.polCode()).isEqualTo("KRPUS");
        assertThat(s.podCode()).isEqualTo("USNYC");
        assertThat(s.shipperCode()).isEqualTo("SHIP01");
        assertThat(s.consigneeCode()).isEqualTo("CONS01");
        assertThat(s.pkgQty()).isEqualTo(5);
        assertThat(s.pkgUnit()).isEqualTo("CTN");
        assertThat(s.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("findSummariesByJobDivAndBound: 결과가 createdAt 내림차순으로 정렬된다")
    void findSummariesByJobDivAndBound_sortedByCreatedAtDesc() {
        HouseBlJpaEntity first = newJpa(JobDiv.SEA, Bound.EXP);
        em.persist(first);
        em.flush();

        HouseBlJpaEntity second = newJpa(JobDiv.SEA, Bound.EXP);
        em.persist(second);
        em.flush();
        em.clear();

        PagedResult<HouseBlSummary> result = houseBlRepository
                .findSummariesByJobDivAndBound(JobDiv.SEA, Bound.EXP, PageRequest.of(0, 10));

        List<HouseBlSummary> content = result.getContent();
        assertThat(content).hasSize(2);
        assertThat(content.get(0).createdAt())
                .isAfterOrEqualTo(content.get(1).createdAt());
    }

    @Test
    @DisplayName("findSummariesByJobDivAndBound: 페이지네이션이 동작한다 (size=2, page=1 → 2건 반환, total=5)")
    void findSummariesByJobDivAndBound_appliesPagination() {
        persist(JobDiv.SEA, Bound.EXP, 5);
        em.flush();

        PagedResult<HouseBlSummary> result = houseBlRepository
                .findSummariesByJobDivAndBound(JobDiv.SEA, Bound.EXP, PageRequest.of(1, 2));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    private void persist(JobDiv jobDiv, Bound bound, int count) {
        for (int i = 0; i < count; i++) {
            em.persist(newJpa(jobDiv, bound));
        }
    }

    private HouseBlJpaEntity newJpa(JobDiv jobDiv, Bound bound) {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(jobDiv);
        jpa.setBound(bound);
        return jpa;
    }

    private MasterBlJpaEntity persistMasterBl(String jobDiv, Bound bound) {
        MasterBlJpaEntity master = new MasterBlJpaEntity();
        master.setJobDiv(jobDiv);
        master.setBound(bound);
        em.persist(master);
        return master;
    }

    private HouseBlJpaEntity persistHouseBlWithSeaExt(MasterBlJpaEntity master, Bound bound, String hblNo) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.SEA);
        house.setBound(bound);
        house.setMasterBlId(master.getMasterBlId());
        house.setHblNo(hblNo);
        house.setShipperCode("SHIP01");
        house.setConsigneeCode("CONS01");
        house.setDocPartnerCode("DOC01");
        house.setPolCode("KRPUS");
        house.setPodCode("USNYC");
        house.setEtd("20251130");
        house.setEta("20251201");
        house.setPkgQty(10);
        house.setPkgUnit("CTN");
        house.setGrossWeightKg(BigDecimal.valueOf(100));
        house.setCbm(BigDecimal.valueOf(1));
        em.persist(house);

        HouseBlSeaJpaEntity sea = new HouseBlSeaJpaEntity();
        sea.setHouseBl(house);
        sea.setVesselName("VESSEL-" + hblNo);
        sea.setVoyageNo("VOY-" + hblNo);
        em.persist(sea);

        return house;
    }

    private HouseBlJpaEntity persistHouseBlWithAirExt(MasterBlJpaEntity master, Bound bound, String hblNo) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.AIR);
        house.setBound(bound);
        house.setMasterBlId(master.getMasterBlId());
        house.setHblNo(hblNo);
        house.setShipperCode("SHIP01");
        house.setConsigneeCode("CONS01");
        house.setDocPartnerCode("DOC01");
        house.setPkgQty(3);
        house.setPkgUnit("PKG");
        house.setGrossWeightKg(BigDecimal.valueOf(30));
        house.setCbm(BigDecimal.valueOf(0.3));
        em.persist(house);

        HouseBlAirJpaEntity air = new HouseBlAirJpaEntity();
        air.setHouseBl(house);
        air.setChargeWeightKg(BigDecimal.valueOf(45));
        em.persist(air);

        return house;
    }

    private HouseBlJpaEntity persistHouseBlWithoutExt(MasterBlJpaEntity master, JobDiv jobDiv, Bound bound) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(jobDiv);
        house.setBound(bound);
        house.setMasterBlId(master.getMasterBlId());
        house.setHblNo("HBL-NO-EXT");
        em.persist(house);
        return house;
    }

    // ── findConsoledSeaSummariesByMasterBlId ──────────────────

    @Test
    @DisplayName("findConsoledSeaSummariesByMasterBlId: PreparedStatement 정확히 1회 (단건 INNER JOIN)")
    void findConsoledSeaSummariesByMasterBlId_emitsExactlySingleStatement() {
        MasterBlJpaEntity master = persistMasterBl("SEA", Bound.EXP);
        em.flush();

        persistHouseBlWithSeaExt(master, Bound.EXP, "HBL-001");
        persistHouseBlWithSeaExt(master, Bound.EXP, "HBL-002");
        em.flush();
        em.clear();
        stats.clear();

        houseBlRepository.findConsoledSeaSummariesByMasterBlId(master.getMasterBlId());

        assertThat(stats.getPrepareStatementCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findConsoledSeaSummariesByMasterBlId: 다른 masterBlId의 HouseBl은 제외된다")
    void findConsoledSeaSummariesByMasterBlId_filtersByMasterBlIdExactly() {
        MasterBlJpaEntity masterA = persistMasterBl("SEA", Bound.EXP);
        em.flush();
        MasterBlJpaEntity masterB = persistMasterBl("SEA", Bound.EXP);
        em.flush();

        persistHouseBlWithSeaExt(masterA, Bound.EXP, "HBL-A-001");
        persistHouseBlWithSeaExt(masterA, Bound.EXP, "HBL-A-002");
        persistHouseBlWithSeaExt(masterB, Bound.EXP, "HBL-B-001");
        em.flush();

        List<ConsoledHouseBlSeaSummary> result =
                houseBlRepository.findConsoledSeaSummariesByMasterBlId(masterA.getMasterBlId());

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.houseBlId() != null);
    }

    @Test
    @DisplayName("findConsoledSeaSummariesByMasterBlId: HouseBlSea 확장 없는 HouseBl은 결과에서 제외된다 (INNER JOIN)")
    void findConsoledSeaSummariesByMasterBlId_excludesHouseBlWithoutSeaExt() {
        MasterBlJpaEntity master = persistMasterBl("SEA", Bound.EXP);
        em.flush();

        persistHouseBlWithSeaExt(master, Bound.EXP, "HBL-WITH-SEA");
        persistHouseBlWithoutExt(master, JobDiv.SEA, Bound.EXP);
        em.flush();

        List<ConsoledHouseBlSeaSummary> result =
                houseBlRepository.findConsoledSeaSummariesByMasterBlId(master.getMasterBlId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).hblNo()).isEqualTo("HBL-WITH-SEA");
    }

    @Test
    @DisplayName("findConsoledSeaSummariesByMasterBlId: createdAt 내림차순 정렬 (나중에 생성된 것이 먼저 반환된다)")
    void findConsoledSeaSummariesByMasterBlId_sortedByHouseBlCreatedAtDesc() {
        MasterBlJpaEntity master = persistMasterBl("SEA", Bound.EXP);
        em.flush();

        persistHouseBlWithSeaExt(master, Bound.EXP, "HBL-FIRST");
        em.flush();

        persistHouseBlWithSeaExt(master, Bound.EXP, "HBL-SECOND");
        em.flush();
        em.clear();

        List<ConsoledHouseBlSeaSummary> result =
                houseBlRepository.findConsoledSeaSummariesByMasterBlId(master.getMasterBlId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).hblNo()).isEqualTo("HBL-SECOND");
    }

    @Test
    @DisplayName("findConsoledSeaSummariesByMasterBlId: 15개 필드(houseBlId, hblNo, shipperCode, consigneeCode, docPartnerCode, pkgQty, pkgUnit, grossWeightKg, cbm, etd, eta, vesselName, voyageNo, polCode, podCode)가 올바른 위치에 매핑된다")
    void findConsoledSeaSummariesByMasterBlId_returnsAllFifteenFieldsCorrectly() {
        MasterBlJpaEntity master = persistMasterBl("SEA", Bound.EXP);
        em.flush();

        persistHouseBlWithSeaExt(master, Bound.EXP, "HBL-FULL");
        em.flush();

        ConsoledHouseBlSeaSummary s =
                houseBlRepository.findConsoledSeaSummariesByMasterBlId(master.getMasterBlId()).get(0);

        assertThat(s.houseBlId()).isNotNull();
        assertThat(s.hblNo()).isEqualTo("HBL-FULL");
        assertThat(s.shipperCode()).isEqualTo("SHIP01");
        assertThat(s.consigneeCode()).isEqualTo("CONS01");
        assertThat(s.docPartnerCode()).isEqualTo("DOC01");
        assertThat(s.pkgQty()).isEqualTo(10);
        assertThat(s.pkgUnit()).isEqualTo("CTN");
        assertThat(s.grossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(s.cbm()).isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(s.etd()).isEqualTo("20251130");
        assertThat(s.eta()).isEqualTo("20251201");
        assertThat(s.vesselName()).isEqualTo("VESSEL-HBL-FULL");
        assertThat(s.voyageNo()).isEqualTo("VOY-HBL-FULL");
        assertThat(s.polCode()).isEqualTo("KRPUS");
        assertThat(s.podCode()).isEqualTo("USNYC");
    }

    @Test
    @DisplayName("findConsoledSeaSummariesByMasterBlId: 매칭 0건 → 빈 리스트(null 아님)")
    void findConsoledSeaSummariesByMasterBlId_zeroMatches_returnsEmptyList() {
        List<ConsoledHouseBlSeaSummary> result =
                houseBlRepository.findConsoledSeaSummariesByMasterBlId(9999L);

        assertThat(result).isNotNull().isEmpty();
    }

    // ── findConsoledAirSummariesByMasterBlId ──────────────────

    @Test
    @DisplayName("findConsoledAirSummariesByMasterBlId: PreparedStatement 정확히 1회 (단건 INNER JOIN)")
    void findConsoledAirSummariesByMasterBlId_emitsExactlySingleStatement() {
        MasterBlJpaEntity master = persistMasterBl("AIR", Bound.EXP);
        em.flush();

        persistHouseBlWithAirExt(master, Bound.EXP, "HBL-AIR-001");
        persistHouseBlWithAirExt(master, Bound.EXP, "HBL-AIR-002");
        em.flush();
        em.clear();
        stats.clear();

        houseBlRepository.findConsoledAirSummariesByMasterBlId(master.getMasterBlId());

        assertThat(stats.getPrepareStatementCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findConsoledAirSummariesByMasterBlId: 다른 masterBlId의 HouseBl은 제외된다")
    void findConsoledAirSummariesByMasterBlId_filtersByMasterBlIdExactly() {
        MasterBlJpaEntity masterA = persistMasterBl("AIR", Bound.EXP);
        em.flush();
        MasterBlJpaEntity masterB = persistMasterBl("AIR", Bound.EXP);
        em.flush();

        persistHouseBlWithAirExt(masterA, Bound.EXP, "HBL-AIR-A-001");
        persistHouseBlWithAirExt(masterA, Bound.EXP, "HBL-AIR-A-002");
        persistHouseBlWithAirExt(masterB, Bound.EXP, "HBL-AIR-B-001");
        em.flush();

        List<ConsoledHouseBlAirSummary> result =
                houseBlRepository.findConsoledAirSummariesByMasterBlId(masterA.getMasterBlId());

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.houseBlId() != null);
    }

    @Test
    @DisplayName("findConsoledAirSummariesByMasterBlId: HouseBlAir 확장 없는 HouseBl은 결과에서 제외된다 (INNER JOIN)")
    void findConsoledAirSummariesByMasterBlId_excludesHouseBlWithoutAirExt() {
        MasterBlJpaEntity master = persistMasterBl("AIR", Bound.EXP);
        em.flush();

        persistHouseBlWithAirExt(master, Bound.EXP, "HBL-WITH-AIR");
        persistHouseBlWithoutExt(master, JobDiv.AIR, Bound.EXP);
        em.flush();

        List<ConsoledHouseBlAirSummary> result =
                houseBlRepository.findConsoledAirSummariesByMasterBlId(master.getMasterBlId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).hblNo()).isEqualTo("HBL-WITH-AIR");
    }

    @Test
    @DisplayName("findConsoledAirSummariesByMasterBlId: createdAt 내림차순 정렬 (나중에 생성된 것이 먼저 반환된다)")
    void findConsoledAirSummariesByMasterBlId_sortedByHouseBlCreatedAtDesc() {
        MasterBlJpaEntity master = persistMasterBl("AIR", Bound.EXP);
        em.flush();

        persistHouseBlWithAirExt(master, Bound.EXP, "HBL-AIR-FIRST");
        em.flush();

        persistHouseBlWithAirExt(master, Bound.EXP, "HBL-AIR-SECOND");
        em.flush();
        em.clear();

        List<ConsoledHouseBlAirSummary> result =
                houseBlRepository.findConsoledAirSummariesByMasterBlId(master.getMasterBlId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).hblNo()).isEqualTo("HBL-AIR-SECOND");
    }

    @Test
    @DisplayName("findConsoledAirSummariesByMasterBlId: 10개 필드(houseBlId, hblNo, shipperCode, consigneeCode, docPartnerCode, pkgQty, pkgUnit, grossWeightKg, cbm, chargeWeightKg)가 올바르게 매핑된다")
    void findConsoledAirSummariesByMasterBlId_returnsAllTenFieldsCorrectly() {
        MasterBlJpaEntity master = persistMasterBl("AIR", Bound.EXP);
        em.flush();

        persistHouseBlWithAirExt(master, Bound.EXP, "HBL-AIR-FULL");
        em.flush();

        ConsoledHouseBlAirSummary a =
                houseBlRepository.findConsoledAirSummariesByMasterBlId(master.getMasterBlId()).get(0);

        assertThat(a.houseBlId()).isNotNull();
        assertThat(a.hblNo()).isEqualTo("HBL-AIR-FULL");
        assertThat(a.shipperCode()).isEqualTo("SHIP01");
        assertThat(a.consigneeCode()).isEqualTo("CONS01");
        assertThat(a.docPartnerCode()).isEqualTo("DOC01");
        assertThat(a.pkgQty()).isEqualTo(3);
        assertThat(a.pkgUnit()).isEqualTo("PKG");
        assertThat(a.grossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(30));
        assertThat(a.cbm()).isEqualByComparingTo(BigDecimal.valueOf(0.3));
        assertThat(a.chargeWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(45));
    }

    @Test
    @DisplayName("findConsoledAirSummariesByMasterBlId: 매칭 0건 → 빈 리스트(null 아님)")
    void findConsoledAirSummariesByMasterBlId_zeroMatches_returnsEmptyList() {
        List<ConsoledHouseBlAirSummary> result =
                houseBlRepository.findConsoledAirSummariesByMasterBlId(9999L);

        assertThat(result).isNotNull().isEmpty();
    }
}
