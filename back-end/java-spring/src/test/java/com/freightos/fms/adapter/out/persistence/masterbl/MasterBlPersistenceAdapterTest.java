package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.SortDirection;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
class MasterBlPersistenceAdapterTest {

    @Mock private MasterBlRepository masterBlRepository;
    @Mock private MasterBlSeaRepository masterBlSeaRepository;
    @Mock private MasterBlAirRepository masterBlAirRepository;
    @Mock private MasterBlSeaDescRepository masterBlSeaDescRepository;
    @Mock private MasterBlAirDescRepository masterBlAirDescRepository;
    @Mock private MasterBlDimRepository masterBlDimRepository;
    @Mock private MasterBlScheduleLegRepository masterBlScheduleLegRepository;
    @Mock private MasterBlAirChargeRepository masterBlAirChargeRepository;
    @Mock private MasterBlMapper masterBlMapper;

    @InjectMocks
    private MasterBlPersistenceAdapter adapter;

    // ── createMasterBl(SEA) ──────────────────────────────────────────

    @Test
    @DisplayName("createMasterBl(SEA): seaRepository.save 호출, desc pre-SELECT 미호출")
    void createMasterBl_sea_savesSeaExtAndSkipsDescPreSelect() {
        MasterBlSea sea = MasterBlSea.create(Bound.EXP);
        MasterBlJpaEntity savedJpa = new MasterBlJpaEntity();
        savedJpa.setJobDiv(MasterBlJobDiv.SEA);
        MasterBlSeaJpaEntity savedSeaJpa = new MasterBlSeaJpaEntity();
        given(masterBlRepository.save(any())).willReturn(savedJpa);
        given(masterBlSeaRepository.save(any())).willReturn(savedSeaJpa);

        adapter.createMasterBl(sea);

        // Create 흐름(isNew=true) — desc SELECT 미발생 회귀 방지
        org.mockito.InOrder order = inOrder(masterBlSeaRepository);
        order.verify(masterBlSeaRepository).save(any());
        verify(masterBlSeaDescRepository, never()).findBySea_MasterBlSeaId(any());
    }

    // ── createMasterBl(AIR) ──────────────────────────────────────────

    @Test
    @DisplayName("createMasterBl(AIR): airRepository.save→syncDims→syncScheduleLegs→syncAirCharges 순서, desc pre-SELECT 미호출")
    void createMasterBl_air_callsSyncMethodsInOrderAndSkipsDescPreSelect() {
        MasterBlAir air = MasterBlAir.create(Bound.EXP);
        MasterBlJpaEntity savedJpa = spy(new MasterBlJpaEntity());
        savedJpa.setJobDiv(MasterBlJobDiv.AIR);
        MasterBlAirJpaEntity savedAirJpa = spy(new MasterBlAirJpaEntity());
        given(masterBlRepository.save(any())).willReturn(savedJpa);
        given(masterBlAirRepository.save(any())).willReturn(savedAirJpa);

        adapter.createMasterBl(air);

        // Create 흐름(isNew=true) — desc SELECT 미발생 회귀 방지
        org.mockito.InOrder order = inOrder(masterBlAirRepository, savedAirJpa);
        order.verify(masterBlAirRepository).save(any());
        order.verify(savedAirJpa).syncDims(any());
        order.verify(savedAirJpa).syncScheduleLegs(any());
        order.verify(savedAirJpa).syncAirCharges(any());
        verify(masterBlAirDescRepository, never()).findByAir_MasterBlAirId(any());
    }

    // ── updateMasterBl — 기존 ID 없을 때 예외 ─────────────────────────

