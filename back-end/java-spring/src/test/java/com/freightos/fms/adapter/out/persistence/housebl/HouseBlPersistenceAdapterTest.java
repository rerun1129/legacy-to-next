package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlAirPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlNonBlPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlSeaPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlTruckPersistenceStrategy;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.SortDirection;
import com.freightos.common.model.PageRequest;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * HouseBlPersistenceAdapter dispatcher 동작 단위 테스트.
 * jobDiv별 Strategy의 saveExt/loadWithExt/deleteExt 호출 여부 + 부모 처리를 검증한다.
 * 각 Strategy 내부 Repository/Mapper 호출 검증은 Strategy 단위 테스트로 분리되었다.
 */
@ExtendWith(MockitoExtension.class)
class HouseBlPersistenceAdapterTest {

    @Mock private HouseBlRepository houseBlRepository;
    @Mock private HouseBlDomainToJpaMapper domainToJpaMapper;
    @Mock private HouseBlSeaPersistenceStrategy seaStrategy;
    @Mock private HouseBlAirPersistenceStrategy airStrategy;
    @Mock private HouseBlTruckPersistenceStrategy truckStrategy;
    @Mock private HouseBlNonBlPersistenceStrategy nonBlStrategy;

    @InjectMocks
    private HouseBlPersistenceAdapter adapter;

    // ── saveHouseBl — Strategy dispatch ────────────────────────────────

    @Test
    @DisplayName("saveHouseBl(SEA): seaStrategy.saveExt 호출, air/truck/nonBl Strategy 미호출")
    void saveHouseBl_sea_dispatchesToSeaStrategy() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = new HouseBlJpaEntity();
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(seaStrategy.saveExt(eq(sea), eq(savedJpa))).willReturn(sea);

        adapter.saveHouseBl(sea);

