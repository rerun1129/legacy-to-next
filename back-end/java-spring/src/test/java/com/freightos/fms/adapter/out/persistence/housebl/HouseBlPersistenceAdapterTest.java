package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.*;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    @Mock private HouseBlMapper houseBlMapper;

    @InjectMocks
    private HouseBlPersistenceAdapter adapter;

    // ── saveHouseBl(AIR) ──────────────────────────────────────────────

    @Test
    @DisplayName("saveHouseBl(AIR): syncDims→syncScheduleLegs→syncLicenses→replaceDesc 순서 후 airRepository.save 호출")
    void saveAirHouseBl_callsSyncInOrderThenSavesAirExt() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.AIR);
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlAirRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlMapper.toAirDomain(eq(savedJpa), any())).willReturn(air);

        adapter.saveHouseBl(air);

        InOrder order = inOrder(savedJpa, houseBlAirRepository);
        order.verify(savedJpa).syncDims(any());
        order.verify(savedJpa).syncScheduleLegs(any());
        order.verify(savedJpa).syncLicenses(any());
        order.verify(savedJpa).replaceDesc(any());
        order.verify(houseBlAirRepository).save(any());
    }

    @Test
    @DisplayName("saveHouseBl(AIR): seaRepository/truckRepository/nonBlRepository는 호출하지 않는다")
    void saveAirHouseBl_doesNotInvokeOtherExtRepos() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.AIR);
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlAirRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlMapper.toAirDomain(eq(savedJpa), any())).willReturn(air);

        adapter.saveHouseBl(air);

        then(houseBlSeaRepository).should(never()).save(any());
        then(houseBlTruckRepository).should(never()).save(any());
        then(houseBlNonBlRepository).should(never()).save(any());
    }

    // ── saveHouseBl(SEA) ──────────────────────────────────────────────

    @Test
    @DisplayName("saveHouseBl(SEA): syncContainers→syncLicenses→replaceDesc 순서 후 seaRepository.save 호출")
    void saveSeaHouseBl_syncsContainersLicensesDescThenSavesSeaExt() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.SEA);
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlSeaRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlMapper.toSeaDomain(eq(savedJpa), any())).willReturn(sea);

        adapter.saveHouseBl(sea);

        InOrder order = inOrder(savedJpa, houseBlSeaRepository);
        order.verify(savedJpa).syncContainers(any());
        order.verify(savedJpa).syncLicenses(any());
        order.verify(savedJpa).replaceDesc(any());
        order.verify(houseBlSeaRepository).save(any());
    }

    @Test
    @DisplayName("saveHouseBl(SEA): airRepository/truckRepository/nonBlRepository는 호출하지 않는다")
    void saveSeaHouseBl_doesNotInvokeOtherExtRepos() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.SEA);
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlSeaRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlMapper.toSeaDomain(eq(savedJpa), any())).willReturn(sea);

        adapter.saveHouseBl(sea);

        then(houseBlAirRepository).should(never()).save(any());
        then(houseBlTruckRepository).should(never()).save(any());
        then(houseBlNonBlRepository).should(never()).save(any());
    }

    // ── saveHouseBl(TRUCK) ────────────────────────────────────────────

    @Test
    @DisplayName("saveHouseBl(TRUCK): syncDims만 호출되고 syncContainers/syncScheduleLegs/syncLicenses/replaceDesc는 없다")
    void saveTruckHouseBl_syncsDimsOnly_skipsLegsLicensesDesc() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.TRUCK);
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlTruckRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlMapper.toTruckDomain(eq(savedJpa), any())).willReturn(truck);

        adapter.saveHouseBl(truck);

        then(savedJpa).should().syncDims(any());
        then(savedJpa).should(never()).syncContainers(any());
        then(savedJpa).should(never()).syncScheduleLegs(any());
        then(savedJpa).should(never()).syncLicenses(any());
        then(savedJpa).should(never()).replaceDesc(any());
        then(houseBlTruckRepository).should().save(any());
    }

    // ── saveHouseBl(NON_BL) ───────────────────────────────────────────

    @Test
    @DisplayName("saveHouseBl(NON_BL): syncContainers→syncDims→replaceDesc 순서 후 nonBlRepository.save 호출")
    void saveNonBlHouseBl_syncsContainersDimsDescThenSavesNonBlExt() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        HouseBlJpaEntity savedJpa = spy(new HouseBlJpaEntity());
        savedJpa.setJobDiv(JobDiv.NON_BL);
        given(houseBlRepository.save(any())).willReturn(savedJpa);
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlMapper.toNonBlDomain(eq(savedJpa), any())).willReturn(nonBl);

        adapter.saveHouseBl(nonBl);

        InOrder order = inOrder(savedJpa, houseBlNonBlRepository);
        order.verify(savedJpa).syncContainers(any());
        order.verify(savedJpa).syncDims(any());
        order.verify(savedJpa).replaceDesc(any());
        order.verify(houseBlNonBlRepository).save(any());
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
        given(houseBlMapper.toAirDomain(eq(jpa), any())).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(1L);

        assertThat(result).contains(expected);
        then(houseBlMapper).should().toAirDomain(eq(jpa), any());
        then(houseBlMapper).should(never()).toSeaDomain(any(), any());
        then(houseBlMapper).should(never()).toTruckDomain(any(), any());
        then(houseBlMapper).should(never()).toNonBlDomain(any(), any());
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
        given(houseBlMapper.toSeaDomain(eq(jpa), any())).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(2L);

        assertThat(result).contains(expected);
        then(houseBlMapper).should().toSeaDomain(eq(jpa), any());
        then(houseBlMapper).should(never()).toAirDomain(any(), any());
    }

    @Test
    @DisplayName("findHouseBlById(TRUCK): jobDiv=TRUCK → mapper.toTruckDomain 호출")
    void findById_truckJob_invokesToTruckDomain() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.TRUCK);
        jpa.setHouseBlId(3L);
        HouseBlTruck expected = HouseBlTruck.create(Bound.EXP);
        given(houseBlRepository.findById(3L)).willReturn(Optional.of(jpa));
        given(houseBlTruckRepository.findByHouseBlHouseBlId(3L)).willReturn(Optional.empty());
        given(houseBlMapper.toTruckDomain(eq(jpa), any())).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(3L);

        assertThat(result).contains(expected);
        then(houseBlMapper).should().toTruckDomain(eq(jpa), any());
        then(houseBlMapper).should(never()).toAirDomain(any(), any());
        then(houseBlMapper).should(never()).toSeaDomain(any(), any());
    }

    @Test
    @DisplayName("findHouseBlById(NON_BL): jobDiv=NON_BL → mapper.toNonBlDomain 호출")
    void findById_nonBlJob_invokesToNonBlDomain() {
        HouseBlJpaEntity jpa = new HouseBlJpaEntity();
        jpa.setJobDiv(JobDiv.NON_BL);
        jpa.setHouseBlId(4L);
        HouseBlNonBl expected = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        given(houseBlRepository.findById(4L)).willReturn(Optional.of(jpa));
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(4L)).willReturn(Optional.empty());
        given(houseBlMapper.toNonBlDomain(eq(jpa), any())).willReturn(expected);

        Optional<HouseBl> result = adapter.findHouseBlById(4L);

        assertThat(result).contains(expected);
        then(houseBlMapper).should().toNonBlDomain(eq(jpa), any());
        then(houseBlMapper).should(never()).toAirDomain(any(), any());
    }

    @Test
    @DisplayName("findHouseBlById: 존재하지 않는 ID → Optional.empty 반환, mapper 미호출")
    void findById_notFound_returnsEmpty() {
        given(houseBlRepository.findById(999L)).willReturn(Optional.empty());

        Optional<HouseBl> result = adapter.findHouseBlById(999L);

        assertThat(result).isEmpty();
        then(houseBlMapper).shouldHaveNoInteractions();
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
}