    @Test
    @DisplayName("updateMasterBl: domain.getId() != null이고 repository가 empty → ResourceNotFoundException")
    void updateMasterBl_whenNotFound_throwsResourceNotFound() {
        MasterBlSea sea = MasterBlSea.create(Bound.EXP);
        sea.assignIdentity(99L, null, null, null, null);
        given(masterBlRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.updateMasterBl(sea))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── loadWithExt(jobDiv == null) → IAE ────────────────────────────

    @Test
    @DisplayName("findMasterBlById: jobDiv == null인 JPA 엔티티 → IllegalArgumentException")
    void loadWithExt_nullJobDiv_throwsIllegalArgumentException() {
        MasterBlJpaEntity jpa = new MasterBlJpaEntity();
        // jobDiv 미설정 → null
        given(masterBlRepository.findById(1L)).willReturn(Optional.of(jpa));

        assertThatThrownBy(() -> adapter.findMasterBlById(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jobDiv is null");
    }

    // ── getMasterBlsByBound — sortBy null ────────────────────────────

    @Test
    @DisplayName("getMasterBlsByBound: sortBy == null → Sort.unsorted() 기반으로 repository 호출")
    void getMasterBlsByBound_sortByNull_usesUnsortedSort() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        MasterBlJpaEntity jpa = new MasterBlJpaEntity();
        jpa.setJobDiv(MasterBlJobDiv.SEA);
        Page<MasterBlJpaEntity> page = new PageImpl<>(List.of(jpa));
        given(masterBlRepository.findAllByBound(eq(Bound.EXP), any(Pageable.class))).willReturn(page);
        given(masterBlSeaRepository.findByMasterBlMasterBlId(any())).willReturn(Optional.empty());
        given(masterBlMapper.toSeaDomain(eq(jpa), any(), any())).willReturn(MasterBlSea.create(Bound.EXP));

        adapter.getMasterBlsByBound(Bound.EXP, pageRequest);

        then(masterBlRepository).should().findAllByBound(eq(Bound.EXP), argThat(pageable ->
                pageable.getSort().isUnsorted()
        ));
    }

    // ── getMasterBlsByBound — sortBy 있을 때 ─────────────────────────

    @Test
    @DisplayName("getMasterBlsByBound: sortBy != null → 지정된 Sort로 repository 호출")
    void getMasterBlsByBound_sortByNonNull_usesSpecifiedSort() {
        PageRequest pageRequest = PageRequest.of(0, 10, "mblNo", SortDirection.ASC);
        Page<MasterBlJpaEntity> page = new PageImpl<>(List.of());
        given(masterBlRepository.findAllByBound(eq(Bound.EXP), any(Pageable.class))).willReturn(page);

        adapter.getMasterBlsByBound(Bound.EXP, pageRequest);

        then(masterBlRepository).should().findAllByBound(eq(Bound.EXP), argThat(pageable ->
                pageable.getSort().isSorted()
                        && pageable.getSort().getOrderFor("mblNo") != null
        ));
    }

    // ── findMasterBlByMblNo 위임 ──────────────────────────────────────

    @Test
    @DisplayName("findMasterBlByMblNo: repository.findByMblNo에 위임하고 결과를 그대로 반환")
    void findMasterBlByMblNo_delegatesToRepository() {
        MasterBlJpaEntity jpa = new MasterBlJpaEntity();
        jpa.setJobDiv(MasterBlJobDiv.SEA);
        MasterBlSea expected = MasterBlSea.create(Bound.EXP);
        given(masterBlRepository.findByMblNo("MBL-001")).willReturn(Optional.of(jpa));
        given(masterBlSeaRepository.findByMasterBlMasterBlId(any())).willReturn(Optional.empty());
        given(masterBlMapper.toSeaDomain(eq(jpa), any(), any())).willReturn(expected);

        Optional<MasterBl> result = adapter.findMasterBlByMblNo("MBL-001");

        assertThat(result).contains(expected);
        then(masterBlRepository).should().findByMblNo("MBL-001");
    }

    // ── existsByMblNo 위임 ────────────────────────────────────────────

    @Test
    @DisplayName("existsByMblNo: repository.existsByMblNo에 위임하고 결과를 그대로 반환")
    void existsByMblNo_delegatesToRepository() {
        given(masterBlRepository.existsByMblNo("MBL-001")).willReturn(true);

        boolean result = adapter.existsByMblNo("MBL-001");

        assertThat(result).isTrue();
        then(masterBlRepository).should().existsByMblNo("MBL-001");
    }

    // ── deleteByIdAndJobDiv 위임 ──────────────────────────────────────

    @Test
    @DisplayName("deleteByIdAndJobDiv(SEA): seaDesc→seaExt→부모 순서 명시 DELETE, airRepository 미호출")
    void deleteByIdAndJobDiv_sea_deletesDescExtParent() {
        adapter.deleteByIdAndJobDiv(10L, MasterBlJobDiv.SEA);

        org.mockito.InOrder order = inOrder(masterBlSeaDescRepository, masterBlSeaRepository, masterBlRepository);
        order.verify(masterBlSeaDescRepository).deleteByParentMasterBlId(10L);
        order.verify(masterBlSeaRepository).deleteByMasterBl_MasterBlId(10L);
        order.verify(masterBlRepository).deleteByIdBulk(10L);
        then(masterBlAirRepository).shouldHaveNoInteractions();
        then(masterBlDimRepository).shouldHaveNoInteractions();
        then(masterBlScheduleLegRepository).shouldHaveNoInteractions();
        then(masterBlAirChargeRepository).shouldHaveNoInteractions();
        then(masterBlAirDescRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("deleteByIdAndJobDiv(AIR): dim→scheduleLeg→airCharge→airDesc→airExt→부모 순서 명시 DELETE, seaRepository 미호출")
    void deleteByIdAndJobDiv_air_deletesChildrenExtParent() {
        adapter.deleteByIdAndJobDiv(11L, MasterBlJobDiv.AIR);

        org.mockito.InOrder order = inOrder(masterBlDimRepository, masterBlScheduleLegRepository,
                masterBlAirChargeRepository, masterBlAirDescRepository, masterBlAirRepository, masterBlRepository);
        order.verify(masterBlDimRepository).deleteByParentMasterBlId(11L);
        order.verify(masterBlScheduleLegRepository).deleteByParentMasterBlId(11L);
        order.verify(masterBlAirChargeRepository).deleteByParentMasterBlId(11L);
        order.verify(masterBlAirDescRepository).deleteByParentMasterBlId(11L);
        order.verify(masterBlAirRepository).deleteByMasterBl_MasterBlId(11L);
        order.verify(masterBlRepository).deleteByIdBulk(11L);
        then(masterBlSeaRepository).shouldHaveNoInteractions();
        then(masterBlSeaDescRepository).shouldHaveNoInteractions();
    }

    // ── searchMasterBls ───────────────────────────────────────

    @Test
    @DisplayName("searchMasterBls: repository.searchByFilter 결과를 그대로 반환 (projection 변환 없음)")
    void searchMasterBls_delegatesToCustomRepositoryAndMapsResult() {
        MasterBlFilter filter = new MasterBlFilter(Bound.EXP, "MBL-001", null, null, null, null, null, null);
        PageRequest pageRequest = PageRequest.of(0, 10);

        MasterBlSummaryResult summary = new MasterBlSummaryResult(1L, "MBL-001", null, null, "EXP", null, null, null, null, null, null, null, null);
        PagedResult<MasterBlSummaryResult> repoResult = PagedResult.of(List.of(summary), 1L, 1, 0, 10);

        given(masterBlRepository.searchByFilter(filter, pageRequest)).willReturn(repoResult);

        PagedResult<MasterBlSummaryResult> result = adapter.searchMasterBls(filter, pageRequest);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(summary);
        assertThat(result.getTotalElements()).isEqualTo(1L);
        then(masterBlRepository).should().searchByFilter(filter, pageRequest);
    }

    // ── loadWithExt SEA desc 로드 ────────────────────────────────────

    @Test
    @DisplayName("loadWithExt(SEA): seaJpa가 있으면 seaDescRepository에서 desc 조회")
    void loadWithExt_sea_loadsDescFromSeaDescRepository() {
        MasterBlJpaEntity jpa = new MasterBlJpaEntity();
        jpa.setJobDiv(MasterBlJobDiv.SEA);
        MasterBlSeaJpaEntity seaJpa = new MasterBlSeaJpaEntity();
        MasterBlSeaDescJpaEntity seaDescJpa = new MasterBlSeaDescJpaEntity();
        MasterBlSea expected = MasterBlSea.create(Bound.EXP);

        given(masterBlRepository.findById(1L)).willReturn(Optional.of(jpa));
        given(masterBlSeaRepository.findByMasterBlMasterBlId(any())).willReturn(Optional.of(seaJpa));
        given(masterBlSeaDescRepository.findBySea_MasterBlSeaId(any())).willReturn(Optional.of(seaDescJpa));
        given(masterBlMapper.toSeaDomain(eq(jpa), eq(seaJpa), eq(seaDescJpa))).willReturn(expected);

        Optional<MasterBl> result = adapter.findMasterBlById(1L);

        assertThat(result).contains(expected);
        then(masterBlSeaDescRepository).should().findBySea_MasterBlSeaId(any());
    }

    // ── loadWithExt AIR desc 로드 ────────────────────────────────────

    @Test
    @DisplayName("loadWithExt(AIR): airJpa가 있으면 airDescRepository에서 desc 조회")
    void loadWithExt_air_loadsDescFromAirDescRepository() {
        MasterBlJpaEntity jpa = new MasterBlJpaEntity();
        jpa.setJobDiv(MasterBlJobDiv.AIR);
        MasterBlAirJpaEntity airJpa = new MasterBlAirJpaEntity();
        MasterBlAirDescJpaEntity airDescJpa = new MasterBlAirDescJpaEntity();
        MasterBlAir expected = MasterBlAir.create(Bound.EXP);

        given(masterBlRepository.findById(1L)).willReturn(Optional.of(jpa));
        given(masterBlAirRepository.findByMasterBlMasterBlId(any())).willReturn(Optional.of(airJpa));
        given(masterBlAirDescRepository.findByAir_MasterBlAirId(any())).willReturn(Optional.of(airDescJpa));
        given(masterBlMapper.toAirDomain(eq(jpa), eq(airJpa), any(), eq(airDescJpa))).willReturn(expected);

        Optional<MasterBl> result = adapter.findMasterBlById(1L);

        assertThat(result).contains(expected);
        then(masterBlAirDescRepository).should().findByAir_MasterBlAirId(any());
    }
}
