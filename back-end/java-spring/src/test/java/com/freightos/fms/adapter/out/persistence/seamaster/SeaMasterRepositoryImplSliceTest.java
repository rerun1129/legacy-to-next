package com.freightos.fms.adapter.out.persistence.seamaster;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.seamaster.SeaMasterFilter;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.application.seamaster.projection.SeaMasterSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SeaMasterRepositoryImpl의 검색 로직을 H2 in-memory DB로 검증한다.
 * searchSeaMasterSummaries가 jobDiv=SEA row만 반환하는지 확인하는 데 중점을 둔다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, SeaMasterRepositoryImpl.class})
class SeaMasterRepositoryImplSliceTest {

    @Autowired
    private SeaMasterRepositoryCustom seaMasterRepositoryCustom;

    @Autowired
    private TestEntityManager em;

    // ── 헬퍼 메서드 ─────────────────────────────────────────────────

    private MasterBlJpaEntity persistMasterBl(MasterBlJobDiv jobDiv, Bound bound) {
        MasterBlJpaEntity jpa = new MasterBlJpaEntity();
        jpa.setJobDiv(jobDiv);
        jpa.setBound(bound);
        em.persist(jpa);
        return jpa;
    }

    private MasterBlJpaEntity persistMasterBlWithSeaExt(Bound bound, String mblNo, String linerCode) {
        MasterBlJpaEntity master = new MasterBlJpaEntity();
        master.setJobDiv(MasterBlJobDiv.SEA);
        master.setBound(bound);
        master.setMblNo(mblNo);
        master.setShipperCode("SHIP01");
        master.setConsigneeCode("CONS01");
        master.setNotifyCode("NOTIFY01");
        master.setPolCode("KRPUS");
        master.setPodCode("USNYC");
        master.setEtd("20251201");
        master.setEta("20251210");
        master.setPkgQty(5);
        master.setPkgUnit("CTN");
        master.setGrossWeightKg(BigDecimal.valueOf(100));
        master.setCbm(BigDecimal.valueOf(1));
        em.persist(master);

        MasterBlSeaJpaEntity sea = new MasterBlSeaJpaEntity();
        sea.setMasterBl(master);
        // liner_code는 VARCHAR(20) — 7자 이내 값 사용
        sea.setLinerCode(linerCode);
        em.persist(sea);

        return master;
    }

    private MasterBlJpaEntity persistMasterBlWithMblAndRef(Bound bound, String mblNo, String masterRefNo) {
        MasterBlJpaEntity master = new MasterBlJpaEntity();
        master.setJobDiv(MasterBlJobDiv.SEA);
        master.setBound(bound);
        master.setMblNo(mblNo);
        master.setMasterRefNo(masterRefNo);
        master.setShipperCode("SHIP02");
        master.setConsigneeCode("CONS02");
        master.setNotifyCode("NOTIFY02");
        master.setPolCode("KRSEL");
        master.setPodCode("JPYOK");
        master.setEtd("20251201");
        master.setEta("20251210");
        master.setPkgQty(1);
        master.setPkgUnit("CTN");
        master.setGrossWeightKg(BigDecimal.valueOf(10));
        master.setCbm(BigDecimal.valueOf(1));
        em.persist(master);
        return master;
    }

