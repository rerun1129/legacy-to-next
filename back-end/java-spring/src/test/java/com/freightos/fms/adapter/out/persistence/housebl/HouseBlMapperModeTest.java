package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 오늘(2026-04-30) 리팩토링 커밋에서 dims/scheduleLegs/licenses/desc가
 * HouseBl 본체로 승격된 후 모드별 toDomain의 통합 매핑을 검증한다.
 * 자식 필드 단위 매핑은 HouseBlMapperTest에서 별도 검증.
 */
class HouseBlMapperModeTest {

    private final HouseBlCargoMapper cargoMapper = new HouseBlCargoMapper();
    private final HouseBlDocMapper docMapper = new HouseBlDocMapper();
    private final HouseBlJpaToDomainMapper mapper = new HouseBlJpaToDomainMapper(cargoMapper, docMapper);

    // ── AIR ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("toAirDomain: dim 2개/scheduleLeg 1개/desc 있으면 모두 도메인에 포함, containers는 빈 컬렉션")
    void toAirDomain_includesDimsScheduleLegsLicensesDesc() {
        HouseBlJpaEntity jpa = airJpa(1L);
        jpa.syncDims(List.of(dimJpa(jpa), dimJpa(jpa)));
        // scheduleLegs는 HouseBlAirJpaEntity 소유 — airJpa에 sync
        HouseBlAirJpaEntity airJpa = new HouseBlAirJpaEntity();
        airJpa.syncScheduleLegs(List.of(legJpa()));
        // desc는 3번째 인자로 명시 전달 — 부모 매핑 없음
        HouseBlDescJpaEntity desc = descJpa(jpa);

        HouseBlAir domain = mapper.toAirDomain(jpa, airJpa, desc);

        assertThat(domain.getDims()).hasSize(2);
        assertThat(domain.getScheduleLegs()).hasSize(1);
        assertThat(domain.getDesc()).isNotNull();
        assertThat(domain.getContainers()).isEmpty();
    }

    @Test
    @DisplayName("toAirDomain: 자식 컬렉션이 모두 비어 있으면 빈 리스트, desc는 null")
    void toAirDomain_emptyChildCollections_returnsEmptyListsAndNullDesc() {
        HouseBlJpaEntity jpa = airJpa(2L);

        HouseBlAir domain = mapper.toAirDomain(jpa, null, null);

        assertThat(domain.getDims()).isEmpty();
        assertThat(domain.getScheduleLegs()).isEmpty();
        assertThat(domain.getDesc()).isNull();
    }

