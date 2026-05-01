package com.freightos.fms.adapter.out.persistence.switchbl;

import com.freightos.fms.adapter.out.persistence.housebl.HouseBlRepository;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlDescriptionJpaEntity;
import com.freightos.fms.adapter.out.persistence.switchbl.entity.SwitchBlJpaEntity;
import com.freightos.fms.domain.common.vo.CustomerCode;
import com.freightos.fms.domain.switchbl.entity.SwitchBl;
import com.freightos.fms.domain.switchbl.entity.SwitchBlDescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwitchBlPersistenceAdapterTest {

    @Mock private SwitchBlJpaRepository switchBlJpaRepository;
    @Mock private SwitchBlDescriptionJpaRepository switchBlDescriptionJpaRepository;
    @Mock private HouseBlRepository houseBlRepository;
    @Mock private SwitchBlMapper switchBlMapper;

    @InjectMocks
    private SwitchBlPersistenceAdapter adapter;

    // ── saveSwitchBl — 신규 엔티티 ───────────────────────────────────

    @Test
    @DisplayName("saveSwitchBl: HouseBl 기준 기존 SwitchBl 없음 → HouseBl 프록시 설정 후 신규 저장")
    void saveSwitchBl_newEntity_persistsAndReturns() {
        SwitchBl domain = SwitchBl.create(1L, CustomerCode.of("SHIP", null));
        // getSwitchBlId()가 5L을 반환하도록 spy로 구성
        SwitchBlJpaEntity savedJpa = spy(new SwitchBlJpaEntity());
        doReturn(5L).when(savedJpa).getSwitchBlId();
        SwitchBlJpaEntity reloaded = spy(new SwitchBlJpaEntity());

        given(switchBlJpaRepository.findByHouseBlHouseBlId(1L)).willReturn(Optional.empty());
        given(houseBlRepository.getReferenceById(1L)).willReturn(new HouseBlJpaEntity());
        given(switchBlJpaRepository.save(any())).willReturn(savedJpa);
        given(switchBlJpaRepository.findById(5L)).willReturn(Optional.of(reloaded));
        given(switchBlMapper.toDomain(reloaded)).willReturn(domain);

        adapter.saveSwitchBl(domain);

        then(houseBlRepository).should().getReferenceById(1L);
        then(switchBlJpaRepository).should().save(any());
    }

    // ── saveSwitchBl — 기존 엔티티 ──────────────────────────────────

    @Test
    @DisplayName("saveSwitchBl: HouseBl 기준 기존 SwitchBl 존재 → HouseBl 프록시 미설정, 기존 엔티티 update")
    void saveSwitchBl_existingEntity_updatesViaReference() {
        SwitchBl domain = SwitchBl.create(2L, CustomerCode.of("SHIP", null));
        SwitchBlJpaEntity existingJpa = existingJpaWithHouseBl();
        SwitchBlJpaEntity savedJpa = spy(new SwitchBlJpaEntity());
        doReturn(7L).when(savedJpa).getSwitchBlId();
        SwitchBlJpaEntity reloaded = spy(new SwitchBlJpaEntity());

        given(switchBlJpaRepository.findByHouseBlHouseBlId(2L)).willReturn(Optional.of(existingJpa));
        given(switchBlJpaRepository.save(existingJpa)).willReturn(savedJpa);
        given(switchBlJpaRepository.findById(7L)).willReturn(Optional.of(reloaded));
        given(switchBlMapper.toDomain(reloaded)).willReturn(domain);

        adapter.saveSwitchBl(domain);

        // 이미 houseBl이 설정된 경우 getReferenceById는 호출하지 않음
        then(houseBlRepository).should(never()).getReferenceById(any());
    }

    // ── saveSwitchBl — description 있을 때 저장 ──────────────────────

    @Test
    @DisplayName("saveSwitchBl: description != null → descriptionRepository.save 호출")
    void saveSwitchBl_withDescription_savesDescription() {
        SwitchBl domain = SwitchBl.create(3L, CustomerCode.of("SHIP", null));
        SwitchBlDescription desc = SwitchBlDescription.create(3L);
        domain.attachDescription(desc);

        SwitchBlJpaEntity savedJpa = spy(new SwitchBlJpaEntity());
        doReturn(8L).when(savedJpa).getSwitchBlId();
        SwitchBlJpaEntity reloaded = spy(new SwitchBlJpaEntity());

        given(switchBlJpaRepository.findByHouseBlHouseBlId(3L)).willReturn(Optional.empty());
        given(houseBlRepository.getReferenceById(3L)).willReturn(new HouseBlJpaEntity());
        given(switchBlJpaRepository.save(any())).willReturn(savedJpa);
        given(switchBlDescriptionJpaRepository.findBySwitchBlSwitchBlId(8L)).willReturn(Optional.empty());
        given(switchBlJpaRepository.findById(8L)).willReturn(Optional.of(reloaded));
        given(switchBlMapper.toDomain(reloaded)).willReturn(domain);

        adapter.saveSwitchBl(domain);

        then(switchBlDescriptionJpaRepository).should().save(any());
    }

    // ── saveSwitchBl — description null일 때 미호출 ──────────────────

    @Test
    @DisplayName("saveSwitchBl: description == null → descriptionRepository.save 미호출")
    void saveSwitchBl_withNullDescription_doesNotSaveDescription() {
        SwitchBl domain = SwitchBl.create(4L, CustomerCode.of("SHIP", null));
        // description 미설정 → null

        SwitchBlJpaEntity savedJpa = spy(new SwitchBlJpaEntity());
        doReturn(9L).when(savedJpa).getSwitchBlId();
        SwitchBlJpaEntity reloaded = spy(new SwitchBlJpaEntity());

        given(switchBlJpaRepository.findByHouseBlHouseBlId(4L)).willReturn(Optional.empty());
        given(houseBlRepository.getReferenceById(4L)).willReturn(new HouseBlJpaEntity());
        given(switchBlJpaRepository.save(any())).willReturn(savedJpa);
        given(switchBlJpaRepository.findById(9L)).willReturn(Optional.of(reloaded));
        given(switchBlMapper.toDomain(reloaded)).willReturn(domain);

        adapter.saveSwitchBl(domain);

        then(switchBlDescriptionJpaRepository).should(never()).save(any());
    }

    // ── deleteSwitchBl — description → 본체 순서 ─────────────────────

    @Test
    @DisplayName("deleteSwitchBl: description 먼저 삭제 후 본체 deleteById 호출 (InOrder 검증)")
    void deleteSwitchBl_deletesDescriptionBeforeEntity() {
        SwitchBl domain = SwitchBl.create(5L, CustomerCode.of("SHIP", null));
        domain.assignSwitchBlId(20L);
        SwitchBlDescriptionJpaEntity descJpa = new SwitchBlDescriptionJpaEntity();
        given(switchBlDescriptionJpaRepository.findBySwitchBlSwitchBlId(20L))
                .willReturn(Optional.of(descJpa));

        adapter.deleteSwitchBl(domain);

        InOrder order = inOrder(switchBlDescriptionJpaRepository, switchBlJpaRepository);
        order.verify(switchBlDescriptionJpaRepository).delete(descJpa);
        order.verify(switchBlJpaRepository).deleteById(20L);
    }

    // ── findSwitchBlById — 없을 때 empty 반환 ────────────────────────

    @Test
    @DisplayName("findSwitchBlById: id 존재하지 않음 → Optional.empty 반환, mapper 미호출")
    void findSwitchBlById_whenNotFound_returnsEmpty() {
        given(switchBlJpaRepository.findById(999L)).willReturn(Optional.empty());

        Optional<SwitchBl> result = adapter.findSwitchBlById(999L);

        assertThat(result).isEmpty();
        then(switchBlMapper).should(never()).toDomain(any());
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────

    /** houseBl이 이미 설정된 기존 JPA 엔티티 */
    private SwitchBlJpaEntity existingJpaWithHouseBl() {
        SwitchBlJpaEntity jpa = new SwitchBlJpaEntity();
        jpa.setHouseBl(new HouseBlJpaEntity());
        return jpa;
    }
}
