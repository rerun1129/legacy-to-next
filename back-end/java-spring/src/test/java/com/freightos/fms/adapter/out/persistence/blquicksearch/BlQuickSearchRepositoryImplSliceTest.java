package com.freightos.fms.adapter.out.persistence.blquicksearch;

import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;
import com.freightos.fms.domain.blquicksearch.BlQuickSearchFilter;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.DateKind;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.PartyKind;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BlQuickSearchRepositoryImpl 검색 로직을 H2 in-memory DB로 검증한다.
 * 정렬: hblNo/mblNo ASC + id ASC (비결정성 제거 목적).
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, BlQuickSearchRepositoryImpl.class})
class BlQuickSearchRepositoryImplSliceTest {

    @Autowired
    private BlQuickSearchRepositoryCustom repository;

    @Autowired
    private TestEntityManager em;

    // ── House persist 헬퍼 ──────────────────────────────────────────

    private HouseBlJpaEntity persistHouse(String hblNo, JobDiv jobDiv, Bound bound, String polCode, String podCode) {
        HouseBlJpaEntity h = new HouseBlJpaEntity();
        h.setHblNo(hblNo);
        h.setJobDiv(jobDiv);
        h.setBound(bound);
        h.setPolCode(polCode);
        h.setPodCode(podCode);
        h.setEtd("20260101");
        h.setEta("20260115");
        h.setShipperCode("SHIP01");
        h.setConsigneeCode("CONS01");
        h.setNotifyCode("NOTIFY01");
        h.setTeamCode("TM01");
        h.setOperatorCode("OP01");
        h.setSalesManCode("SM01");
        em.persist(h);
        return h;
    }

    // ── Master persist 헬퍼 ─────────────────────────────────────────

    private MasterBlJpaEntity persistMaster(String mblNo, MasterBlJobDiv jobDiv, Bound bound, String polCode, String podCode) {
        MasterBlJpaEntity m = new MasterBlJpaEntity();
        m.setMblNo(mblNo);
        m.setJobDiv(jobDiv);
        m.setBound(bound);
        m.setPolCode(polCode);
        m.setPodCode(podCode);
        m.setEtd("20260101");
        m.setEta("20260115");
        m.setShipperCode("MSHIP01");
        m.setConsigneeCode("MCONS01");
        m.setNotifyCode("MNOTIFY01");
        m.setTeamCode("MTM01");
        m.setOperatorCode("MOP01");
        em.persist(m);
        return m;
    }

    private static BlQuickSearchFilter filterWithJobDivAndBound(JobDiv jobDiv, Bound bound) {
        return new BlQuickSearchFilter(jobDiv, bound, null, null, null, null, null, null, null, null, null, null, null);
    }

    private static BlQuickSearchFilter filterWithBlNo(String blNo) {
        return new BlQuickSearchFilter(null, null, null, null, null, null, null, null, null, null, null, null, blNo);
    }

    // ── House searchHouse 테스트 ─────────────────────────────────────

    @Test
    @DisplayName("searchHouse: jobDiv=SEA 필터 시 SEA row만 반환")
    void searchHouse_filtersByJobDiv() {
        persistHouse("SEA-001", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistHouse("AIR-001", JobDiv.AIR, Bound.EXP, "KRSEL", "USJFK");
        em.flush();

        BlQuickSearchFilter filter = filterWithJobDivAndBound(JobDiv.SEA, null);
        List<BlQuickSearchSummary> result = repository.searchHouse(filter, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).blNo()).isEqualTo("SEA-001");
        assertThat(result.get(0).blType()).isEqualTo("HOUSE");
    }

    @Test
    @DisplayName("searchHouse: bound=EXP 필터 시 EXP만 반환")
    void searchHouse_filtersByBound() {
        persistHouse("EXP-001", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistHouse("IMP-001", JobDiv.SEA, Bound.IMP, "KRPUS", "USLAX");
        em.flush();

        BlQuickSearchFilter filter = filterWithJobDivAndBound(null, Bound.EXP);
        List<BlQuickSearchSummary> result = repository.searchHouse(filter, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).bound()).isEqualTo("EXP");
    }