    private static SeaMasterFilter filterWithBoundOnly(Bound bound) {
        return SeaMasterFilter.of(bound, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private static SeaMasterFilter filterWithBoundAndMblNo(Bound bound, String mblNo) {
        return SeaMasterFilter.of(bound, null, null, "MBL", mblNo, null, null, null, null, null, null, null, null);
    }

    // ── 테스트 케이스 ────────────────────────────────────────────────

    @Test
    @DisplayName("searchSeaMasterSummaries: jobDiv=SEA row만 반환되고 AIR는 제외된다")
    void searchSeaMasterSummaries_returnsOnlySeaJobDiv() {
        persistMasterBl(MasterBlJobDiv.AIR, Bound.EXP);
        persistMasterBlWithSeaExt(Bound.EXP, "SEABL-001", "SEA-EXP");
        persistMasterBlWithSeaExt(Bound.EXP, "SEABL-002", "SEA-IMP");
        em.flush();

        SeaMasterFilter filter = filterWithBoundOnly(Bound.EXP);
        PagedResult<SeaMasterSummary> result = seaMasterRepositoryCustom.searchSeaMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("searchSeaMasterSummaries: linerCode 필드가 올바르게 매핑된다")
    void searchSeaMasterSummaries_mapsAllFieldsCorrectly() {
        persistMasterBlWithSeaExt(Bound.EXP, "SEABL-FULL", "LINER-X");
        em.flush();

        SeaMasterFilter filter = filterWithBoundAndMblNo(Bound.EXP, "SEABL-FULL");
        PagedResult<SeaMasterSummary> result = seaMasterRepositoryCustom.searchSeaMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        SeaMasterSummary s = result.getContent().get(0);
        assertThat(s.id()).isNotNull();
        assertThat(s.mblNo()).isEqualTo("SEABL-FULL");
        assertThat(s.bound()).isEqualTo("EXP");
        assertThat(s.polCode()).isEqualTo("KRPUS");
        assertThat(s.podCode()).isEqualTo("USNYC");
        assertThat(s.shipperCode()).isEqualTo("SHIP01");
        assertThat(s.consigneeCode()).isEqualTo("CONS01");
        assertThat(s.notifyCode()).isEqualTo("NOTIFY01");
        assertThat(s.linerCode()).isEqualTo("LINER-X");
        assertThat(s.pkgQty()).isEqualTo(5);
        assertThat(s.grossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("searchSeaMasterSummaries: sea 확장 없는 SEA jobDiv row는 linerCode=null로 반환된다 (LEFT JOIN)")
    void searchSeaMasterSummaries_seaExtAbsent_linerCodeIsNull() {
        persistMasterBl(MasterBlJobDiv.SEA, Bound.EXP);
        em.flush();

        SeaMasterFilter filter = filterWithBoundOnly(Bound.EXP);
        PagedResult<SeaMasterSummary> result = seaMasterRepositoryCustom.searchSeaMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).linerCode()).isNull();
    }

    @Test
    @DisplayName("searchSeaMasterSummaries: 매칭 0건 → 빈 리스트(null 아님)")
    void searchSeaMasterSummaries_zeroMatches_returnsEmptyPagedResult() {
        SeaMasterFilter filter = filterWithBoundAndMblNo(Bound.EXP, "NONEXISTENT");
        PagedResult<SeaMasterSummary> result = seaMasterRepositoryCustom.searchSeaMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotNull().isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("searchSeaMasterSummaries: bound 필터가 필수로 동작한다 — EXP 필터는 EXP만, IMP 필터는 IMP만 반환")
    void searchSeaMasterSummaries_filtersByBound_bothExpAndImp() {
        persistMasterBlWithSeaExt(Bound.EXP, "SEABL-EXP", "SEA-EXP");
        persistMasterBlWithSeaExt(Bound.IMP, "SEABL-IMP", "SEA-IMP");
        em.flush();

        SeaMasterFilter expFilter = filterWithBoundOnly(Bound.EXP);
        PagedResult<SeaMasterSummary> expResult = seaMasterRepositoryCustom.searchSeaMasterSummaries(expFilter, PageRequest.of(0, 10));

        assertThat(expResult.getContent()).hasSize(1);
        assertThat(expResult.getContent().get(0).bound()).isEqualTo("EXP");

        SeaMasterFilter impFilter = filterWithBoundOnly(Bound.IMP);
        PagedResult<SeaMasterSummary> impResult = seaMasterRepositoryCustom.searchSeaMasterSummaries(impFilter, PageRequest.of(0, 10));

        assertThat(impResult.getContent()).hasSize(1);
        assertThat(impResult.getContent().get(0).bound()).isEqualTo("IMP");
    }

    @Test
    @DisplayName("masterBlContains: masterBlKind=MBL 시 mblNo로 contains 검색된다")
    void masterBlContains_MBL_kind_uses_mblNo() {
        persistMasterBlWithMblAndRef(Bound.EXP, "TESTMBL", "OTHERREF");
        em.flush();

        SeaMasterFilter filter = SeaMasterFilter.of(Bound.EXP, null, null, "MBL", "TESTM", null, null, null, null, null, null, null, null);
        PagedResult<SeaMasterSummary> result = seaMasterRepositoryCustom.searchSeaMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("masterBlContains: masterBlKind=REF 시 masterRefNo로 contains 검색된다")
    void masterBlContains_REF_kind_uses_masterRefNo() {
        persistMasterBlWithMblAndRef(Bound.EXP, "OTHERMBL", "TESTREF");
        em.flush();

        SeaMasterFilter filter = SeaMasterFilter.of(Bound.EXP, null, null, "REF", "TESTRE", null, null, null, null, null, null, null, null);
        PagedResult<SeaMasterSummary> result = seaMasterRepositoryCustom.searchSeaMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}
