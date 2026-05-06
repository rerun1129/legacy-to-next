package com.freightos.fms.adapter.out.persistence.airmaster;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.airmaster.AirMasterFilter;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.application.airmaster.projection.AirMasterSummary;
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
 * AirMasterRepositoryImpl의 검색 로직을 H2 in-memory DB로 검증한다.
 * searchAirMasterSummaries가 jobDiv=AIR row만 반환하는지 확인하는 데 중점을 둔다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, AirMasterRepositoryImpl.class})
class AirMasterRepositoryImplSliceTest {

    @Autowired
    private AirMasterRepositoryCustom airMasterRepositoryCustom;

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

    private MasterBlJpaEntity persistMasterBlWithAirExt(Bound bound, String mblNo, String airlineCode) {
        MasterBlJpaEntity master = new MasterBlJpaEntity();
        master.setJobDiv(MasterBlJobDiv.AIR);
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

        MasterBlAirJpaEntity air = new MasterBlAirJpaEntity();
        air.setMasterBl(master);
        air.setAirlineCode(airlineCode);
        em.persist(air);

        return master;
    }

    private MasterBlJpaEntity persistMasterBlWithMblAndRef(Bound bound, String mblNo, String masterRefNo) {
        MasterBlJpaEntity master = new MasterBlJpaEntity();
        master.setJobDiv(MasterBlJobDiv.AIR);
        master.setBound(bound);
        master.setMblNo(mblNo);
        master.setMasterRefNo(masterRefNo);
        master.setShipperCode("SHIP02");
        master.setConsigneeCode("CONS02");
        master.setNotifyCode("NOTIFY02");
        master.setPolCode("KRSEL");
        master.setPodCode("JPNRT");
        master.setEtd("20251201");
        master.setEta("20251210");
        master.setPkgQty(1);
        master.setPkgUnit("CTN");
        master.setGrossWeightKg(BigDecimal.valueOf(10));
        master.setCbm(BigDecimal.valueOf(1));
        em.persist(master);
        return master;
    }

    private static AirMasterFilter filterWithBoundOnly(Bound bound) {
        return AirMasterFilter.of(bound, null, null, null, null, null, null, null, null, null);
    }

    private static AirMasterFilter filterWithBoundAndMblNo(Bound bound, String mblNo) {
        return AirMasterFilter.of(bound, null, null, "MBL", mblNo, null, null, null, null, null);
    }

    // ── 테스트 케이스 ────────────────────────────────────────────────

