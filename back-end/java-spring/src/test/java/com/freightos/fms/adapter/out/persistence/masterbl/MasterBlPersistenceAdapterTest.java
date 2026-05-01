package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.SortDirection;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
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
    @Mock private MasterBlMapper masterBlMapper;

    @InjectMocks
    private MasterBlPersistenceAdapter adapter;

    // ── saveMasterBl(SEA) ─────────────────────────────────────────────

    @Test
    @DisplayName("saveMasterBl(SEA): replaceDesc 호출, syncDims/syncScheduleLegs/syncAirCharges 미호출")
    void saveMasterBl_sea_callsSyncMethodsInOrder() {
        MasterBlSea sea = MasterBlSea.create(Bound.EXP);
        MasterBlJpaEntity savedJpa = spy(new MasterBlJpaEntity());
        savedJpa.setJobDiv(MasterBlJobDiv.SEA);
        given(masterBlRepository.save(any())).willReturn(savedJpa);
        given(masterBlSeaRepository.findByMasterBlMasterBlId(any())).willReturn(Optional.empty());
        given(masterBlMapper.toSeaDomain(eq(savedJpa), any())).willReturn(sea);

        adapter.saveMasterBl(sea);

        then(savedJpa).should().replaceDesc(any());
        then(savedJpa).should(never()).syncDims(any());
        then(savedJpa).should(never()).syncScheduleLegs(any());
        then(savedJpa).should(never()).syncAirCharges(any());
        then(masterBlSeaRepository).should().save(any());
    }

    // ── saveMasterBl(AIR) ─────────────────────────────────────────────

    @Test
    @DisplayName("saveMasterBl(AIR): syncAirCharges→syncDims→syncScheduleLegs→replaceDesc 순서 후 airRepository.save 호출")
    void saveMasterBl_air_callsSyncMethodsInOrder() {
        MasterBlAir air = MasterBlAir.create(Bound.EXP);
        MasterBlJpaEntity savedJpa = spy(new MasterBlJpaEntity());
        savedJpa.setJobDiv(MasterBlJobDiv.AIR);
        given(masterBlRepository.save(any())).willReturn(savedJpa);
        given(masterBlAirRepository.findByMasterBlMasterBlId(any())).willReturn(Optional.empty());
        given(masterBlMapper.toAirDomain(eq(savedJpa), any())).willReturn(air);

        adapter.saveMasterBl(air);

        org.mockito.InOrder order = inOrder(savedJpa, masterBlAirRepository);
        order.verify(savedJpa).syncAirCharges(any());
        order.verify(savedJpa).syncDims(any());
        order.verify(savedJpa).syncScheduleLegs(any());
        order.verify(savedJpa).replaceDesc(any());
        order.verify(masterBlAirRepository).save(any());
    }

    // ── saveMasterBl — 기존 ID 없을 때 예외 ─────────────────────────

    @Test
    @DisplayName("saveMasterBl: domain.getId() != null이고 repository가 empty → ResourceNotFoundException")
    void saveMasterBl_withExistingId_whenNotFound_throwsResourceNotFound() {
        MasterBlSea sea = MasterBlSea.create(Bound.EXP);
        sea.assignIdentity(99L, null, null, null, null);
        given(masterBlRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.saveMasterBl(sea))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── saveMasterBl — 지원하지 않는 타입 ───────────────────────────

    @Test
    @DisplayName("saveMasterBl: 지원하지 않는 MasterBl 서브타입 → IllegalArgumentException")
    void saveMasterBl_unsupportedType_throwsIllegalArgumentException() {
        // MasterBl의 생성자가 protected이므로 Mockito.mock으로 임의 서브클래스 생성 → default 분기 유발
        MasterBl unknown = mock(MasterBl.class);
        MasterBlJpaEntity savedJpa = new MasterBlJpaEntity();
        given(masterBlRepository.save(any())).willReturn(savedJpa);

        assertThatThrownBy(() -> adapter.saveMasterBl(unknown))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported MasterBl type");
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
        given(masterBlMapper.toSeaDomain(eq(jpa), any())).willReturn(MasterBlSea.create(Bound.EXP));

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
        given(masterBlMapper.toSeaDomain(eq(jpa), any())).willReturn(expected);

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

    // ── deleteMasterBl 위임 ───────────────────────────────────────────

    @Test
    @DisplayName("deleteMasterBl: sea/air ext 삭제 후 masterBlRepository.deleteById 호출")
    void deleteMasterBl_delegatesToRepository() {
        MasterBlSea sea = MasterBlSea.create(Bound.EXP);
        sea.assignIdentity(10L, null, null, null, null);
        given(masterBlSeaRepository.findByMasterBlMasterBlId(10L)).willReturn(Optional.empty());
        given(masterBlAirRepository.findByMasterBlMasterBlId(10L)).willReturn(Optional.empty());

        adapter.deleteMasterBl(sea);

        org.mockito.InOrder order = inOrder(masterBlSeaRepository, masterBlAirRepository, masterBlRepository);
        order.verify(masterBlSeaRepository).findByMasterBlMasterBlId(10L);
        order.verify(masterBlAirRepository).findByMasterBlMasterBlId(10L);
        order.verify(masterBlRepository).deleteById(10L);
    }
}
