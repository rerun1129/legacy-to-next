package com.freightos.fms.adapter.out.persistence.airhouse;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.airhouse.AirHouseFilter;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.application.airhouse.projection.AirHouseSummary;
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
 * AirHouseRepositoryImpl의 검색 로직을 H2 in-memory DB로 검증한다.
 * searchAirHouseSummaries가 jobDiv=AIR row만 반환하는지 확인하는 데 중점을 둔다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, AirHouseRepositoryImpl.class})
class AirHouseRepositoryImplSliceTest {

    @Autowired
    private AirHouseRepositoryCustom airHouseRepositoryCustom;

    @Autowired
    private TestEntityManager em;

    // ── 헬퍼 메서드 ─────────────────────────────────────────────────

    private HouseBlJpaEntity persistHouseBl(JobDiv jobDiv, Bound bound) {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(jobDiv);
        jpa.setBound(bound);
        em.persist(jpa);
        return jpa;
    }

    private HouseBlJpaEntity persistHouseBlWithAirExt(Bound bound, String hblNo, String airlineCode) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.AIR);
        house.setBound(bound);
        house.setHblNo(hblNo);
        house.setShipperCode("SHIP01");
        house.setConsigneeCode("CONS01");
        house.setNotifyCode("NOTIFY01");
        house.setDocPartnerCode("DOC01");
        house.setPolCode("KRPUS");
        house.setPodCode("USNYC");
        house.setEtd("20251201");
        house.setEta("20251210");
        house.setPkgQty(5);
        house.setPkgUnit("CTN");
        house.setGrossWeightKg(BigDecimal.valueOf(100));
        house.setCbm(BigDecimal.valueOf(1));
        em.persist(house);

        HouseBlAirJpaEntity air = new HouseBlAirJpaEntity();
        air.setHouseBl(house);
        air.setAirlineCode(airlineCode);
        em.persist(air);

        return house;
    }

    private HouseBlJpaEntity persistHouseBlWithMblAndRef(Bound bound, String mblNo, String masterRefNo) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.AIR);
        house.setBound(bound);
        house.setMblNo(mblNo);
        house.setMasterRefNo(masterRefNo);
        house.setShipperCode("SHIP02");
        house.setConsigneeCode("CONS02");
        house.setNotifyCode("NOTIFY02");
        house.setPolCode("KRSEL");
        house.setPodCode("JPNRT");
        house.setEtd("20251201");
        house.setEta("20251210");
        house.setPkgQty(1);
        house.setPkgUnit("CTN");
        house.setGrossWeightKg(BigDecimal.valueOf(10));
        house.setCbm(BigDecimal.valueOf(1));
        em.persist(house);
        return house;
    }

    /** 신규 필드 없이 of() 호출 시 사용하는 null-fill 헬퍼 */
    private static AirHouseFilter filterWithBoundAndHblNo(Bound bound, String hblNo) {
        return AirHouseFilter.of(bound, null, null, null, null, hblNo, null, null, null, null, null, null, null, null, null, null);
    }

    private static AirHouseFilter filterWithBoundOnly(Bound bound) {
        return AirHouseFilter.of(bound, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    // ── 테스트 케이스 ────────────────────────────────────────────────

    @Test
    @DisplayName("searchAirHouseSummaries: jobDiv=AIR row만 반환되고 SEA/TRUCK는 제외된다")
    void searchAirHouseSummaries_returnsOnlyAirJobDiv() {
        persistHouseBl(JobDiv.SEA, Bound.EXP);
        persistHouseBl(JobDiv.TRUCK, Bound.EXP);
        persistHouseBlWithAirExt(Bound.EXP, "AIR-001", "AIRLINE01");
        persistHouseBlWithAirExt(Bound.EXP, "AIR-002", "AIRLINE02");
        em.flush();

        AirHouseFilter filter = filterWithBoundOnly(Bound.EXP);
        PagedResult<AirHouseSummary> result = airHouseRepositoryCustom.searchAirHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("searchAirHouseSummaries: airlineCode 필드가 올바르게 매핑된다")
    void searchAirHouseSummaries_mapsAllFieldsCorrectly() {
        persistHouseBlWithAirExt(Bound.EXP, "AIR-FULL", "AIRLINE-X");
        em.flush();

        AirHouseFilter filter = filterWithBoundAndHblNo(Bound.EXP, "AIR-FULL");
        PagedResult<AirHouseSummary> result = airHouseRepositoryCustom.searchAirHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        AirHouseSummary s = result.getContent().get(0);
        assertThat(s.id()).isNotNull();
        assertThat(s.hblNo()).isEqualTo("AIR-FULL");
        assertThat(s.bound()).isEqualTo("EXP");
        assertThat(s.polCode()).isEqualTo("KRPUS");
        assertThat(s.podCode()).isEqualTo("USNYC");
        assertThat(s.shipperCode()).isEqualTo("SHIP01");
        assertThat(s.consigneeCode()).isEqualTo("CONS01");
        assertThat(s.notifyCode()).isEqualTo("NOTIFY01");
        assertThat(s.docPartnerCode()).isEqualTo("DOC01");
        assertThat(s.airlineCode()).isEqualTo("AIRLINE-X");
        assertThat(s.pkgQty()).isEqualTo(5);
        assertThat(s.grossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(s.hblNo()).isNotNull();
    }

    @Test
    @DisplayName("searchAirHouseSummaries: air 확장 없는 AIR jobDiv row는 airlineCode=null로 반환된다 (LEFT JOIN)")
    void searchAirHouseSummaries_airExtAbsent_airlineCodeIsNull() {
        persistHouseBl(JobDiv.AIR, Bound.EXP);
        em.flush();

        AirHouseFilter filter = filterWithBoundOnly(Bound.EXP);
        PagedResult<AirHouseSummary> result = airHouseRepositoryCustom.searchAirHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).airlineCode()).isNull();
    }

    @Test
    @DisplayName("searchAirHouseSummaries: 매칭 0건 → 빈 리스트(null 아님)")
    void searchAirHouseSummaries_zeroMatches_returnsEmptyPagedResult() {
        AirHouseFilter filter = filterWithBoundAndHblNo(Bound.EXP, "NONEXISTENT");
        PagedResult<AirHouseSummary> result = airHouseRepositoryCustom.searchAirHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotNull().isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("searchAirHouseSummaries: bound 필터가 필수로 동작한다 — EXP 필터는 EXP만, IMP 필터는 IMP만 반환")
    void searchAirHouseSummaries_filtersByBound_bothExpAndImp() {
        persistHouseBlWithAirExt(Bound.EXP, "AIR-EXP", "AIR-EXP");
        persistHouseBlWithAirExt(Bound.IMP, "AIR-IMP", "AIR-IMP");
        em.flush();

        AirHouseFilter expFilter = filterWithBoundOnly(Bound.EXP);
        PagedResult<AirHouseSummary> expResult = airHouseRepositoryCustom.searchAirHouseSummaries(expFilter, PageRequest.of(0, 10));

        assertThat(expResult.getContent()).hasSize(1);
        assertThat(expResult.getContent().get(0).bound()).isEqualTo("EXP");

        AirHouseFilter impFilter = filterWithBoundOnly(Bound.IMP);
        PagedResult<AirHouseSummary> impResult = airHouseRepositoryCustom.searchAirHouseSummaries(impFilter, PageRequest.of(0, 10));

        assertThat(impResult.getContent()).hasSize(1);
        assertThat(impResult.getContent().get(0).bound()).isEqualTo("IMP");
    }

    @Test
    @DisplayName("masterAwbContains: masterAwbKind=MBL 시 mblNo로 contains 검색된다")
    void masterAwbContains_MBL_kind_uses_mblNo() {
        persistHouseBlWithMblAndRef(Bound.EXP, "TESTMBL", "OTHERREF");
        em.flush();

        AirHouseFilter filter = AirHouseFilter.of(Bound.EXP, null, null, "MBL", "TESTM", null, null, null, null, null, null, null, null, null, null, null);
        PagedResult<AirHouseSummary> result = airHouseRepositoryCustom.searchAirHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("masterAwbContains: masterAwbKind=REF 시 masterRefNo로 contains 검색된다")
    void masterAwbContains_REF_kind_uses_masterRefNo() {
        persistHouseBlWithMblAndRef(Bound.EXP, "OTHERMBL", "TESTREF");
        em.flush();

        AirHouseFilter filter = AirHouseFilter.of(Bound.EXP, null, null, "REF", "TESTRE", null, null, null, null, null, null, null, null, null, null, null);
        PagedResult<AirHouseSummary> result = airHouseRepositoryCustom.searchAirHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}
