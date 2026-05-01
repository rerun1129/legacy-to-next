package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.*;
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
    private final HouseBlMapper mapper = new HouseBlMapper(cargoMapper, docMapper);

    // ── AIR ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("toAirDomain: dim 2개/scheduleLeg 1개/license 1개/desc 있으면 모두 도메인에 포함, containers는 빈 컬렉션")
    void toAirDomain_includesDimsScheduleLegsLicensesDesc() {
        HouseBlJpaEntity jpa = airJpa(1L);
        jpa.syncDims(List.of(dimJpa(jpa), dimJpa(jpa)));
        jpa.syncScheduleLegs(List.of(legJpa(jpa)));
        jpa.syncLicenses(List.of(licenseJpa(jpa)));
        jpa.replaceDesc(descJpa(jpa));

        HouseBlAir domain = mapper.toAirDomain(jpa, null);

        assertThat(domain.getDims()).hasSize(2);
        assertThat(domain.getScheduleLegs()).hasSize(1);
        assertThat(domain.getLicenses()).hasSize(1);
        assertThat(domain.getDesc()).isNotNull();
        assertThat(domain.getContainers()).isEmpty();
    }

    @Test
    @DisplayName("toAirDomain: 자식 컬렉션이 모두 비어 있으면 빈 리스트, desc는 null")
    void toAirDomain_emptyChildCollections_returnsEmptyListsAndNullDesc() {
        HouseBlJpaEntity jpa = airJpa(2L);

        HouseBlAir domain = mapper.toAirDomain(jpa, null);

        assertThat(domain.getDims()).isEmpty();
        assertThat(domain.getScheduleLegs()).isEmpty();
        assertThat(domain.getLicenses()).isEmpty();
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

        HouseBlAir domain = mapper.toAirDomain(jpa, null);

        assertThat(domain.getDims().get(0).getLengthCm()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(domain.getDims().get(1).getLengthCm()).isEqualByComparingTo(BigDecimal.valueOf(20));
    }

    // ── SEA ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("toSeaDomain: containers/licenses/desc 포함, dims와 scheduleLegs는 빈 컬렉션")
    void toSeaDomain_includesContainersLicensesDesc_excludesDimsAndLegs() {
        HouseBlJpaEntity jpa = seaJpa(4L);
        jpa.syncContainers(List.of(containerJpa(jpa), containerJpa(jpa)));
        jpa.syncLicenses(List.of(licenseJpa(jpa)));
        jpa.replaceDesc(descJpa(jpa));

        HouseBlSea domain = mapper.toSeaDomain(jpa, null);

        assertThat(domain.getContainers()).hasSize(2);
        assertThat(domain.getLicenses()).hasSize(1);
        assertThat(domain.getDesc()).isNotNull();
        assertThat(domain.getDims()).isEmpty();
        assertThat(domain.getScheduleLegs()).isEmpty();
    }

    @Test
    @DisplayName("toSeaDomain: 자식 컬렉션 없으면 빈 리스트, desc null")
    void toSeaDomain_emptyChildren_returnsEmptyCollectionsAndNullDesc() {
        HouseBlJpaEntity jpa = seaJpa(5L);

        HouseBlSea domain = mapper.toSeaDomain(jpa, null);

        assertThat(domain.getContainers()).isEmpty();
        assertThat(domain.getLicenses()).isEmpty();
        assertThat(domain.getDesc()).isNull();
    }

    // ── TRUCK ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("toTruckDomain: dims만 포함, containers/scheduleLegs/licenses 빈 컬렉션, desc null")
    void toTruckDomain_includesDimsOnly_excludesContainersLegsLicensesAndDesc() {
        HouseBlJpaEntity jpa = truckJpa(6L);
        jpa.syncDims(List.of(dimJpa(jpa), dimJpa(jpa)));

        HouseBlTruck domain = mapper.toTruckDomain(jpa, null);

        assertThat(domain.getDims()).hasSize(2);
        assertThat(domain.getContainers()).isEmpty();
        assertThat(domain.getScheduleLegs()).isEmpty();
        assertThat(domain.getLicenses()).isEmpty();
        assertThat(domain.getDesc()).isNull();
    }

    @Test
    @DisplayName("toTruckDomain: dims 없으면 빈 리스트")
    void toTruckDomain_noDims_returnsEmptyDimList() {
        HouseBlJpaEntity jpa = truckJpa(7L);

        HouseBlTruck domain = mapper.toTruckDomain(jpa, null);

        assertThat(domain.getDims()).isEmpty();
    }

    // ── NON_BL ───────────────────────────────────────────────────────

    @Test
    @DisplayName("toNonBlDomain: containers/dims/desc 포함, scheduleLegs/licenses 빈 컬렉션")
    void toNonBlDomain_includesContainersDimsDesc_excludesLicensesAndLegs() {
        HouseBlJpaEntity jpa = nonBlJpa(8L);
        jpa.syncContainers(List.of(containerJpa(jpa)));
        jpa.syncDims(List.of(dimJpa(jpa)));
        jpa.replaceDesc(descJpa(jpa));

        HouseBlNonBl domain = mapper.toNonBlDomain(jpa, null);

        assertThat(domain.getContainers()).hasSize(1);
        assertThat(domain.getDims()).hasSize(1);
        assertThat(domain.getDesc()).isNotNull();
        assertThat(domain.getScheduleLegs()).isEmpty();
        assertThat(domain.getLicenses()).isEmpty();
    }

    @Test
    @DisplayName("toNonBlDomain: 자식 컬렉션 없으면 빈 리스트, desc null")
    void toNonBlDomain_emptyChildren_returnsEmptyCollectionsAndNullDesc() {
        HouseBlJpaEntity jpa = nonBlJpa(9L);

        HouseBlNonBl domain = mapper.toNonBlDomain(jpa, null);

        assertThat(domain.getContainers()).isEmpty();
        assertThat(domain.getDims()).isEmpty();
        assertThat(domain.getDesc()).isNull();
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
        jpa.setHouseBl(parent);
        return jpa;
    }

    private HouseBlScheduleLegJpaEntity legJpa(HouseBlJpaEntity parent) {
        HouseBlScheduleLegJpaEntity jpa = new HouseBlScheduleLegJpaEntity();
        jpa.setHouseBl(parent);
        jpa.setToCode("NRT");
        jpa.setOnBoardDt("20260501");
        jpa.setArrivalDt("20260502");
        return jpa;
    }

    private HouseBlLicenseJpaEntity licenseJpa(HouseBlJpaEntity parent) {
        HouseBlLicenseJpaEntity jpa = new HouseBlLicenseJpaEntity();
        jpa.setHouseBl(parent);
        return jpa;
    }

    private HouseBlDescJpaEntity descJpa(HouseBlJpaEntity parent) {
        HouseBlDescJpaEntity jpa = new HouseBlDescJpaEntity();
        jpa.setHouseBl(parent);
        return jpa;
    }

    private HouseBlContainerJpaEntity containerJpa(HouseBlJpaEntity parent) {
        // ContainerNumber는 AAAA1234567 형식(ISO 6346) 요구
        return HouseBlContainerJpaEntity.of(parent, "ABCD1234567", null, 20);
    }
}
