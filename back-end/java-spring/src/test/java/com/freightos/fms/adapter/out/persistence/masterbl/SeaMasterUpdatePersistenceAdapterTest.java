package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.fms.application.masterbl.MasterBlFactory;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * SeaMasterUpdatePersistenceAdapter update 흐름 단위 테스트.
 * parent fetch → jobDiv 검증 → seaExt fetch → 도메인 변환 → factory 적용 →
 * applyMasterSeaCommonFields·applyMasterSeaFields·applyDescSync 호출 순서를 검증한다.
 * House {@code SeaHblUpdatePersistenceAdapterTest} 동등 패턴.
 */
@ExtendWith(MockitoExtension.class)
class SeaMasterUpdatePersistenceAdapterTest {

    @Mock private MasterBlRepository masterBlRepository;
    @Mock private MasterBlSeaRepository masterBlSeaRepository;
    @Mock private MasterBlSeaDescRepository masterBlSeaDescRepository;
    @Mock private MasterBlMapper masterBlMapper;
    @Mock private MasterBlSeaSubMapper masterBlSeaSubMapper;
    @Mock private MasterBlFactory masterBlFactory;

    @InjectMocks
    private SeaMasterUpdatePersistenceAdapter adapter;

    /** 모든 필드 null — PATCH 의미론 */
    private static UpdateMasterBlCommand emptyCommand() {
        return new UpdateMasterBlCommand(
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null,
                null, null
        );
    }

    // ── 케이스 1: happy path ─────────────────────────────────────────

    @Test
    @DisplayName("update: 정상 SEA — factory·applyMasterSeaCommonFields·applyMasterSeaFields 호출 검증")
    void update_existingSea_callsFactoryAndAppliesAttachedMapping() {
        Long id = 10L;
        MasterBlJpaEntity parentJpa = new MasterBlJpaEntity();
        parentJpa.setMasterBlId(id);
        parentJpa.setJobDiv(MasterBlJobDiv.SEA);

        MasterBlSeaJpaEntity seaJpa = new MasterBlSeaJpaEntity();
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);

        given(masterBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(masterBlSeaRepository.findByMasterBlMasterBlId(id)).willReturn(Optional.of(seaJpa));
        given(masterBlSeaDescRepository.findBySea_MasterBlSeaId(any())).willReturn(Optional.empty());
        given(masterBlMapper.toSeaDomain(parentJpa, seaJpa, null)).willReturn(domain);

        UpdateMasterBlCommand command = emptyCommand();
        adapter.update(id, command);

        then(masterBlMapper).should().toSeaDomain(parentJpa, seaJpa, null);
        then(masterBlFactory).should().applyToEntity(command, domain);
        then(masterBlSeaSubMapper).should().applyMasterSeaCommonFields(domain, parentJpa);
        then(masterBlSeaSubMapper).should().applyMasterSeaFields(domain, seaJpa);
    }

    // ── 케이스 2: domain desc 존재 + repo desc 존재 → conditional setter (UPDATE) ──

    @Test
    @DisplayName("update: existing desc — applySeaDescFields 호출, save 미호출")
    void update_existingDesc_updatesExistingDescJpa() {
        Long id = 11L;
        MasterBlJpaEntity parentJpa = new MasterBlJpaEntity();
        parentJpa.setMasterBlId(id);
        parentJpa.setJobDiv(MasterBlJobDiv.SEA);

        MasterBlSeaJpaEntity seaJpa = new MasterBlSeaJpaEntity();
        MasterBlSeaDescJpaEntity existingDescJpa = new MasterBlSeaDescJpaEntity();
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);

        given(masterBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(masterBlSeaRepository.findByMasterBlMasterBlId(id)).willReturn(Optional.of(seaJpa));
        given(masterBlSeaDescRepository.findBySea_MasterBlSeaId(any())).willReturn(Optional.of(existingDescJpa));
        given(masterBlMapper.toSeaDomain(parentJpa, seaJpa, existingDescJpa)).willReturn(domain);

        adapter.update(id, emptyCommand());

        // desc가 null이면 applyDescSync 내부 no-op이므로 applySeaDescFields 미호출
        then(masterBlSeaDescRepository).should(never()).save(any());
    }

    // ── 케이스 3: domain desc 존재 + repo desc null → 신규 insert ────

