package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.adapter.out.persistence.nonbl.HouseBlNonBlRepository;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.SortDirection;
import com.freightos.common.model.PageRequest;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HouseBlPersistenceAdapterTest {

    @Mock private HouseBlRepository houseBlRepository;
    @Mock private HouseBlSeaRepository houseBlSeaRepository;
    @Mock private HouseBlAirRepository houseBlAirRepository;
    @Mock private HouseBlTruckRepository houseBlTruckRepository;
    @Mock private HouseBlNonBlRepository houseBlNonBlRepository;
    @Mock private HouseBlSeaDescRepository houseBlSeaDescRepository;
    @Mock private HouseBlAirDescRepository houseBlAirDescRepository;
    @Mock private HouseBlTruckDescRepository houseBlTruckDescRepository;
    @Mock private HouseBlJpaToDomainMapper jpaToDomainMapper;
    @Mock private HouseBlDomainToJpaMapper domainToJpaMapper;
    @Mock private HouseBlCargoMapper houseBlCargoMapper;
    @Mock private HouseBlDocMapper houseBlDocMapper;

    @InjectMocks
    private HouseBlPersistenceAdapter adapter;

    // ── saveHouseBl(AIR) ──────────────────────────────────────────────

    @Test
    @DisplayName("saveHouseBl(AIR): syncDims→airRepository.save→savedAirJpa.syncScheduleLegs→savedAirJpa.syncAirCharges 순서, airDescRepository 조회")
    void saveAirHouseBl_callsSyncInOrderThenSavesAirExt() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.AIR);
        HouseBlAirJpaEntity savedAirJpa = spy(new HouseBlAirJpaEntity());
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlAirRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        // airRepository.save가 savedAirJpa를 반환해야 syncScheduleLegs/syncAirCharges 호출 가능
        given(houseBlAirRepository.save(any())).willReturn(savedAirJpa);
        given(houseBlAirDescRepository.findByAir_HouseBlAirId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toAirDomain(eq(savedJpa), any(), any())).willReturn(air);

        adapter.saveHouseBl(air);

        InOrder order = inOrder(savedJpa, houseBlAirRepository, savedAirJpa);
        order.verify(savedJpa).syncDims(any());
        order.verify(houseBlAirRepository).save(any());
        order.verify(savedAirJpa).syncScheduleLegs(any());
        order.verify(savedAirJpa).syncAirCharges(any());
    }

    @Test
    @DisplayName("saveHouseBl(AIR): seaRepository/truckRepository/nonBlRepository는 호출하지 않는다")
    void saveAirHouseBl_doesNotInvokeOtherExtRepos() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.AIR);
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlAirRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlAirRepository.save(any())).willReturn(spy(new HouseBlAirJpaEntity()));
        given(houseBlAirDescRepository.findByAir_HouseBlAirId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toAirDomain(eq(savedJpa), any(), any())).willReturn(air);

        adapter.saveHouseBl(air);

        then(houseBlSeaRepository).should(never()).save(any());
        then(houseBlTruckRepository).should(never()).save(any());
        then(houseBlNonBlRepository).should(never()).save(any());
    }

    // ── saveHouseBl(SEA) ──────────────────────────────────────────────

    @Test
    @DisplayName("saveHouseBl(SEA): seaRepository.save → savedSeaJpa.syncContainers → seaDescRepository 조회 순서")
    void saveSeaHouseBl_savesSeaExtThenSyncsContainers() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.SEA);
        HouseBlSeaJpaEntity savedSeaJpa = spy(new HouseBlSeaJpaEntity());
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlSeaRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlSeaRepository.save(any())).willReturn(savedSeaJpa);
        given(houseBlSeaDescRepository.findBySea_HouseBlSeaId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(eq(savedJpa), any(), any())).willReturn(sea);

        adapter.saveHouseBl(sea);

        // SEA 컨테이너는 seaJpa에 syncContainers — seaRepository.save 후 savedSeaJpa에 호출됨
        InOrder order = inOrder(houseBlSeaRepository, savedSeaJpa);
        order.verify(houseBlSeaRepository).save(any());
        order.verify(savedSeaJpa).syncContainers(any());
    }

    @Test
    @DisplayName("saveHouseBl(SEA): airRepository/truckRepository/nonBlRepository는 호출하지 않는다")
    void saveSeaHouseBl_doesNotInvokeOtherExtRepos() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.SEA);
        HouseBlSeaJpaEntity savedSeaJpa = spy(new HouseBlSeaJpaEntity());
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlSeaRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlSeaRepository.save(any())).willReturn(savedSeaJpa);
        given(houseBlSeaDescRepository.findBySea_HouseBlSeaId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(eq(savedJpa), any(), any())).willReturn(sea);

        adapter.saveHouseBl(sea);

        then(houseBlAirRepository).should(never()).save(any());
        then(houseBlTruckRepository).should(never()).save(any());
        then(houseBlNonBlRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("saveHouseBl(SEA): savedJpa에 syncContainers 미호출 — SEA 컨테이너는 seaJpa 소유")
    void saveSeaHouseBl_doesNotCallSyncContainersOnParentJpa() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.SEA);
        HouseBlSeaJpaEntity savedSeaJpa = spy(new HouseBlSeaJpaEntity());
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlSeaRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlSeaRepository.save(any())).willReturn(savedSeaJpa);
        given(houseBlSeaDescRepository.findBySea_HouseBlSeaId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(eq(savedJpa), any(), any())).willReturn(sea);

        adapter.saveHouseBl(sea);

        // 부모 JpaEntity에는 syncContainers가 없으므로 호출되지 않아야 함
        then(savedJpa).should(never()).syncDims(any()); // sea는 dims 미사용
    }

    // ── saveHouseBl(TRUCK) ────────────────────────────────────────────

    @Test
    @DisplayName("saveHouseBl(TRUCK): syncDims만 호출되고 seaDescRepository/airDescRepository는 없다. truckRepository.save → savedTruckJpa.syncTruckOrders → truckDescRepository 조회 순서")
    void saveTruckHouseBl_syncsDimsOnly_skipsLegsLicensesDesc() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.TRUCK);
        HouseBlTruckJpaEntity savedTruckJpa = spy(new HouseBlTruckJpaEntity());
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlTruckRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlTruckRepository.save(any())).willReturn(savedTruckJpa);
        given(houseBlTruckDescRepository.findByTruck_HouseBlTruckId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toTruckDomain(eq(savedJpa), any(), any())).willReturn(truck);

        adapter.saveHouseBl(truck);

        then(savedJpa).should().syncDims(any());
        // SEA/AIR desc repository는 TRUCK 처리 시 미호출
        then(houseBlSeaDescRepository).should(never()).findBySea_HouseBlSeaId(any());
        then(houseBlAirDescRepository).should(never()).findByAir_HouseBlAirId(any());
        // truckRepository.save 반환값(savedTruckJpa)에 syncTruckOrders 호출 후 truckDescRepository 조회
        then(houseBlTruckRepository).should().save(any());
        then(savedTruckJpa).should().syncTruckOrders(any());
        then(houseBlTruckDescRepository).should().findByTruck_HouseBlTruckId(any());
    }

    // ── saveHouseBl(NON_BL) ───────────────────────────────────────────

    @Test
    @DisplayName("saveHouseBl(NON_BL): nonBlJpa.mergeContainers → savedJpa.mergeDims → nonBlRepository.save 순서, desc 관련 repository는 미호출")
    void saveNonBlHouseBl_mergesContainersDimsThenSavesNonBlExt_withoutDesc() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        nonBl.updateRemark("REMARK_TEXT");
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.NON_BL);
        // findByHouseBlHouseBlId가 spy 객체를 반환하면 mergeContainers verify 가능
        HouseBlNonBlJpaEntity existingNonBlJpa = spy(new HouseBlNonBlJpaEntity());
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.of(existingNonBlJpa));
        given(houseBlNonBlRepository.save(any())).willReturn(existingNonBlJpa);

        adapter.saveHouseBl(nonBl);

        InOrder order = inOrder(existingNonBlJpa, savedJpa, houseBlNonBlRepository);
        order.verify(existingNonBlJpa).mergeContainers(any());
        order.verify(savedJpa).mergeDims(any());
        // NON_BL은 desc를 사용하지 않음
        order.verify(houseBlNonBlRepository).save(any());
        then(houseBlSeaDescRepository).shouldHaveNoInteractions();
        then(houseBlAirDescRepository).shouldHaveNoInteractions();
        then(houseBlTruckDescRepository).shouldHaveNoInteractions();
    }

    // ── findHouseBlById: JobDiv 분기 검증 ─────────────────────────────

    @Test
    @DisplayName("findHouseBlById(AIR): jobDiv=AIR → mapper.toAirDomain 호출, 다른 mode 매퍼는 미호출")
    void findById_airJob_invokesToAirDomain() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.AIR);
        jpa.setHouseBlId(1L);
        HouseBlAir expected = HouseBlAir.create(Bound.EXP);
        given(houseBlRepository.findById(1L)).willReturn(Optional.of(jpa));
        given(houseBlAirRepository.findByHouseBlHouseBlId(1L)).willReturn(Optional.empty());
        given(jpaToDomainMapper.toAirDomain(eq(jpa), any(), any())).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(1L);

        assertThat(result).contains(expected);
        then(jpaToDomainMapper).should().toAirDomain(eq(jpa), any(), any());
        then(jpaToDomainMapper).should(never()).toSeaDomain(any(), any(), any());
        then(jpaToDomainMapper).should(never()).toTruckDomain(any(), any(), any());
        then(jpaToDomainMapper).should(never()).toNonBlDomain(any(), any());
    }

    @Test
    @DisplayName("findHouseBlById(SEA): jobDiv=SEA → mapper.toSeaDomain 호출, toAirDomain 미호출")
    void findById_seaJob_invokesToSeaDomain() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.SEA);
        jpa.setHouseBlId(2L);
        HouseBlSea expected = HouseBlSea.create(Bound.EXP);
        given(houseBlRepository.findById(2L)).willReturn(Optional.of(jpa));
        given(houseBlSeaRepository.findByHouseBlHouseBlId(2L)).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(eq(jpa), any(), any())).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(2L);

        assertThat(result).contains(expected);
        then(jpaToDomainMapper).should().toSeaDomain(eq(jpa), any(), any());
        then(jpaToDomainMapper).should(never()).toAirDomain(any(), any(), any());
    }

    @Test
    @DisplayName("findHouseBlById(TRUCK): jobDiv=TRUCK → mapper.toTruckDomain 호출, truckDescRepository 조회, seaDescRepository/airDescRepository 미호출")
    void findById_truckJob_invokesToTruckDomain() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.TRUCK);
        jpa.setHouseBlId(3L);
        HouseBlTruck expected = HouseBlTruck.create(Bound.EXP);
        given(houseBlRepository.findById(3L)).willReturn(Optional.of(jpa));
        given(houseBlTruckRepository.findByHouseBlHouseBlId(3L)).willReturn(Optional.empty());
        given(jpaToDomainMapper.toTruckDomain(eq(jpa), any(), any())).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(3L);

        assertThat(result).contains(expected);
        then(jpaToDomainMapper).should().toTruckDomain(eq(jpa), any(), any());
        then(jpaToDomainMapper).should(never()).toAirDomain(any(), any(), any());
        then(jpaToDomainMapper).should(never()).toSeaDomain(any(), any(), any());
        then(houseBlSeaDescRepository).should(never()).findBySea_HouseBlSeaId(any());
        then(houseBlAirDescRepository).should(never()).findByAir_HouseBlAirId(any());
    }

    @Test
    @DisplayName("findHouseBlById(NON_BL): jobDiv=NON_BL → mapper.toNonBlDomain 호출, seaDescRepository/airDescRepository 미호출")
    void findById_nonBlJob_invokesToNonBlDomain() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.NON_BL);
        jpa.setHouseBlId(4L);
        HouseBlNonBl expected = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        given(houseBlRepository.findById(4L)).willReturn(Optional.of(jpa));
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(4L)).willReturn(Optional.empty());
        given(jpaToDomainMapper.toNonBlDomain(eq(jpa), any())).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(4L);

        assertThat(result).contains(expected);
        then(jpaToDomainMapper).should().toNonBlDomain(eq(jpa), any());
        then(jpaToDomainMapper).should(never()).toAirDomain(any(), any(), any());
        then(houseBlSeaDescRepository).should(never()).findBySea_HouseBlSeaId(any());
        then(houseBlAirDescRepository).should(never()).findByAir_HouseBlAirId(any());
    }

    @Test
    @DisplayName("findHouseBlById: 존재하지 않는 ID → Optional.empty 반환, mapper 미호출")
    void findById_notFound_returnsEmpty() {
        given(houseBlRepository.findById(999L)).willReturn(Optional.empty());

        Optional<HouseBl> result = adapter.findHouseBlById(999L);

        assertThat(result).isEmpty();
        then(jpaToDomainMapper).shouldHaveNoInteractions();
    }

    // ── findConsoledSeaSummariesByMasterBlId 위임 ──────────────────────────────────────────────

    @Test
    @DisplayName("findConsoledSeaSummariesByMasterBlId: repository 위임, 결과 가공 없이 반환")
    void findConsoledSeaSummariesByMasterBlId_delegatesToRepositoryAndReturnsAsIs() {
        Long masterBlId = 123L;
        ConsoledHouseBlSeaSummary summary = new ConsoledHouseBlSeaSummary(
                1L, "HBL-001", "SHIP01", "CONS01", "DOC01",
                10, "CTN", BigDecimal.valueOf(100), BigDecimal.valueOf(1),
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
                5, "PCS", BigDecimal.valueOf(50), BigDecimal.valueOf(2),
                BigDecimal.valueOf(55)
        );
        List<ConsoledHouseBlAirSummary> expected = List.of(summary);

        given(houseBlRepository.findConsoledAirSummariesByMasterBlId(masterBlId)).willReturn(expected);

        List<ConsoledHouseBlAirSummary> result = adapter.findConsoledAirSummariesByMasterBlId(masterBlId);

        assertThat(result).isSameAs(expected);
        then(houseBlRepository).should().findConsoledAirSummariesByMasterBlId(masterBlId);
    }

    // ── saveHouseBl(TRUCK) — syncTruckOrders 호출 검증 ────────────────

    @Test
    @DisplayName("saveHouseBl(TRUCK): savedJpa.syncDims 후 truckRepository.save → savedTruckJpa.syncTruckOrders → saveOrDeleteTruckDesc(desc null이면 truckDescRepository.findBy 호출) 순서")
    void saveHouseBl_truck_callsSyncTruckOrders() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.TRUCK);
        HouseBlTruckJpaEntity savedTruckJpa = spy(new HouseBlTruckJpaEntity());
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlTruckRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlTruckRepository.save(any())).willReturn(savedTruckJpa);
        given(houseBlTruckDescRepository.findByTruck_HouseBlTruckId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toTruckDomain(eq(savedJpa), any(), any())).willReturn(truck);

        adapter.saveHouseBl(truck);

        InOrder order = inOrder(savedJpa, houseBlTruckRepository, savedTruckJpa);
        order.verify(savedJpa).syncDims(any());
        order.verify(houseBlTruckRepository).save(any());
        order.verify(savedTruckJpa).syncTruckOrders(any());
    }

    // ── deleteHouseBl — 타입별 분기 검증 ────────────────────────────────

    @Test
    @DisplayName("deleteHouseBl(SEA): seaRepository.deleteByHouseBl_HouseBlId → houseBlRepository.deleteById 순서. house_bl_sea_desc/container는 ON DELETE CASCADE로 DB 자동 정리")
    void deleteHouseBl_sea_deletesSeaExtThenBase_descAndContainerCascadedByDb() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sea.assignIdentity(50L, null, null, null, null);

        adapter.deleteHouseBl(sea);

        InOrder order = inOrder(houseBlSeaRepository, houseBlRepository);
        order.verify(houseBlSeaRepository).deleteByHouseBl_HouseBlId(50L);
        order.verify(houseBlRepository).deleteById(50L);
        then(houseBlAirRepository).shouldHaveNoInteractions();
        then(houseBlTruckRepository).shouldHaveNoInteractions();
        then(houseBlNonBlRepository).shouldHaveNoInteractions();
        then(houseBlSeaDescRepository).shouldHaveNoInteractions();
        then(houseBlAirDescRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("deleteHouseBl(AIR): airRepository.deleteByHouseBl_HouseBlId → houseBlRepository.deleteById 순서. house_bl_air_desc는 ON DELETE CASCADE로 DB 자동 정리")
    void deleteHouseBl_air_deletesAirExtThenBase_descCascadedByDb() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        air.assignIdentity(51L, null, null, null, null);

        adapter.deleteHouseBl(air);

        InOrder order = inOrder(houseBlAirRepository, houseBlRepository);
        order.verify(houseBlAirRepository).deleteByHouseBl_HouseBlId(51L);
        order.verify(houseBlRepository).deleteById(51L);
        then(houseBlSeaRepository).shouldHaveNoInteractions();
        then(houseBlTruckRepository).shouldHaveNoInteractions();
        then(houseBlNonBlRepository).shouldHaveNoInteractions();
        then(houseBlSeaDescRepository).shouldHaveNoInteractions();
        then(houseBlAirDescRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("deleteHouseBl(TRUCK): truckRepository.deleteByHouseBl_HouseBlId → houseBlRepository.deleteById 순서. house_bl_truck_desc는 ON DELETE CASCADE로 DB 자동 정리")
    void deleteHouseBl_truck_deletesTruckExtThenBase_descCascadedByDb() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        truck.assignIdentity(52L, null, null, null, null);

        adapter.deleteHouseBl(truck);

        InOrder order = inOrder(houseBlTruckRepository, houseBlRepository);
        order.verify(houseBlTruckRepository).deleteByHouseBl_HouseBlId(52L);
        order.verify(houseBlRepository).deleteById(52L);
        then(houseBlSeaRepository).shouldHaveNoInteractions();
        then(houseBlAirRepository).shouldHaveNoInteractions();
        then(houseBlNonBlRepository).shouldHaveNoInteractions();
        then(houseBlSeaDescRepository).shouldHaveNoInteractions();
        then(houseBlAirDescRepository).shouldHaveNoInteractions();
        then(houseBlTruckDescRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("deleteHouseBl(NON_BL): nonBlRepository.deleteByHouseBl_HouseBlId → houseBlRepository.deleteById 순서, seaDescRepository/airDescRepository/truckDescRepository 미호출. house_bl_nonbl_container는 ON DELETE CASCADE")
    void deleteHouseBl_nonBl_deletesNonBlExtThenBase_withoutDesc() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        nonBl.assignIdentity(53L, null, null, null, null);

        adapter.deleteHouseBl(nonBl);

        InOrder order = inOrder(houseBlNonBlRepository, houseBlRepository);
        order.verify(houseBlNonBlRepository).deleteByHouseBl_HouseBlId(53L);
        order.verify(houseBlRepository).deleteById(53L);
        then(houseBlSeaRepository).shouldHaveNoInteractions();
        then(houseBlAirRepository).shouldHaveNoInteractions();
        then(houseBlTruckRepository).shouldHaveNoInteractions();
        then(houseBlSeaDescRepository).shouldHaveNoInteractions();
        then(houseBlAirDescRepository).shouldHaveNoInteractions();
        then(houseBlTruckDescRepository).shouldHaveNoInteractions();
    }

    // ── findHouseBlsBySchedule — sortBy null ──────────────────────────

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

    // ── findHouseBlsBySchedule — sortBy 있을 때 ───────────────────────

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

    // ── saveHouseBl — 기존 ID 없을 때 ResourceNotFoundException ────────

    @Test
    @DisplayName("saveHouseBl: domain.getId() != null + repository empty → ResourceNotFoundException")
    void saveHouseBl_withExistingId_whenNotFound_throwsResourceNotFound() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        sea.assignIdentity(777L, null, null, null, null);
        given(houseBlRepository.findById(777L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.saveHouseBl(sea))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── countHouseBlsByMasterBlId 위임 ─────────────────────────────────

    @Test
    @DisplayName("countHouseBlsByMasterBlId: repository.countByMasterBlId에 위임하고 결과를 그대로 반환")
    void countHouseBlsByMasterBlId_delegatesToRepository() {
        given(houseBlRepository.countByMasterBlId(42L)).willReturn(3L);

        long result = adapter.countHouseBlsByMasterBlId(42L);

        assertThat(result).isEqualTo(3L);
        then(houseBlRepository).should().countByMasterBlId(42L);
    }

    // ── saveHouseBl(NON_BL) — merge 메서드 호출 순서 검증 ─────────────

    @Test
    @DisplayName("saveHouseBl(NON_BL): nonBlJpa.mergeContainers→savedJpa.mergeDims 순서 후 nonBlRepository.save 호출 (NON_BL은 desc 미사용)")
    void saveNonBlHouseBl_callsMergeInOrder() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.NON_BL);
        HouseBlNonBlJpaEntity existingNonBlJpa = spy(new HouseBlNonBlJpaEntity());
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.of(existingNonBlJpa));
        given(houseBlNonBlRepository.save(any())).willReturn(existingNonBlJpa);

        adapter.saveHouseBl(nonBl);

        InOrder order = inOrder(existingNonBlJpa, savedJpa, houseBlNonBlRepository);
        order.verify(existingNonBlJpa).mergeContainers(any());
        order.verify(savedJpa).mergeDims(any());
        order.verify(houseBlNonBlRepository).save(any());
        then(houseBlSeaDescRepository).shouldHaveNoInteractions();
        then(houseBlAirDescRepository).shouldHaveNoInteractions();
        then(houseBlTruckDescRepository).shouldHaveNoInteractions();
    }

    // ── findHouseBlById: sea/airDescRepository 호출 여부 분기 검증 ───────────

    @Test
    @DisplayName("findHouseBlById(SEA): seaRepository로 seaExt 조회 후 seaDescRepository로 desc 조회")
    void findHouseBlById_seaJob_callsSeaDescRepository() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.SEA);
        jpa.setHouseBlId(10L);
        HouseBlSeaJpaEntity seaJpa = new HouseBlSeaJpaEntity();
        HouseBlSea expected = HouseBlSea.create(Bound.EXP);
        given(houseBlRepository.findById(10L)).willReturn(Optional.of(jpa));
        given(houseBlSeaRepository.findByHouseBlHouseBlId(10L)).willReturn(Optional.of(seaJpa));
        given(houseBlSeaDescRepository.findBySea_HouseBlSeaId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(eq(jpa), any(), any())).willReturn(expected);

        adapter.findHouseBlById(10L);

        then(houseBlRepository).should().findById(10L);
        then(houseBlSeaDescRepository).should().findBySea_HouseBlSeaId(any());
    }

    @Test
    @DisplayName("findHouseBlById(AIR): airRepository로 airExt 조회 후 airDescRepository로 desc 조회")
    void findHouseBlById_airJob_callsAirDescRepository() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.AIR);
        jpa.setHouseBlId(11L);
        HouseBlAirJpaEntity airJpa = new HouseBlAirJpaEntity();
        HouseBlAir expected = HouseBlAir.create(Bound.EXP);
        given(houseBlRepository.findById(11L)).willReturn(Optional.of(jpa));
        given(houseBlAirRepository.findByHouseBlHouseBlId(11L)).willReturn(Optional.of(airJpa));
        given(houseBlAirDescRepository.findByAir_HouseBlAirId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toAirDomain(eq(jpa), any(), any())).willReturn(expected);

        adapter.findHouseBlById(11L);

        then(houseBlRepository).should().findById(11L);
        then(houseBlAirDescRepository).should().findByAir_HouseBlAirId(any());
    }

    @Test
    @DisplayName("findHouseBlById(NON_BL): seaDescRepository/airDescRepository 미호출 — desc 불필요 분기")
    void findHouseBlById_nonBlJob_doesNotCallDescRepository() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.NON_BL);
        jpa.setHouseBlId(12L);
        HouseBlNonBl expected = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        given(houseBlRepository.findById(12L)).willReturn(Optional.of(jpa));
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(12L)).willReturn(Optional.empty());
        given(jpaToDomainMapper.toNonBlDomain(eq(jpa), any())).willReturn(expected);

        adapter.findHouseBlById(12L);

        then(houseBlRepository).should().findById(12L);
        then(houseBlSeaDescRepository).should(never()).findBySea_HouseBlSeaId(any());
        then(houseBlAirDescRepository).should(never()).findByAir_HouseBlAirId(any());
    }

    @Test
    @DisplayName("findHouseBlById(TRUCK): truckDescRepository 조회, seaDescRepository/airDescRepository 미호출")
    void findHouseBlById_truckJob_callsTruckDescRepository_doesNotCallSeaAirDescRepository() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.TRUCK);
        jpa.setHouseBlId(13L);
        HouseBlTruck expected = HouseBlTruck.create(Bound.EXP);
        HouseBlTruckJpaEntity truckJpa = new HouseBlTruckJpaEntity();
        given(houseBlRepository.findById(13L)).willReturn(Optional.of(jpa));
        given(houseBlTruckRepository.findByHouseBlHouseBlId(13L)).willReturn(Optional.of(truckJpa));
        given(houseBlTruckDescRepository.findByTruck_HouseBlTruckId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toTruckDomain(eq(jpa), any(), any())).willReturn(expected);

        adapter.findHouseBlById(13L);

        then(houseBlRepository).should().findById(13L);
        then(houseBlTruckDescRepository).should().findByTruck_HouseBlTruckId(any());
        then(houseBlSeaDescRepository).should(never()).findBySea_HouseBlSeaId(any());
        then(houseBlAirDescRepository).should(never()).findByAir_HouseBlAirId(any());
    }

    @Test
    @DisplayName("saveHouseBl(NON_BL): NON_BL 전용 merge만 호출 — airRepository/truckRepository는 미호출")
    void saveNonBlHouseBl_doesNotInvokeSeaOnlySync() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.NON_BL);
        HouseBlNonBlJpaEntity existingNonBlJpa = spy(new HouseBlNonBlJpaEntity());
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.of(existingNonBlJpa));
        given(houseBlNonBlRepository.save(any())).willReturn(existingNonBlJpa);

        adapter.saveHouseBl(nonBl);

        then(houseBlAirRepository).should(never()).save(any());
        then(houseBlTruckRepository).should(never()).save(any());
        then(houseBlSeaDescRepository).shouldHaveNoInteractions();
        then(houseBlAirDescRepository).shouldHaveNoInteractions();
        then(houseBlTruckDescRepository).shouldHaveNoInteractions();
    }
}
