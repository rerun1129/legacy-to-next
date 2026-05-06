package com.freightos.fms.adapter.out.persistence.truckbl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import com.freightos.common.config.QueryDslConfig;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.application.truckbl.projection.TruckBlSummary;
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
 * TruckBlRepositoryImpl의 검색 로직을 H2 in-memory DB로 검증한다.
 * searchTruckBlSummaries가 jobDiv=TRUCK row만 반환하는지 확인하는 데 중점을 둔다.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, TruckBlRepositoryImpl.class})
class TruckBlRepositoryImplSliceTest {

    @Autowired
    private TruckBlRepositoryCustom truckBlRepositoryCustom;

    @Autowired
    private TestEntityManager em;

    // ── 헬퍼 메서드 ──────────────────────────────────────────���───

    private HouseBlJpaEntity persistHouseBl(JobDiv jobDiv, Bound bound) {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(jobDiv);
        jpa.setBound(bound);
        em.persist(jpa);
        return jpa;
    }

    private HouseBlJpaEntity persistHouseBlWithTruckExt(Bound bound, String hblNo, String truckerCode) {
        HouseBlJpaEntity house = new HouseBlJpaEntity();
        house.setJobDiv(JobDiv.TRUCK);
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

        HouseBlTruckJpaEntity truck = new HouseBlTruckJpaEntity();
        truck.setHouseBl(house);
        truck.setTruckerCode(truckerCode);
        em.persist(truck);

        return house;
    }

    // ── 테스트 케이스 ─────────────────────────────��───────────────

    @Test
    @DisplayName("searchTruckBlSummaries: jobDiv=TRUCK row만 반환되고 SEA/AIR는 제외된다")
    void searchTruckBlSummaries_returnsOnlyTruckJobDiv() {
        persistHouseBl(JobDiv.SEA, Bound.EXP);
        persistHouseBl(JobDiv.AIR, Bound.EXP);
        persistHouseBlWithTruckExt(Bound.EXP, "TRUCK-001", "TRUCKER01");
        persistHouseBlWithTruckExt(Bound.EXP, "TRUCK-002", "TRUCKER02");
        em.flush();

        TruckBlFilter filter = TruckBlFilter.of(Bound.EXP, null, null, null, null, null, null, null, null, null);
        PagedResult<TruckBlSummary> result = truckBlRepositoryCustom.searchTruckBlSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        result.getContent().forEach(s -> assertThat(s.jobDiv()).isEqualTo("TRUCK"));
    }

    @Test
    @DisplayName("searchTruckBlSummaries: truckerCode 필드가 올바르게 매핑된다")
    void searchTruckBlSummaries_mapsAllFieldsCorrectly() {
        persistHouseBlWithTruckExt(Bound.EXP, "TRUCK-FULL", "TRUCKER-X");
        em.flush();

        TruckBlFilter filter = TruckBlFilter.of(null, "TRUCK-FULL", null, null, null, null, null, null, null, null);
        PagedResult<TruckBlSummary> result = truckBlRepositoryCustom.searchTruckBlSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        TruckBlSummary s = result.getContent().get(0);
        assertThat(s.id()).isNotNull();
        assertThat(s.hblNo()).isEqualTo("TRUCK-FULL");
        assertThat(s.jobDiv()).isEqualTo("TRUCK");
        assertThat(s.bound()).isEqualTo("EXP");
        assertThat(s.polCode()).isEqualTo("KRPUS");
        assertThat(s.podCode()).isEqualTo("USNYC");
        assertThat(s.shipperCode()).isEqualTo("SHIP01");
        assertThat(s.consigneeCode()).isEqualTo("CONS01");
        assertThat(s.notifyCode()).isEqualTo("NOTIFY01");
        assertThat(s.docPartnerCode()).isEqualTo("DOC01");
        assertThat(s.truckerCode()).isEqualTo("TRUCKER-X");
        assertThat(s.pkgQty()).isEqualTo(5);
        assertThat(s.grossWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(s.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("searchTruckBlSummaries: truck 확장 없는 TRUCK jobDiv row는 truckerCode=null로 반환된다 (LEFT JOIN)")
    void searchTruckBlSummaries_truckExtAbsent_truckerCodeIsNull() {
        persistHouseBl(JobDiv.TRUCK, Bound.EXP);
        em.flush();

        TruckBlFilter filter = TruckBlFilter.of(Bound.EXP, null, null, null, null, null, null, null, null, null);
        PagedResult<TruckBlSummary> result = truckBlRepositoryCustom.searchTruckBlSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).truckerCode()).isNull();
    }

    @Test
    @DisplayName("searchTruckBlSummaries: 매칭 0건 → 빈 리스트(null 아님)")
    void searchTruckBlSummaries_zeroMatches_returnsEmptyPagedResult() {
        TruckBlFilter filter = TruckBlFilter.of(Bound.EXP, "NONEXISTENT", null, null, null, null, null, null, null, null);
        PagedResult<TruckBlSummary> result = truckBlRepositoryCustom.searchTruckBlSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).isNotNull().isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("searchTruckBlSummaries: bound 필터가 동작한다")
    void searchTruckBlSummaries_filtersByBound() {
        persistHouseBlWithTruckExt(Bound.EXP, "TRUCK-EXP", "TRUCKER-EXP");
        persistHouseBlWithTruckExt(Bound.IMP, "TRUCK-IMP", "TRUCKER-IMP");
        em.flush();

        TruckBlFilter filter = TruckBlFilter.of(Bound.EXP, null, null, null, null, null, null, null, null, null);
        PagedResult<TruckBlSummary> result = truckBlRepositoryCustom.searchTruckBlSummaries(filter, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).bound()).isEqualTo("EXP");
    }
}