    @Test
    @DisplayName("toAirDomain: dim 삽입 순서가 도메인 리스트 순서와 동일하게 유지된다")
    void toAirDomain_preservesDimOrder() {
        HouseBlJpaEntity jpa = airJpa(3L);
        HouseBlDimJpaEntity dim1 = dimJpa(jpa);
        dim1.setLengthCm(BigDecimal.valueOf(10));
        HouseBlDimJpaEntity dim2 = dimJpa(jpa);
        dim2.setLengthCm(BigDecimal.valueOf(20));
        jpa.syncDims(List.of(dim1, dim2));

        HouseBlAir domain = mapper.toAirDomain(jpa, null, null);

        assertThat(domain.getDims().get(0).getLengthCm()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(domain.getDims().get(1).getLengthCm()).isEqualByComparingTo(BigDecimal.valueOf(20));
    }

    // ── SEA ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("toSeaDomain: containers/desc 포함, dims는 빈 컬렉션")
    void toSeaDomain_includesContainersLicensesDesc_excludesDimsAndLegs() {
        HouseBlJpaEntity jpa = seaJpa(4L);
        jpa.syncContainers(List.of(containerJpa(jpa), containerJpa(jpa)));
        // desc는 3번째 인자로 명시 전달 — 부모 매핑 없음
        HouseBlDescJpaEntity desc = descJpa(jpa);

        HouseBlSea domain = mapper.toSeaDomain(jpa, null, desc);

        assertThat(domain.getContainers()).hasSize(2);
        assertThat(domain.getDesc()).isNotNull();
        assertThat(domain.getDims()).isEmpty();
    }

    @Test
    @DisplayName("toSeaDomain: 자식 컬렉션 없으면 빈 리스트, desc null")
    void toSeaDomain_emptyChildren_returnsEmptyCollectionsAndNullDesc() {
        HouseBlJpaEntity jpa = seaJpa(5L);

        HouseBlSea domain = mapper.toSeaDomain(jpa, null, null);

        assertThat(domain.getContainers()).isEmpty();
        assertThat(domain.getDesc()).isNull();
    }

    // ── TRUCK ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("toTruckDomain: dims만 포함, containers 빈 컬렉션, desc null. truckJpa null이면 truckOrders 빈 리스트")
    void toTruckDomain_includesDimsOnly_excludesContainersLegsLicensesAndDesc() {
        HouseBlJpaEntity jpa = truckJpa(6L);
        jpa.syncDims(List.of(dimJpa(jpa), dimJpa(jpa)));

        HouseBlTruck domain = mapper.toTruckDomain(jpa, null);

        assertThat(domain.getDims()).hasSize(2);
        assertThat(domain.getTruckOrders()).isEmpty();
        assertThat(domain.getContainers()).isEmpty();
        assertThat(domain.getDesc()).isNull();
    }

    @Test
    @DisplayName("toTruckDomain: dims 없으면 빈 리스트")
    void toTruckDomain_noDims_returnsEmptyDimList() {
        HouseBlJpaEntity jpa = truckJpa(7L);

        HouseBlTruck domain = mapper.toTruckDomain(jpa, null);

        assertThat(domain.getDims()).isEmpty();
    }

    @Test
    @DisplayName("toTruckDomain: truckJpa에 truckOrders 있으면 도메인에 포함된다")
    void toTruckDomain_truckJpaHasTruckOrders_includesThemInDomain() {
        HouseBlJpaEntity jpa = truckJpa(10L);
        HouseBlTruckOrderJpaEntity order = truckOrderJpa();
        HouseBlTruckJpaEntity truckJpa = new HouseBlTruckJpaEntity();
        truckJpa.syncTruckOrders(List.of(order));

        HouseBlTruck domain = mapper.toTruckDomain(jpa, truckJpa);

        assertThat(domain.getTruckOrders()).hasSize(1);
    }

    // ── NON_BL ───────────────────────────────────────────────────────

    @Test
    @DisplayName("toNonBlDomain: containers/dims 포함, desc는 null(house_bl_non_bl.remark 컬럼으로 이전됨)")
    void toNonBlDomain_includesContainersDims_descIsNull() {
        HouseBlJpaEntity jpa = nonBlJpa(8L);
        jpa.syncContainers(List.of(containerJpa(jpa)));
        jpa.syncDims(List.of(dimJpa(jpa)));

        HouseBlNonBlJpaEntity nonBlJpa = new HouseBlNonBlJpaEntity();
        nonBlJpa.setRemark("TEST_REMARK");

        HouseBlNonBl domain = mapper.toNonBlDomain(jpa, nonBlJpa);

        assertThat(domain.getContainers()).hasSize(1);
        assertThat(domain.getDims()).hasSize(1);
        // NON_BL은 desc를 사용하지 않음 — remark는 house_bl_non_bl 컬럼으로 관리됨
        assertThat(domain.getDesc()).isNull();
        assertThat(domain.getRemark()).isEqualTo("TEST_REMARK");
    }

    @Test
    @DisplayName("toNonBlDomain: 자식 컬렉션 없으면 빈 리스트, desc null, remark null")
    void toNonBlDomain_emptyChildren_returnsEmptyCollectionsAndNullDescAndNullRemark() {
        HouseBlJpaEntity jpa = nonBlJpa(9L);

        HouseBlNonBl domain = mapper.toNonBlDomain(jpa, null);

        assertThat(domain.getContainers()).isEmpty();
        assertThat(domain.getDims()).isEmpty();
        assertThat(domain.getDesc()).isNull();
        assertThat(domain.getRemark()).isNull();
    }

    // ── 픽스처 헬퍼 ─────────────────────────────────────────────────

    private HouseBlJpaEntity airJpa(Long id) {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setHouseBlId(id);
        jpa.setBound(Bound.EXP);
        jpa.setJobDiv(JobDiv.AIR);
        return jpa;
    }

    private HouseBlJpaEntity seaJpa(Long id) {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setHouseBlId(id);
        jpa.setBound(Bound.EXP);
        jpa.setJobDiv(JobDiv.SEA);
        return jpa;
    }

    private HouseBlJpaEntity truckJpa(Long id) {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setHouseBlId(id);
        jpa.setBound(Bound.EXP);
        jpa.setJobDiv(JobDiv.TRUCK);
        return jpa;
    }

    private HouseBlJpaEntity nonBlJpa(Long id) {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setHouseBlId(id);
        jpa.setBound(Bound.EXP);
        jpa.setJobDiv(JobDiv.NON_BL);
        return jpa;
    }

    private HouseBlDimJpaEntity dimJpa(HouseBlJpaEntity parent) {
        HouseBlDimJpaEntity jpa = new HouseBlDimJpaEntity();
        jpa.setHouseBlId(parent.getHouseBlId());
        return jpa;
    }

    private HouseBlScheduleLegJpaEntity legJpa() {
        HouseBlScheduleLegJpaEntity jpa = new HouseBlScheduleLegJpaEntity();
        jpa.setToCode("NRT");
        jpa.setOnBoardDt("20260501");
        jpa.setArrivalDt("20260502");
        return jpa;
    }

    private HouseBlDescJpaEntity descJpa(HouseBlJpaEntity parent) {
        HouseBlDescJpaEntity jpa = new HouseBlDescJpaEntity();
        jpa.setHouseBl(parent);
        return jpa;
    }

    private HouseBlContainerJpaEntity containerJpa(HouseBlJpaEntity parent) {
        // ContainerNumber는 AAAA1234567 형식(ISO 6346) 요구
        return HouseBlContainerJpaEntity.of("ABCD1234567", null, 20);
    }

    private HouseBlTruckOrderJpaEntity truckOrderJpa() {
        HouseBlTruckOrderJpaEntity jpa = new HouseBlTruckOrderJpaEntity();
        jpa.setTruckNo("TRUCK-01");
        return jpa;
    }
}
