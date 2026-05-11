package com.freightos.fms.adapter.out.persistence.housebl.strategy;

import com.freightos.fms.adapter.out.persistence.housebl.HouseBlAirChargeRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlAirDescRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlAirDimRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlAirRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDocMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlScheduleLegRepository;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
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
 * HouseBlAirPersistenceStrategy 단위 테스트.
 * AIR 전용 Repository/Mapper 호출 순서 및 desc 저장·삭제 분기를 검증한다.
 * (기존 HouseBlPersistenceAdapterTest L69~109 검증 의도를 이전)
 */
@ExtendWith(MockitoExtension.class)
class HouseBlAirPersistenceStrategyTest {

    @Mock private HouseBlAirRepository houseBlAirRepository;
    @Mock private HouseBlAirDescRepository houseBlAirDescRepository;
    @Mock private HouseBlAirDimRepository houseBlAirDimRepository;
    @Mock private HouseBlScheduleLegRepository houseBlScheduleLegRepository;
    @Mock private HouseBlAirChargeRepository houseBlAirChargeRepository;
    @Mock private HouseBlJpaToDomainMapper jpaToDomainMapper;
    @Mock private HouseBlDomainToJpaMapper domainToJpaMapper;
    @Mock private HouseBlCargoMapper houseBlCargoMapper;
    @Mock private HouseBlDocMapper houseBlDocMapper;

    @InjectMocks
    private HouseBlAirPersistenceStrategy strategy;

    // ── saveExt ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveExt(AIR): airRepository.save → savedAirJpa.syncDims → syncScheduleLegs → syncAirCharges 순서")
    void saveExt_air_callsSyncInOrder() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlJpaEntity savedParent = new HouseBlJpaEntity();
        HouseBlAirJpaEntity savedAirJpa = spy(new HouseBlAirJpaEntity());
        given(houseBlAirRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlAirRepository.save(any())).willReturn(savedAirJpa);
        given(houseBlAirDescRepository.findByAir_HouseBlAirId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toAirDomain(any(), any(), any())).willReturn(air);

        strategy.saveExt(air, savedParent);

        InOrder order = inOrder(houseBlAirRepository, savedAirJpa);
        order.verify(houseBlAirRepository).save(any());
        order.verify(savedAirJpa).syncDims(any());
        order.verify(savedAirJpa).syncScheduleLegs(any());
        order.verify(savedAirJpa).syncAirCharges(any());
    }

    @Test
    @DisplayName("saveExt(AIR): desc가 null이면 airDescRepository.save 미호출")
    void saveExt_air_descNull_doesNotSaveDesc() {
        HouseBlAir air = HouseBlAir.create(Bound.EXP);
        HouseBlJpaEntity savedParent = new HouseBlJpaEntity();
        HouseBlAirJpaEntity savedAirJpa = spy(new HouseBlAirJpaEntity());
        given(houseBlAirRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlAirRepository.save(any())).willReturn(savedAirJpa);
        given(houseBlAirDescRepository.findByAir_HouseBlAirId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toAirDomain(any(), any(), any())).willReturn(air);

        strategy.saveExt(air, savedParent);

        // desc가 null이므로 airDescRepository.save 미호출
        then(houseBlAirDescRepository).should(never()).save(any());
    }

    // ── loadWithExt ──────────────────────────────────────────────────────

    @Test
    @DisplayName("loadWithExt(AIR): airRepository 조회 후 airDescRepository 조회 → toAirDomain 호출")
    void loadWithExt_air_callsAirRepoThenAirDescRepo() {
        HouseBlJpaEntity parent = new HouseBlJpaEntity();
        HouseBlAirJpaEntity airJpa = new HouseBlAirJpaEntity();
        HouseBlAir expected = HouseBlAir.create(Bound.EXP);
        given(houseBlAirRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.of(airJpa));
        given(houseBlAirDescRepository.findByAir_HouseBlAirId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toAirDomain(any(), any(), any())).willReturn(expected);

        strategy.loadWithExt(parent);

        then(houseBlAirRepository).should().findByHouseBlHouseBlId(any());
        then(houseBlAirDescRepository).should().findByAir_HouseBlAirId(any());
        then(jpaToDomainMapper).should().toAirDomain(any(), any(), any());
    }

    @Test
    @DisplayName("loadWithExt(AIR): airRepository 조회 결과 없으면 airDescRepository 미호출")
    void loadWithExt_air_noAirExt_doesNotCallAirDescRepo() {
        HouseBlJpaEntity parent = new HouseBlJpaEntity();
        HouseBlAir expected = HouseBlAir.create(Bound.EXP);
        given(houseBlAirRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toAirDomain(any(), any(), any())).willReturn(expected);

        strategy.loadWithExt(parent);

        then(houseBlAirDescRepository).should(never()).findByAir_HouseBlAirId(any());
    }

    // ── deleteExt ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteExt(AIR): airDim → scheduleLeg → airCharge → airDesc → airExt 순서로 삭제")
    void deleteExt_air_deletesChildrenThenExtInOrder() {
        strategy.deleteExt(51L);

        InOrder order = inOrder(houseBlAirDimRepository, houseBlScheduleLegRepository,
                houseBlAirChargeRepository, houseBlAirDescRepository, houseBlAirRepository);
        order.verify(houseBlAirDimRepository).deleteByParentHouseBlId(51L);
        order.verify(houseBlScheduleLegRepository).deleteByParentHouseBlId(51L);
        order.verify(houseBlAirChargeRepository).deleteByParentHouseBlId(51L);
        order.verify(houseBlAirDescRepository).deleteByParentHouseBlId(51L);
        order.verify(houseBlAirRepository).deleteByHouseBl_HouseBlId(51L);
    }

    // ── jobDiv 확인 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("jobDiv(): AIR 반환")
    void jobDiv_returnsAir() {
        assert strategy.jobDiv() == JobDiv.AIR;
    }
}