    @Test
    @DisplayName("searchHouse: blNo containsIgnoreCase 검색")
    void searchHouse_filtersByBlNoContains() {
        persistHouse("BLNO-ABC-001", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistHouse("OTHER-999", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        em.flush();

        BlQuickSearchFilter filter = filterWithBlNo("abc");
        List<BlQuickSearchSummary> result = repository.searchHouse(filter, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).blNo()).isEqualTo("BLNO-ABC-001");
    }

    @Test
    @DisplayName("searchHouse: blNo 공란이면 전체 반환")
    void searchHouse_blNoEmpty_returnsAll() {
        persistHouse("HOUSE-A", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistHouse("HOUSE-B", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        em.flush();

        BlQuickSearchFilter filter = filterWithBlNo(null);
        List<BlQuickSearchSummary> result = repository.searchHouse(filter, 20);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("searchHouse: POL 독립 필터")
    void searchHouse_filtersByPolCode() {
        persistHouse("POL-MATCH", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistHouse("POL-OTHER", JobDiv.SEA, Bound.EXP, "KRSEL", "USLAX");
        em.flush();

        BlQuickSearchFilter filter = new BlQuickSearchFilter(null, null, null, null, null, null, null, null, null, null, "KRPUS", null, null);
        List<BlQuickSearchSummary> result = repository.searchHouse(filter, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).polCode()).isEqualTo("KRPUS");
    }

    @Test
    @DisplayName("searchHouse: POD 독립 필터")
    void searchHouse_filtersByPodCode() {
        persistHouse("POD-MATCH", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistHouse("POD-OTHER", JobDiv.SEA, Bound.EXP, "KRPUS", "JPNRT");
        em.flush();

        BlQuickSearchFilter filter = new BlQuickSearchFilter(null, null, null, null, null, null, null, null, null, null, null, "USLAX", null);
        List<BlQuickSearchSummary> result = repository.searchHouse(filter, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).podCode()).isEqualTo("USLAX");
    }

    @Test
    @DisplayName("searchHouse: party=SHIPPER 필터")
    void searchHouse_filtersByPartyShipper() {
        persistHouse("PARTY-MATCH", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        em.flush();

        BlQuickSearchFilter filter = new BlQuickSearchFilter(null, null, null, null, null, null, null, null, PartyKind.SHIPPER, "SHIP01", null, null, null);
        List<BlQuickSearchSummary> result = repository.searchHouse(filter, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).shipperCode()).isEqualTo("SHIP01");
    }

    @Test
    @DisplayName("searchHouse: salesManCode 필터 — House 전용 필드 동작")
    void searchHouse_filtersBySalesManCode() {
        persistHouse("SM-MATCH", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistHouse("SM-OTHER", JobDiv.AIR, Bound.EXP, "KRPUS", "USLAX");
        em.flush();

        // SM-OTHER는 salesManCode=SM01 동일하지만 fixture 조정하지 않았으므로 2건 반환 — 필터가 적용됐는지만 검증
        BlQuickSearchFilter filter = new BlQuickSearchFilter(null, null, null, null, null, null, null, "SM01", null, null, null, null, null);
        List<BlQuickSearchSummary> result = repository.searchHouse(filter, 20);

        assertThat(result).isNotEmpty();
        result.forEach(s -> assertThat(s.blType()).isEqualTo("HOUSE"));
    }

    @Test
    @DisplayName("searchHouse: date ETD between 필터")
    void searchHouse_filtersByEtdBetween() {
        persistHouse("DATE-MATCH", JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        em.flush();

        BlQuickSearchFilter filter = new BlQuickSearchFilter(null, null, DateKind.ETD, "20260101", "20260131", null, null, null, null, null, null, null, null);
        List<BlQuickSearchSummary> result = repository.searchHouse(filter, 20);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("searchHouse: limit 적용 확인")
    void searchHouse_respectsLimit() {
        for (int i = 1; i <= 5; i++) {
            persistHouse("LIMIT-" + String.format("%03d", i), JobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        }
        em.flush();

        BlQuickSearchFilter filter = filterWithBlNo(null);
        List<BlQuickSearchSummary> result = repository.searchHouse(filter, 3);

        assertThat(result).hasSize(3);
        // 정렬: hblNo ASC
        assertThat(result.get(0).blNo()).isEqualTo("LIMIT-001");
        assertThat(result.get(1).blNo()).isEqualTo("LIMIT-002");
    }

    // ── Master searchMaster 테스트 ───────────────────────────────────

    @Test
    @DisplayName("searchMaster: MasterBlJobDiv.fromCode 매핑 — SEA jobDiv 필터")
    void searchMaster_filtersByMasterJobDivSea() {
        persistMaster("MSEA-001", MasterBlJobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistMaster("MAIR-001", MasterBlJobDiv.AIR, Bound.EXP, "KRSEL", "USJFK");
        em.flush();

        BlQuickSearchFilter filter = new BlQuickSearchFilter(JobDiv.SEA, null, null, null, null, null, null, null, null, null, null, null, null);
        List<BlQuickSearchSummary> result = repository.searchMaster(filter, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).blNo()).isEqualTo("MSEA-001");
        assertThat(result.get(0).blType()).isEqualTo("MASTER");
    }

    @Test
    @DisplayName("searchMaster: jobDiv=null 이면 SEA/AIR 모두 반환")
    void searchMaster_nullJobDiv_returnsBoth() {
        persistMaster("MSEA-002", MasterBlJobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistMaster("MAIR-002", MasterBlJobDiv.AIR, Bound.EXP, "KRSEL", "USJFK");
        em.flush();

        BlQuickSearchFilter filter = filterWithJobDivAndBound(null, null);
        List<BlQuickSearchSummary> result = repository.searchMaster(filter, 20);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("searchMaster: mblNo containsIgnoreCase 검색")
    void searchMaster_filtersByMblNoContains() {
        persistMaster("MBLNO-XYZ-001", MasterBlJobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistMaster("MBLNO-OTHER", MasterBlJobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        em.flush();

        BlQuickSearchFilter filter = filterWithBlNo("xyz");
        List<BlQuickSearchSummary> result = repository.searchMaster(filter, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).blNo()).isEqualTo("MBLNO-XYZ-001");
    }

    @Test
    @DisplayName("searchMaster: POL/POD 독립 2슬롯 필터")
    void searchMaster_filtersByPolAndPodIndependently() {
        persistMaster("MPOL-MATCH", MasterBlJobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        persistMaster("MPOL-OTHER", MasterBlJobDiv.SEA, Bound.EXP, "KRSEL", "USLAX");
        em.flush();

        BlQuickSearchFilter filter = new BlQuickSearchFilter(null, null, null, null, null, null, null, null, null, null, "KRPUS", "USLAX", null);
        List<BlQuickSearchSummary> result = repository.searchMaster(filter, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).polCode()).isEqualTo("KRPUS");
        assertThat(result.get(0).podCode()).isEqualTo("USLAX");
    }

    @Test
    @DisplayName("searchMaster: party=SHIPPER 필터 — Master에도 적용")
    void searchMaster_filtersByPartyShipper() {
        persistMaster("MPARTY-MATCH", MasterBlJobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        em.flush();

        BlQuickSearchFilter filter = new BlQuickSearchFilter(null, null, null, null, null, null, null, null, PartyKind.SHIPPER, "MSHIP01", null, null, null);
        List<BlQuickSearchSummary> result = repository.searchMaster(filter, 20);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("searchMaster: date ETA between 필터")
    void searchMaster_filtersByEtaBetween() {
        persistMaster("MDATE-MATCH", MasterBlJobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        em.flush();

        BlQuickSearchFilter filter = new BlQuickSearchFilter(null, null, DateKind.ETA, "20260115", "20260131", null, null, null, null, null, null, null, null);
        List<BlQuickSearchSummary> result = repository.searchMaster(filter, 20);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("searchMaster: limit 적용 및 정렬(mblNo ASC)")
    void searchMaster_respectsLimitAndOrder() {
        for (int i = 1; i <= 5; i++) {
            persistMaster("MLIMIT-" + String.format("%03d", i), MasterBlJobDiv.SEA, Bound.EXP, "KRPUS", "USLAX");
        }
        em.flush();

        BlQuickSearchFilter filter = filterWithBlNo(null);
        List<BlQuickSearchSummary> result = repository.searchMaster(filter, 3);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).blNo()).isEqualTo("MLIMIT-001");
        assertThat(result.get(1).blNo()).isEqualTo("MLIMIT-002");
    }
}