        then(seaStrategy).should().saveExt(eq(sea), eq(savedJpa));
        then(airStrategy).should(never()).saveExt(any(), any());
        then(truckStrategy).should(never()).saveExt(any(), any());
        then(nonBlStrategy).should(never()).saveExt(any(), any());
    }

    @Test
    @DisplayName("saveHouseBl(AIR): airStrategy.saveExt 호출, sea/truck/nonBl Strategy 미호출")
    void saveHouseBl_air_dispatchesToAirStrategy() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = new HouseBlJpaEntity();
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(airStrategy.saveExt(eq(air), eq(savedJpa))).willReturn(air);

        adapter.saveHouseBl(air);

        then(airStrategy).should().saveExt(eq(air), eq(savedJpa));
        then(seaStrategy).should(never()).saveExt(any(), any());
        then(truckStrategy).should(never()).saveExt(any(), any());
        then(nonBlStrategy).should(never()).saveExt(any(), any());
    }

    @Test
    @DisplayName("saveHouseBl(TRUCK): truckStrategy.saveExt 호출, sea/air/nonBl Strategy 미호출")
    void saveHouseBl_truck_dispatchesToTruckStrategy() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = new HouseBlJpaEntity();
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(truckStrategy.saveExt(eq(truck), eq(savedJpa))).willReturn(truck);

        adapter.saveHouseBl(truck);

        then(truckStrategy).should().saveExt(eq(truck), eq(savedJpa));
        then(seaStrategy).should(never()).saveExt(any(), any());
        then(airStrategy).should(never()).saveExt(any(), any());
        then(nonBlStrategy).should(never()).saveExt(any(), any());
    }

    @Test
    @DisplayName("saveHouseBl(NON_BL): nonBlStrategy.saveExt 호출, sea/air/truck Strategy 미호출")
    void saveHouseBl_nonBl_dispatchesToNonBlStrategy() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        HouseBlJpaEntity savedJpa = new HouseBlJpaEntity();
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(nonBlStrategy.saveExt(eq(nonBl), eq(savedJpa))).willReturn(nonBl);

        adapter.saveHouseBl(nonBl);

        then(nonBlStrategy).should().saveExt(eq(nonBl), eq(savedJpa));
        then(seaStrategy).should(never()).saveExt(any(), any());
        then(airStrategy).should(never()).saveExt(any(), any());
        then(truckStrategy).should(never()).saveExt(any(), any());
    }

    // ── saveHouseBl — 부모 처리 동작 ──────────────────────────────────

    @Test
    @DisplayName("saveHouseBl: domain.getId() != null + repository.existsById() false → ResourceNotFoundException")
    void saveHouseBl_withExistingId_whenNotFound_throwsResourceNotFound() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sea.assignIdentity(777L, null, null, null, null);
        given(houseBlRepository.existsById(777L)).willReturn(false);

        assertThatThrownBy(() -> adapter.saveHouseBl(sea))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── findHouseBlById — Strategy dispatch ────────────────────────────

    @Test
    @DisplayName("findHouseBlById(SEA): seaStrategy.loadWithExt 호출, 결과 반환")
    void findHouseBlById_sea_dispatchesToSeaStrategy() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.SEA);
        jpa.setHouseBlId(2L);
        HouseBlSea expected = HouseBlSea.create(Bound.EXP);
        given(houseBlRepository.findById(2L)).willReturn(Optional.of(jpa));
        given(seaStrategy.loadWithExt(jpa)).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(2L);

        assertThat(result).contains(expected);
        then(seaStrategy).should().loadWithExt(jpa);
        then(airStrategy).should(never()).loadWithExt(any());
        then(truckStrategy).should(never()).loadWithExt(any());
        then(nonBlStrategy).should(never()).loadWithExt(any());
    }

    @Test
    @DisplayName("findHouseBlById(AIR): airStrategy.loadWithExt 호출, 결과 반환")
    void findHouseBlById_air_dispatchesToAirStrategy() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.AIR);
        jpa.setHouseBlId(1L);
        HouseBlAir expected = HouseBlAir.create(Bound.EXP);
        given(houseBlRepository.findById(1L)).willReturn(Optional.of(jpa));
        given(airStrategy.loadWithExt(jpa)).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(1L);

        assertThat(result).contains(expected);
        then(airStrategy).should().loadWithExt(jpa);
        then(seaStrategy).should(never()).loadWithExt(any());
        then(truckStrategy).should(never()).loadWithExt(any());
        then(nonBlStrategy).should(never()).loadWithExt(any());
    }

    @Test
    @DisplayName("findHouseBlById(TRUCK): truckStrategy.loadWithExt 호출, 결과 반환")
    void findHouseBlById_truck_dispatchesToTruckStrategy() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.TRUCK);
        jpa.setHouseBlId(3L);
        HouseBlTruck expected = HouseBlTruck.create(Bound.EXP);
        given(houseBlRepository.findById(3L)).willReturn(Optional.of(jpa));
        given(truckStrategy.loadWithExt(jpa)).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(3L);

        assertThat(result).contains(expected);
        then(truckStrategy).should().loadWithExt(jpa);
        then(seaStrategy).should(never()).loadWithExt(any());
        then(airStrategy).should(never()).loadWithExt(any());
        then(nonBlStrategy).should(never()).loadWithExt(any());
    }

    @Test
    @DisplayName("findHouseBlById(NON_BL): nonBlStrategy.loadWithExt 호출, 결과 반환")
    void findHouseBlById_nonBl_dispatchesToNonBlStrategy() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.NON_BL);
        jpa.setHouseBlId(4L);
        HouseBlNonBl expected = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        given(houseBlRepository.findById(4L)).willReturn(Optional.of(jpa));
        given(nonBlStrategy.loadWithExt(jpa)).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(4L);

        assertThat(result).contains(expected);
        then(nonBlStrategy).should().loadWithExt(jpa);
        then(seaStrategy).should(never()).loadWithExt(any());
        then(airStrategy).should(never()).loadWithExt(any());
        then(truckStrategy).should(never()).loadWithExt(any());
    }

    @Test
    @DisplayName("findHouseBlById: 존재하지 않는 ID → Optional.empty 반환, Strategy 미호출")
    void findHouseBlById_notFound_returnsEmpty() {
        given(houseBlRepository.findById(999L)).willReturn(Optional.empty());

        Optional<HouseBl> result = adapter.findHouseBlById(999L);

        assertThat(result).isEmpty();
        then(seaStrategy).should(never()).loadWithExt(any());
        then(airStrategy).should(never()).loadWithExt(any());
        then(truckStrategy).should(never()).loadWithExt(any());
        then(nonBlStrategy).should(never()).loadWithExt(any());
    }

    // ── deleteByIdAndJobDiv — Strategy dispatch + 부모 deleteByIdBulk ─────

    @Test
    @DisplayName("deleteByIdAndJobDiv(SEA): seaStrategy.deleteExt → houseBlRepository.deleteByIdBulk 순서, 다른 Strategy 미호출")
    void deleteByIdAndJobDiv_sea_dispatchesToSeaStrategyThenDeletesParent() {
        adapter.deleteByIdAndJobDiv(50L, JobDiv.SEA);

        then(seaStrategy).should().deleteExt(50L);
        then(houseBlRepository).should().deleteByIdBulk(50L);
        then(airStrategy).should(never()).deleteExt(any());
        then(truckStrategy).should(never()).deleteExt(any());
        then(nonBlStrategy).should(never()).deleteExt(any());
    }

    @Test
    @DisplayName("deleteByIdAndJobDiv(AIR): airStrategy.deleteExt → houseBlRepository.deleteByIdBulk 순서, 다른 Strategy 미호출")
    void deleteByIdAndJobDiv_air_dispatchesToAirStrategyThenDeletesParent() {
        adapter.deleteByIdAndJobDiv(51L, JobDiv.AIR);

        then(airStrategy).should().deleteExt(51L);
        then(houseBlRepository).should().deleteByIdBulk(51L);
        then(seaStrategy).should(never()).deleteExt(any());
        then(truckStrategy).should(never()).deleteExt(any());
        then(nonBlStrategy).should(never()).deleteExt(any());
    }

    @Test
    @DisplayName("deleteByIdAndJobDiv(TRUCK): truckStrategy.deleteExt → houseBlRepository.deleteByIdBulk 순서, 다른 Strategy 미호출")
    void deleteByIdAndJobDiv_truck_dispatchesToTruckStrategyThenDeletesParent() {
        adapter.deleteByIdAndJobDiv(52L, JobDiv.TRUCK);

        then(truckStrategy).should().deleteExt(52L);
        then(houseBlRepository).should().deleteByIdBulk(52L);
        then(seaStrategy).should(never()).deleteExt(any());
        then(airStrategy).should(never()).deleteExt(any());
        then(nonBlStrategy).should(never()).deleteExt(any());
    }

    @Test
    @DisplayName("deleteByIdAndJobDiv(NON_BL): nonBlStrategy.deleteExt → houseBlRepository.deleteByIdBulk 순서, 다른 Strategy 미호출")
    void deleteByIdAndJobDiv_nonBl_dispatchesToNonBlStrategyThenDeletesParent() {
        adapter.deleteByIdAndJobDiv(53L, JobDiv.NON_BL);

        then(nonBlStrategy).should().deleteExt(53L);
        then(houseBlRepository).should().deleteByIdBulk(53L);
        then(seaStrategy).should(never()).deleteExt(any());
        then(airStrategy).should(never()).deleteExt(any());
        then(truckStrategy).should(never()).deleteExt(any());
    }

    // ── findHouseBlsBySchedule — Sort 변환 검증 ────────────────────────

    @Test
    @DisplayName("findHouseBlsBySchedule: sortBy == null → Sort.unsorted() 기반 repository 호출")
    void findHouseBlsBySchedule_sortByNull_usesUnsortedSort() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<HouseBlJpaEntity> emptyPage = new PageImpl<>(List.of());
        given(houseBlRepository.findBySchedule(
                eq(JobDiv.AIR), eq(Bound.EXP), eq("20250101"), eq("20250131"),
                any(Pageable.class))).willReturn(emptyPage);

        adapter.findHouseBlsBySchedule(JobDiv.AIR, Bound.EXP, "20250101", "20250131", pageRequest);

        then(houseBlRepository).should().findBySchedule(
                eq(JobDiv.AIR), eq(Bound.EXP), eq("20250101"), eq("20250131"),
                argThat(pageable -> pageable.getSort().isUnsorted())
        );
    }

    @Test
    @DisplayName("findHouseBlsBySchedule: sortBy != null → 지정 Sort로 repository 호출")
    void findHouseBlsBySchedule_sortByNonNull_usesSort() {
        PageRequest pageRequest = PageRequest.of(0, 10, "etd", SortDirection.ASC);
        Page<HouseBlJpaEntity> emptyPage = new PageImpl<>(List.of());
        given(houseBlRepository.findBySchedule(
                eq(JobDiv.SEA), eq(Bound.IMP), eq("20250201"), eq("20250228"),
                any(Pageable.class))).willReturn(emptyPage);

        adapter.findHouseBlsBySchedule(JobDiv.SEA, Bound.IMP, "20250201", "20250228", pageRequest);

        then(houseBlRepository).should().findBySchedule(
                eq(JobDiv.SEA), eq(Bound.IMP), eq("20250201"), eq("20250228"),
                argThat(pageable ->
                        pageable.getSort().isSorted()
                                && pageable.getSort().getOrderFor("etd") != null)
        );
    }

    // ── 위임 메서드 ────────────────────────────────────────────────────

    @Test
    @DisplayName("countHouseBlsByMasterBlId: repository.countByMasterBlId에 위임하고 결과를 그대로 반환")
    void countHouseBlsByMasterBlId_delegatesToRepository() {
        given(houseBlRepository.countByMasterBlId(42L)).willReturn(3L);

        long result = adapter.countHouseBlsByMasterBlId(42L);

        assertThat(result).isEqualTo(3L);
        then(houseBlRepository).should().countByMasterBlId(42L);
    }

    @Test
    @DisplayName("findConsoledSeaSummariesByMasterBlId: repository 위임, 결과 가공 없이 반환")
    void findConsoledSeaSummariesByMasterBlId_delegatesToRepositoryAndReturnsAsIs() {
        Long masterBlId = 123L;
        ConsoledHouseBlSeaSummary summary = new ConsoledHouseBlSeaSummary(
                1L, "HBL-001", "SHIP01", "CONS01", "DOC01",
                10, "CTN", null, BigDecimal.valueOf(100), BigDecimal.valueOf(1),
                "20251130", "20251201", "VESSEL A", "V001", "KRPUS", "USNYC"
        );
        List<ConsoledHouseBlSeaSummary> expected = List.of(summary);
        given(houseBlRepository.findConsoledSeaSummariesByMasterBlId(masterBlId)).willReturn(expected);

        List<ConsoledHouseBlSeaSummary> result = adapter.findConsoledSeaSummariesByMasterBlId(masterBlId);

        assertThat(result).isSameAs(expected);
        then(houseBlRepository).should().findConsoledSeaSummariesByMasterBlId(masterBlId);
    }

    @Test
    @DisplayName("findConsoledAirSummariesByMasterBlId: repository 위임, 결과 가공 없이 반환")
    void findConsoledAirSummariesByMasterBlId_delegatesToRepositoryAndReturnsAsIs() {
        Long masterBlId = 123L;
        ConsoledHouseBlAirSummary summary = new ConsoledHouseBlAirSummary(
                1L, "HBL-002", "SHIP02", "CONS02", "DOC02",
                5, "PCS", null, BigDecimal.valueOf(50), BigDecimal.valueOf(2),
                BigDecimal.valueOf(55)
        );
        List<ConsoledHouseBlAirSummary> expected = List.of(summary);
        given(houseBlRepository.findConsoledAirSummariesByMasterBlId(masterBlId)).willReturn(expected);

        List<ConsoledHouseBlAirSummary> result = adapter.findConsoledAirSummariesByMasterBlId(masterBlId);

        assertThat(result).isSameAs(expected);
        then(houseBlRepository).should().findConsoledAirSummariesByMasterBlId(masterBlId);
    }

}