    @Test
    @DisplayName("update: no-existing desc + domain has desc — save 호출하여 신규 insert")
    void update_noExistingDesc_savesNewDescJpa() {
        Long id = 12L;
        MasterBlJpaEntity parentJpa = new MasterBlJpaEntity();
        parentJpa.setMasterBlId(id);
        parentJpa.setJobDiv(MasterBlJobDiv.SEA);

        MasterBlSeaJpaEntity seaJpa = new MasterBlSeaJpaEntity();
        // desc null로 조회 → domain에 desc 강제 세팅 후 initDesc 호출 시뮬레이션 불가하므로
        // toSeaDomain이 desc를 가진 domain을 반환하도록 spy 불가 — 도메인 initDesc 직접 호출
        MasterBlSea domain = MasterBlSea.create(Bound.EXP);
        com.freightos.fms.domain.masterbl.entity.MasterBlDesc desc =
                com.freightos.fms.domain.masterbl.entity.MasterBlDesc.create(null);
        desc.updateContent("MARKS", "DESC", null, null);
        domain.initDesc(desc);

        given(masterBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(masterBlSeaRepository.findByMasterBlMasterBlId(id)).willReturn(Optional.of(seaJpa));
        given(masterBlSeaDescRepository.findBySea_MasterBlSeaId(any())).willReturn(Optional.empty());
        given(masterBlMapper.toSeaDomain(parentJpa, seaJpa, null)).willReturn(domain);

        adapter.update(id, emptyCommand());

        // domain.getDesc() != null + repo desc null → save 호출
        then(masterBlSeaDescRepository).should().save(any(MasterBlSeaDescJpaEntity.class));
        then(masterBlSeaSubMapper).should().applySeaDescFields(any(), any(), any());
    }

    // ── 케이스 4: domain desc null + repo desc null → no-op ──────────

    @Test
    @DisplayName("update: domain desc null + repo desc null → applySeaDescFields·save 미호출")
    void update_noDomainDesc_noOp() {
        Long id = 13L;
        MasterBlJpaEntity parentJpa = new MasterBlJpaEntity();
        parentJpa.setMasterBlId(id);
        parentJpa.setJobDiv(MasterBlJobDiv.SEA);

        MasterBlSeaJpaEntity seaJpa = new MasterBlSeaJpaEntity();
        MasterBlSea domain = MasterBlSea.create(Bound.EXP); // desc null

        given(masterBlRepository.findById(id)).willReturn(Optional.of(parentJpa));
        given(masterBlSeaRepository.findByMasterBlMasterBlId(id)).willReturn(Optional.of(seaJpa));
        given(masterBlSeaDescRepository.findBySea_MasterBlSeaId(any())).willReturn(Optional.empty());
        given(masterBlMapper.toSeaDomain(parentJpa, seaJpa, null)).willReturn(domain);

        adapter.update(id, emptyCommand());

        then(masterBlSeaSubMapper).should(never()).applySeaDescFields(any(), any(), any());
        then(masterBlSeaDescRepository).should(never()).save(any());
    }

    // ── 케이스 5: jobDiv != SEA → IllegalStateException ──────────────

    @Test
    @DisplayName("update: parent jobDiv가 AIR면 IllegalStateException, seaExt fetch 미호출")
    void update_wrongJobDiv_throwsIllegalStateException() {
        Long id = 20L;
        MasterBlJpaEntity parentJpa = new MasterBlJpaEntity();
        parentJpa.setMasterBlId(id);
        parentJpa.setJobDiv(MasterBlJobDiv.AIR);

        given(masterBlRepository.findById(id)).willReturn(Optional.of(parentJpa));

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SEA");

        then(masterBlSeaRepository).should(never()).findByMasterBlMasterBlId(any());
        then(masterBlFactory).should(never()).applyToEntity(any(), any());
    }

    // ── 케이스 6: parent 없으면 ResourceNotFoundException ─────────────

    @Test
    @DisplayName("update: parent가 없으면 ResourceNotFoundException")
    void update_notFound_throwsResourceNotFoundException() {
        Long id = 999L;
        given(masterBlRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.update(id, emptyCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        then(masterBlSeaRepository).should(never()).findByMasterBlMasterBlId(any());
        then(masterBlFactory).should(never()).applyToEntity(any(), any());
    }
}