    @Test
    @DisplayName("searchAirMasterSummaries: jobDiv=AIR row만 반환되고 SEA는 제외된다")
    void searchAirMasterSummaries_returnsOnlyAirJobDiv() {
        persistMasterBl(MasterBlJobDiv.SEA, Bound.EXP);
        persistMasterBlWithAirExt(Bound.EXP, "MAWB-001", "AIRLINE01");
        persistMasterBlWithAirExt(Bound.EXP, "MAWB-002", "AIRLINE02");
        em.flush();

        AirMasterFilter filter = filterWithBoundOnly(Bound.EXP);
        PagedResult<AirMasterSummary> result = airMasterRepositoryCustom.searchAirMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("searchAirMasterSummaries: airlineCode 필드가 올바르게 매핑된다")
    void searchAirMasterSummaries_mapsAllFieldsCorrectly() {
        persistMasterBlWithAirExt(Bound.EXP, "MAWB-FULL", "AIRLINE-X");
        em.flush();

        AirMasterFilter filter = filterWithBoundAndMblNo(Bound.EXP, "MAWB-FULL");
        PagedResult<AirMasterSummary> result = airMasterRepositoryCustom.searchAirMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        AirMasterSummary s = result.getContent().get(0);
        assertThat(s.id()).isNotNull();
        assertThat(s.mblNo()).isEqualTo("MAWB-FULL");
        assertThat(s.bound()).isEqualTo("EXP");
        assertThat(s.polCode()).isEqualTo("KRPUS");
        assertThat(s.podCode()).isEqualTo("USNYC");
        assertThat(s.shipperCode()).isEqualTo("SHIP01");
        assertThat(s.consigneeCode()).isEqualTo("CONS01");
        assertThat(s.notifyCode()).isEqualTo("NOTIFY01");
        assertThat(s.airlineCode()).isEqualTo("AIRLINE-X");
        assertThat(s.pkgQty()).isEqualTo(5);
        assertThat(s.grossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("searchAirMasterSummaries: air 확장 없는 AIR jobDiv row는 airlineCode=null로 반환된다 (LEFT JOIN)")
    void searchAirMasterSummaries_airExtAbsent_airlineCodeIsNull() {
        persistMasterBl(MasterBlJobDiv.AIR, Bound.EXP);
        em.flush();

        AirMasterFilter filter = filterWithBoundOnly(Bound.EXP);
        PagedResult<AirMasterSummary> result = airMasterRepositoryCustom.searchAirMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).airlineCode()).isNull();
    }

    @Test
    @DisplayName("searchAirMasterSummaries: 매칭 0건 → 빈 리스트(null 아님)")
    void searchAirMasterSummaries_zeroMatches_returnsEmptyPagedResult() {
        AirMasterFilter filter = filterWithBoundAndMblNo(Bound.EXP, "NONEXISTENT");
        PagedResult<AirMasterSummary> result = airMasterRepositoryCustom.searchAirMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotNull().isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("searchAirMasterSummaries: bound 필터가 필수로 동작한다 — EXP 필터는 EXP만, IMP 필터는 IMP만 반환")
    void searchAirMasterSummaries_filtersByBound_bothExpAndImp() {
        persistMasterBlWithAirExt(Bound.EXP, "MAWB-EXP", "AIR-EXP");
        persistMasterBlWithAirExt(Bound.IMP, "MAWB-IMP", "AIR-IMP");
        em.flush();

        AirMasterFilter expFilter = filterWithBoundOnly(Bound.EXP);
        PagedResult<AirMasterSummary> expResult = airMasterRepositoryCustom.searchAirMasterSummaries(expFilter, PageRequest.of(0, 10));

        assertThat(expResult.getContent()).hasSize(1);
        assertThat(expResult.getContent().get(0).bound()).isEqualTo("EXP");

        AirMasterFilter impFilter = filterWithBoundOnly(Bound.IMP);
        PagedResult<AirMasterSummary> impResult = airMasterRepositoryCustom.searchAirMasterSummaries(impFilter, PageRequest.of(0, 10));

        assertThat(impResult.getContent()).hasSize(1);
        assertThat(impResult.getContent().get(0).bound()).isEqualTo("IMP");
    }

    @Test
    @DisplayName("masterAwbContains: masterAwbKind=MBL 시 mblNo로 contains 검색된다")
    void masterAwbContains_MBL_kind_uses_mblNo() {
        persistMasterBlWithMblAndRef(Bound.EXP, "TESTMBL", "OTHERREF");
        em.flush();

        AirMasterFilter filter = AirMasterFilter.of(Bound.EXP, null, null, "MBL", "TESTM", null, null, null, null, null);
        PagedResult<AirMasterSummary> result = airMasterRepositoryCustom.searchAirMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("masterAwbContains: masterAwbKind=REF 시 masterRefNo로 contains 검색된다")
    void masterAwbContains_REF_kind_uses_masterRefNo() {
        persistMasterBlWithMblAndRef(Bound.EXP, "OTHERMBL", "TESTREF");
        em.flush();

        AirMasterFilter filter = AirMasterFilter.of(Bound.EXP, null, null, "REF", "TESTRE", null, null, null, null, null);
        PagedResult<AirMasterSummary> result = airMasterRepositoryCustom.searchAirMasterSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}
