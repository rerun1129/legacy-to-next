package com.freightos.fms.adapter.out.persistence.nonbl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlNonBlJpaEntity;
import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.entity.HouseBlNonBl;
import com.freightos.fms.domain.nonbl.projection.NonBlSummary;
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
 * NonBlRepositoryImpl의 검색 로직을 H2 in-memory DB로 검증한다.
 * searchNonBlSummaries가 jobDiv=NON_BL row만 반환하는지 확인하는 데 중점을 둔다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, NonBlRepositoryImpl.class})
class NonBlRepositoryImplSliceTest {

    @Autowired
    private NonBlRepositoryCustom nonBlRepositoryCustom;

    @Autowired
    private TestEntityManager em;

    // ── 헬퍼 메서드 ──────────────────────────────────────────────

    private HouseBlJpaEntity persistHouseBl(JobDiv jobDiv, Bound bound) {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(jobDiv);
        jpa.setBound(bound);
        em.persist(jpa);
        return jpa;
    }

    private HouseBlJpaEntity persistHouseBlWithNonBlExt(Bound bound, String hblNo,
                                                         String vesselName, String linerCode) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.NON_BL);
        house.setBound(bound);
        house.setHblNo(hblNo);
        house.setShipperCode("SHIP01");
        house.setConsigneeCode("CONS01");
        house.setNotifyCode("NOTIFY01");
        house.setPolCode("KRPUS");
        house.setPodCode("USNYC");
        house.setEtd("20251201");
        house.setEta("20251210");
        house.setPkgQty(3);
        house.setPkgUnit("PKG");
        house.setGrossWeightKg(BigDecimal.valueOf(50));
        house.setCbm(BigDecimal.valueOf(0.5));
        em.persist(house);

        HouseBlNonBlJpaEntity nonBl = new HouseBlNonBlJpaEntity();
        nonBl.setHouseBl(house);
        nonBl.setWorkDivision(HouseBlNonBl.WorkDivision.SEA);
        nonBl.setVesselName(vesselName);
        nonBl.setLinerCode(linerCode);
        nonBl.setLinerName("LINER-" + linerCode);
        nonBl.setVoyageNo("VOY-001");
        em.persist(nonBl);

        return house;
    }

    // ── 테스트 케이스 ───────────���───────────��─────────────────────

    @Test
    @DisplayName("searchNonBlSummaries: jobDiv=NON_BL row만 반환되고 SEA/TRUCK은 제외된다")
    void searchNonBlSummaries_returnsOnlyNonBlJobDiv() {
        persistHouseBl(JobDiv.SEA, Bound.EXP);
        persistHouseBl(JobDiv.TRUCK, Bound.EXP);
        persistHouseBlWithNonBlExt(Bound.EXP, "NONBL-001", "VESSEL-A", "LINER01");
        persistHouseBlWithNonBlExt(Bound.EXP, "NONBL-002", "VESSEL-B", "LINER02");
        em.flush();

        NonBlFilter filter = NonBlFilter.of(Bound.EXP, null, null, null, null, null, null, null, null, null, null);
        PagedResult<NonBlSummary> result = nonBlRepositoryCustom.searchNonBlSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        result.getContent().forEach(s -> assertThat(s.jobDiv()).isEqualTo(JobDiv.NON_BL));
    }

    @Test
    @DisplayName("searchNonBlSummaries: innerJoin이므로 NonBl 확장 없는 NON_BL row는 제외된다")
    void searchNonBlSummaries_excludesNonBlWithoutExtension() {
        // NON_BL jobDiv지만 house_bl_non_bl 확장 행 없음
        persistHouseBl(JobDiv.NON_BL, Bound.EXP);
        persistHouseBlWithNonBlExt(Bound.EXP, "NONBL-WITH-EXT", "VESSEL-C", "LINER03");
        em.flush();

        NonBlFilter filter = NonBlFilter.of(Bound.EXP, null, null, null, null, null, null, null, null, null, null);
        PagedResult<NonBlSummary> result = nonBlRepositoryCustom.searchNonBlSummaries(filter, PageRequest.of(0, 10));

        // innerJoin이므로 확장 없는 NON_BL은 제외
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).hblNo()).isEqualTo("NONBL-WITH-EXT");
    }

    @Test
    @DisplayName("searchNonBlSummaries: 22개 필드가 올바르게 매핑된다")
    void searchNonBlSummaries_mapsAllFieldsCorrectly() {
        persistHouseBlWithNonBlExt(Bound.EXP, "NONBL-FULL", "VESSEL-FULL", "LIN99");
        em.flush();

        NonBlFilter filter = NonBlFilter.of(null, "NONBL-FULL", null, null, null, null, null, null, null, null, null);
        PagedResult<NonBlSummary> result = nonBlRepositoryCustom.searchNonBlSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        NonBlSummary s = result.getContent().get(0);
        assertThat(s.houseBlId()).isNotNull();
        assertThat(s.hblNo()).isEqualTo("NONBL-FULL");
        assertThat(s.jobDiv()).isEqualTo(JobDiv.NON_BL);
        assertThat(s.bound()).isEqualTo(Bound.EXP);
        assertThat(s.polCode()).isEqualTo("KRPUS");
        assertThat(s.podCode()).isEqualTo("USNYC");
        assertThat(s.shipperCode()).isEqualTo("SHIP01");
        assertThat(s.consigneeCode()).isEqualTo("CONS01");
        assertThat(s.pkgQty()).isEqualTo(3);
        assertThat(s.pkgUnit()).isEqualTo("PKG");
        assertThat(s.grossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(s.cbm()).isEqualByComparingTo(BigDecimal.valueOf(0.5));
        assertThat(s.vesselName()).isEqualTo("VESSEL-FULL");
        assertThat(s.linerCode()).isEqualTo("LIN99");
        assertThat(s.linerName()).isEqualTo("LINER-LIN99");
        assertThat(s.voyageNo()).isEqualTo("VOY-001");
        assertThat(s.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("searchNonBlSummaries: linerCode 부분 일치 필터가 동작한다")
    void searchNonBlSummaries_filtersByLinerCode() {
        persistHouseBlWithNonBlExt(Bound.EXP, "NONBL-LIN1", "VESSEL-X", "MATCH01");
        persistHouseBlWithNonBlExt(Bound.EXP, "NONBL-LIN2", "VESSEL-Y", "OTHER99");
        em.flush();

        NonBlFilter filter = NonBlFilter.of(null, null, null, null, "MATCH", null, null, null, null, null, null);
        PagedResult<NonBlSummary> result = nonBlRepositoryCustom.searchNonBlSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).linerCode()).isEqualTo("MATCH01");
    }

    @Test
    @DisplayName("searchNonBlSummaries: 매칭 0건 → 빈 리스트(null 아님)")
    void searchNonBlSummaries_zeroMatches_returnsEmptyPagedResult() {
        NonBlFilter filter = NonBlFilter.of(Bound.EXP, "NONEXISTENT", null, null, null, null, null, null, null, null, null);
        PagedResult<NonBlSummary> result = nonBlRepositoryCustom.searchNonBlSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotNull().isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("searchNonBlSummaries: bound 필터가 동작한다")
    void searchNonBlSummaries_filtersByBound() {
        persistHouseBlWithNonBlExt(Bound.EXP, "NONBL-EXP", "VESSEL-EXP", "LIN-EXP");
        persistHouseBlWithNonBlExt(Bound.IMP, "NONBL-IMP", "VESSEL-IMP", "LIN-IMP");
        em.flush();

        NonBlFilter filter = NonBlFilter.of(Bound.EXP, null, null, null, null, null, null, null, null, null, null);
        PagedResult<NonBlSummary> result = nonBlRepositoryCustom.searchNonBlSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).bound()).isEqualTo(Bound.EXP);
    }
}
