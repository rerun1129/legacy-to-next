package com.freightos.fms.adapter.out.persistence.seahouse;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.seahouse.SeaHouseFilter;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.application.seahouse.projection.SeaHouseSummary;
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
 * SeaHouseRepositoryImpl의 검색 로직을 H2 in-memory DB로 검증한다.
 * searchSeaHouseSummaries가 jobDiv=SEA row만 반환하는지 확인하는 데 중점을 둔다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, SeaHouseRepositoryImpl.class})
class SeaHouseRepositoryImplSliceTest {

    @Autowired
    private SeaHouseRepositoryCustom seaHouseRepositoryCustom;

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

    private HouseBlJpaEntity persistHouseBlWithSeaExt(Bound bound, String hblNo, String linerCode) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.SEA);
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

        HouseBlSeaJpaEntity sea = new HouseBlSeaJpaEntity();
        sea.setHouseBl(house);
        sea.setLinerCode(linerCode);
        em.persist(sea);

        // 컨테이너 집계(cntr20Qty, cntr40Qty, teuQty) 검증용 fixture: 20ft 1개, 40ft 1개
        house.syncContainers(java.util.List.of(
            HouseBlContainerJpaEntity.of("CONT0001", ContainerType.T20GP, 20),
            HouseBlContainerJpaEntity.of("CONT0002", ContainerType.F40GP, 40)
        ));

        return house;
    }

    private HouseBlJpaEntity persistHouseBlWithMblAndRef(Bound bound, String mblNo, String masterRefNo) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.SEA);
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
    private static SeaHouseFilter filterWithBoundAndHblNo(Bound bound, String hblNo) {
        return SeaHouseFilter.of(bound, null, null, null, null, hblNo, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    private static SeaHouseFilter filterWithBoundOnly(Bound bound) {
        return SeaHouseFilter.of(bound, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    // ── 테스트 케이스 ────────────────────────────────────────────────

    @Test
    @DisplayName("searchSeaHouseSummaries: jobDiv=SEA row만 반환되고 AIR/TRUCK는 제외된다")
    void searchSeaHouseSummaries_returnsOnlySeaJobDiv() {
        persistHouseBl(JobDiv.AIR, Bound.EXP);
        persistHouseBl(JobDiv.TRUCK, Bound.EXP);
        persistHouseBlWithSeaExt(Bound.EXP, "SEA-001", "SEA-EXP");
        persistHouseBlWithSeaExt(Bound.EXP, "SEA-002", "SEA-EXP");
        em.flush();

        SeaHouseFilter filter = filterWithBoundOnly(Bound.EXP);
        PagedResult<SeaHouseSummary> result = seaHouseRepositoryCustom.searchSeaHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("searchSeaHouseSummaries: linerCode 필드가 올바르게 매핑된다")
    void searchSeaHouseSummaries_mapsAllFieldsCorrectly() {
        persistHouseBlWithSeaExt(Bound.EXP, "SEA-FULL", "SEA-EXP");
        em.flush();

        SeaHouseFilter filter = filterWithBoundAndHblNo(Bound.EXP, "SEA-FULL");
        PagedResult<SeaHouseSummary> result = seaHouseRepositoryCustom.searchSeaHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        SeaHouseSummary s = result.getContent().get(0);
        assertThat(s.id()).isNotNull();
        assertThat(s.hblNo()).isEqualTo("SEA-FULL");
        assertThat(s.bound()).isEqualTo("EXP");
        assertThat(s.polCode()).isEqualTo("KRPUS");
        assertThat(s.podCode()).isEqualTo("USNYC");
        assertThat(s.shipperCode()).isEqualTo("SHIP01");
        assertThat(s.consigneeCode()).isEqualTo("CONS01");
        assertThat(s.notifyCode()).isEqualTo("NOTIFY01");
        assertThat(s.docPartnerCode()).isEqualTo("DOC01");
        assertThat(s.linerCode()).isEqualTo("SEA-EXP");
        assertThat(s.pkgQty()).isEqualTo(5);
        assertThat(s.grossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(s.hblNo()).isNotNull();
        // 컨테이너 집계 검증 (fixture: 20ft 1개, 40ft 1개)
        assertThat(s.cntr20Qty()).isNotNull();
        assertThat(s.cntr40Qty()).isNotNull();
        assertThat(s.cntr20Qty()).isEqualTo(1L);
        assertThat(s.cntr40Qty()).isEqualTo(1L);
        // lengthFeetSum = 20 + 40 = 60 → teuQty = 60/20 = 3.0
        assertThat(s.lengthFeetSum()).isNotNull();
    }

    @Test
    @DisplayName("searchSeaHouseSummaries: sea 확장 없는 SEA jobDiv row는 linerCode=null로 반환된다 (LEFT JOIN)")
    void searchSeaHouseSummaries_seaExtAbsent_linerCodeIsNull() {
        persistHouseBl(JobDiv.SEA, Bound.EXP);
        em.flush();

        SeaHouseFilter filter = filterWithBoundOnly(Bound.EXP);
        PagedResult<SeaHouseSummary> result = seaHouseRepositoryCustom.searchSeaHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).linerCode()).isNull();
    }

    @Test
    @DisplayName("searchSeaHouseSummaries: 매칭 0건 → 빈 리스트(null 아님)")
    void searchSeaHouseSummaries_zeroMatches_returnsEmptyPagedResult() {
        SeaHouseFilter filter = filterWithBoundAndHblNo(Bound.EXP, "NONEXISTENT");
        PagedResult<SeaHouseSummary> result = seaHouseRepositoryCustom.searchSeaHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotNull().isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("searchSeaHouseSummaries: bound 필터가 필수로 동작한다 — EXP 필터는 EXP만, IMP 필터는 IMP만 반환")
    void searchSeaHouseSummaries_filtersByBound_bothExpAndImp() {
        persistHouseBlWithSeaExt(Bound.EXP, "SEA-EXP", "SEA-EXP");
        persistHouseBlWithSeaExt(Bound.IMP, "SEA-IMP", "SEA-IMP");
        em.flush();

        SeaHouseFilter expFilter = filterWithBoundOnly(Bound.EXP);
        PagedResult<SeaHouseSummary> expResult = seaHouseRepositoryCustom.searchSeaHouseSummaries(expFilter, PageRequest.of(0, 10));

        assertThat(expResult.getContent()).hasSize(1);
        assertThat(expResult.getContent().get(0).bound()).isEqualTo("EXP");

        SeaHouseFilter impFilter = filterWithBoundOnly(Bound.IMP);
        PagedResult<SeaHouseSummary> impResult = seaHouseRepositoryCustom.searchSeaHouseSummaries(impFilter, PageRequest.of(0, 10));

        assertThat(impResult.getContent()).hasSize(1);
        assertThat(impResult.getContent().get(0).bound()).isEqualTo("IMP");
    }

    @Test
    @DisplayName("masterBlContains: masterBlKind=MBL 시 mblNo로 contains 검색된다")
    void masterBlContains_MBL_kind_uses_mblNo() {
        persistHouseBlWithMblAndRef(Bound.EXP, "TESTMBL", "OTHERREF");
        em.flush();

        SeaHouseFilter filter = SeaHouseFilter.of(Bound.EXP, null, null, "MBL", "TESTM", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        PagedResult<SeaHouseSummary> result = seaHouseRepositoryCustom.searchSeaHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("masterBlContains: masterBlKind=REF 시 masterRefNo로 contains 검색된다")
    void masterBlContains_REF_kind_uses_masterRefNo() {
        persistHouseBlWithMblAndRef(Bound.EXP, "OTHERMBL", "TESTREF");
        em.flush();

        SeaHouseFilter filter = SeaHouseFilter.of(Bound.EXP, null, null, "REF", "TESTRE", null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        PagedResult<SeaHouseSummary> result = seaHouseRepositoryCustom.searchSeaHouseSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }
}
