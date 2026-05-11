package com.freightos.fms.adapter.out.persistence.housebl.strategy;

import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.HouseBlNonBlContainerRepository;
import com.freightos.fms.adapter.out.persistence.nonbl.HouseBlNonBlDimRepository;
import com.freightos.fms.adapter.out.persistence.nonbl.HouseBlNonBlRepository;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
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
import static org.mockito.Mockito.spy;

/**
 * HouseBlNonBlPersistenceStrategy 단위 테스트.
 * NON_BL 전용 mergeContainers/mergeDims 호출 순서 및 desc 미사용 분기를 검증한다.
 * (기존 HouseBlPersistenceAdapterTest L199~222, L524~543, L626~643 검증 의도를 이전)
 */
@ExtendWith(MockitoExtension.class)
class HouseBlNonBlPersistenceStrategyTest {

    @Mock private HouseBlNonBlRepository houseBlNonBlRepository;
    @Mock private HouseBlNonBlContainerRepository houseBlNonBlContainerRepository;
    @Mock private HouseBlNonBlDimRepository houseBlNonBlDimRepository;
    @Mock private HouseBlJpaToDomainMapper jpaToDomainMapper;
    @Mock private HouseBlDomainToJpaMapper domainToJpaMapper;
    @Mock private HouseBlCargoMapper houseBlCargoMapper;

    @InjectMocks
    private HouseBlNonBlPersistenceStrategy strategy;

    // ── saveExt ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveExt(NON_BL): nonBlJpa.mergeContainers → nonBlJpa.mergeDims → nonBlRepository.save 순서")
    void saveExt_nonBl_mergesContainersDimsThenSaves() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        HouseBlJpaEntity savedParent = new HouseBlJpaEntity();
        HouseBlNonBlJpaEntity existingNonBlJpa = spy(new HouseBlNonBlJpaEntity());
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.of(existingNonBlJpa));
        given(houseBlNonBlRepository.save(any())).willReturn(existingNonBlJpa);

        strategy.saveExt(nonBl, savedParent);

        InOrder order = inOrder(existingNonBlJpa, houseBlNonBlRepository);
        order.verify(existingNonBlJpa).mergeContainers(any());
        order.verify(existingNonBlJpa).mergeDims(any());
        order.verify(houseBlNonBlRepository).save(any());
    }

    @Test
    @DisplayName("saveExt(NON_BL): remark 필드가 있어도 jpaToDomainMapper 미호출 — NON_BL은 역방향 sync 패치로 응답 (SELECT 생략)")
    void saveExt_nonBl_withRemark_doesNotCallMapper() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        nonBl.updateRemark("REMARK_TEXT");
        HouseBlJpaEntity savedParent = new HouseBlJpaEntity();
        HouseBlNonBlJpaEntity existingNonBlJpa = spy(new HouseBlNonBlJpaEntity());
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.of(existingNonBlJpa));
        given(houseBlNonBlRepository.save(any())).willReturn(existingNonBlJpa);

        strategy.saveExt(nonBl, savedParent);

        // NON_BL saveExt는 역방향 sync 패치 흐름 — DB 재조회(toNonBlDomain) 없이 in-memory domain 반환
        then(jpaToDomainMapper).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("saveExt(NON_BL): nonBlRepository.findByHouseBlHouseBlId → 없으면 새 엔티티로 insert")
    void saveExt_nonBl_notFound_createsNewEntity() {
        HouseBlNonBl nonBl = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        HouseBlJpaEntity savedParent = new HouseBlJpaEntity();
        HouseBlNonBlJpaEntity newNonBlJpa = spy(new HouseBlNonBlJpaEntity());
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlNonBlRepository.save(any())).willReturn(newNonBlJpa);

        strategy.saveExt(nonBl, savedParent);

        then(houseBlNonBlRepository).should().save(any());
    }

    // ── loadWithExt ──────────────────────────────────────────────────────

    @Test
    @DisplayName("loadWithExt(NON_BL): nonBlRepository 조회 → toNonBlDomain 호출")
    void loadWithExt_nonBl_callsNonBlRepoThenMapper() {
        HouseBlJpaEntity parent = new HouseBlJpaEntity();
        HouseBlNonBl expected = HouseBlNonBl.create(HouseBlNonBl.WorkDivision.SEA, Bound.EXP);
        given(houseBlNonBlRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toNonBlDomain(any(), any())).willReturn(expected);

        strategy.loadWithExt(parent);

        then(houseBlNonBlRepository).should().findByHouseBlHouseBlId(any());
        then(jpaToDomainMapper).should().toNonBlDomain(any(), any());
    }

    // ── deleteExt ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteExt(NON_BL): nonBlContainer → nonBlDim → nonBlExt 순서로 삭제")
    void deleteExt_nonBl_deletesChildrenThenExtInOrder() {
        strategy.deleteExt(53L);

        InOrder order = inOrder(houseBlNonBlContainerRepository, houseBlNonBlDimRepository, houseBlNonBlRepository);
        order.verify(houseBlNonBlContainerRepository).deleteByParentHouseBlId(53L);
        order.verify(houseBlNonBlDimRepository).deleteByParentHouseBlId(53L);
        order.verify(houseBlNonBlRepository).deleteByHouseBl_HouseBlId(53L);
    }

    // ── jobDiv 확인 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("jobDiv(): NON_BL 반환")
    void jobDiv_returnsNonBl() {
        assert strategy.jobDiv() == JobDiv.NON_BL;
    }
}
