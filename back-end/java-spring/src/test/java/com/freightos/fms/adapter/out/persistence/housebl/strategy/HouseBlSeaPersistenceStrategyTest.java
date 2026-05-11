package com.freightos.fms.adapter.out.persistence.housebl.strategy;

import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDocMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlSeaContainerRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlSeaDescRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlSeaRepository;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

/**
 * HouseBlSeaPersistenceStrategy 단위 테스트.
 * SEA 전용 Repository/Mapper 호출 순서 및 desc 저장·삭제 분기를 검증한다.
 * (기존 HouseBlPersistenceAdapterTest L114~168 검증 의도를 이전)
 */
@ExtendWith(MockitoExtension.class)
class HouseBlSeaPersistenceStrategyTest {

    @Mock private HouseBlSeaRepository houseBlSeaRepository;
    @Mock private HouseBlSeaDescRepository houseBlSeaDescRepository;
    @Mock private HouseBlSeaContainerRepository houseBlSeaContainerRepository;
    @Mock private HouseBlJpaToDomainMapper jpaToDomainMapper;
    @Mock private HouseBlDomainToJpaMapper domainToJpaMapper;
    @Mock private HouseBlCargoMapper houseBlCargoMapper;
    @Mock private HouseBlDocMapper houseBlDocMapper;

    @InjectMocks
    private HouseBlSeaPersistenceStrategy strategy;

    // ── saveExt ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveExt(SEA): seaRepository.save → savedSeaJpa.syncContainers 순서 검증")
    void saveExt_sea_savesSeaExtThenSyncsContainers() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        HouseBlJpaEntity savedParent = new HouseBlJpaEntity();
        HouseBlSeaJpaEntity savedSeaJpa = spy(new HouseBlSeaJpaEntity());
        given(houseBlSeaRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlSeaRepository.save(any())).willReturn(savedSeaJpa);
        given(houseBlSeaDescRepository.findBySea_HouseBlSeaId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(any(), any(), any())).willReturn(sea);

        strategy.saveExt(sea, savedParent);

        InOrder order = inOrder(houseBlSeaRepository, savedSeaJpa);
        order.verify(houseBlSeaRepository).save(any());
        order.verify(savedSeaJpa).syncContainers(any());
    }

    @Test
    @DisplayName("saveExt(SEA): desc가 null이면 seaDescRepository.findBySea_HouseBlSeaId 조회 후 기존 없으면 save 미호출")
    void saveExt_sea_descNull_doesNotSaveDesc() {
        HouseBlSea sea = HouseBlSea.create(Bound.EXP);
        HouseBlJpaEntity savedParent = new HouseBlJpaEntity();
        HouseBlSeaJpaEntity savedSeaJpa = spy(new HouseBlSeaJpaEntity());
        given(houseBlSeaRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlSeaRepository.save(any())).willReturn(savedSeaJpa);
        given(houseBlSeaDescRepository.findBySea_HouseBlSeaId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(any(), any(), any())).willReturn(sea);

        strategy.saveExt(sea, savedParent);

        // desc가 null이므로 seaDescRepository.save 미호출
        then(houseBlSeaDescRepository).should(never()).save(any());
    }

    // ── loadWithExt ──────────────────────────────────────────────────────

    @Test
    @DisplayName("loadWithExt(SEA): seaRepository 조회 후 seaDescRepository 조회 → toSeaDomain 호출")
    void loadWithExt_sea_callsSeaRepoThenSeaDescRepo() {
        HouseBlJpaEntity parent = new HouseBlJpaEntity();
        HouseBlSeaJpaEntity seaJpa = new HouseBlSeaJpaEntity();
        HouseBlSea expected = HouseBlSea.create(Bound.EXP);
        given(houseBlSeaRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.of(seaJpa));
        given(houseBlSeaDescRepository.findBySea_HouseBlSeaId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(any(), any(), any())).willReturn(expected);

        strategy.loadWithExt(parent);

        then(houseBlSeaRepository).should().findByHouseBlHouseBlId(any());
        then(houseBlSeaDescRepository).should().findBySea_HouseBlSeaId(any());
        then(jpaToDomainMapper).should().toSeaDomain(any(), any(), any());
    }

    @Test
    @DisplayName("loadWithExt(SEA): seaRepository 조회 결과 없으면 seaDescRepository 미호출")
    void loadWithExt_sea_noSeaExt_doesNotCallSeaDescRepo() {
        HouseBlJpaEntity parent = new HouseBlJpaEntity();
        HouseBlSea expected = HouseBlSea.create(Bound.EXP);
        given(houseBlSeaRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toSeaDomain(any(), any(), any())).willReturn(expected);

        strategy.loadWithExt(parent);

        then(houseBlSeaDescRepository).should(never()).findBySea_HouseBlSeaId(any());
    }

    // ── deleteExt ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteExt(SEA): seaContainer → seaDesc → seaExt 순서로 삭제")
    void deleteExt_sea_deletesChildrenThenExtInOrder() {
        strategy.deleteExt(50L);

        InOrder order = inOrder(houseBlSeaContainerRepository, houseBlSeaDescRepository, houseBlSeaRepository);
        order.verify(houseBlSeaContainerRepository).deleteByParentHouseBlId(50L);
        order.verify(houseBlSeaDescRepository).deleteByParentHouseBlId(50L);
        order.verify(houseBlSeaRepository).deleteByHouseBl_HouseBlId(50L);
    }

    // ── jobDiv 확인 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("jobDiv(): SEA 반환")
    void jobDiv_returnsSea() {
        assert strategy.jobDiv() == JobDiv.SEA;
    }
}
