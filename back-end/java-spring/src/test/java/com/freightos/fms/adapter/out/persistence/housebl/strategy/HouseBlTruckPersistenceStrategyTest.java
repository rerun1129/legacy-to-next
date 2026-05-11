package com.freightos.fms.adapter.out.persistence.housebl.strategy;

import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDocMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlTruckDescRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlTruckDimRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlTruckOrderRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlTruckRepository;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
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
 * HouseBlTruckPersistenceStrategy 단위 테스트.
 * TRUCK 전용 Repository/Mapper 호출 순서 및 desc 저장·삭제 분기를 검증한다.
 * (기존 HouseBlPersistenceAdapterTest L173~195, L359~377 검증 의도를 이전)
 */
@ExtendWith(MockitoExtension.class)
class HouseBlTruckPersistenceStrategyTest {

    @Mock private HouseBlTruckRepository houseBlTruckRepository;
    @Mock private HouseBlTruckDescRepository houseBlTruckDescRepository;
    @Mock private HouseBlTruckDimRepository houseBlTruckDimRepository;
    @Mock private HouseBlTruckOrderRepository houseBlTruckOrderRepository;
    @Mock private HouseBlJpaToDomainMapper jpaToDomainMapper;
    @Mock private HouseBlDomainToJpaMapper domainToJpaMapper;
    @Mock private HouseBlCargoMapper houseBlCargoMapper;
    @Mock private HouseBlDocMapper houseBlDocMapper;

    @InjectMocks
    private HouseBlTruckPersistenceStrategy strategy;

    // ── saveExt ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveExt(TRUCK): truckRepository.save → savedTruckJpa.syncDims → syncTruckOrders 순서")
    void saveExt_truck_callsSyncDimsThenSyncTruckOrders() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        HouseBlJpaEntity savedParent = new HouseBlJpaEntity();
        HouseBlTruckJpaEntity savedTruckJpa = spy(new HouseBlTruckJpaEntity());
        given(houseBlTruckRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlTruckRepository.save(any())).willReturn(savedTruckJpa);
        given(houseBlTruckDescRepository.findByTruck_HouseBlTruckId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toTruckDomain(any(), any(), any())).willReturn(truck);

        strategy.saveExt(truck, savedParent);

        InOrder order = inOrder(houseBlTruckRepository, savedTruckJpa);
        order.verify(houseBlTruckRepository).save(any());
        order.verify(savedTruckJpa).syncDims(any());
        order.verify(savedTruckJpa).syncTruckOrders(any());
    }

    @Test
    @DisplayName("saveExt(TRUCK): desc가 null이면 truckDescRepository.save 미호출, truckDescRepository.findBy만 호출")
    void saveExt_truck_descNull_doesNotSaveDesc() {
        HouseBlTruck truck = HouseBlTruck.create(Bound.EXP);
        HouseBlJpaEntity savedParent = new HouseBlJpaEntity();
        HouseBlTruckJpaEntity savedTruckJpa = spy(new HouseBlTruckJpaEntity());
        given(houseBlTruckRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(houseBlTruckRepository.save(any())).willReturn(savedTruckJpa);
        given(houseBlTruckDescRepository.findByTruck_HouseBlTruckId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toTruckDomain(any(), any(), any())).willReturn(truck);

        strategy.saveExt(truck, savedParent);

        // desc가 null이므로 truckDescRepository.save 미호출
        then(houseBlTruckDescRepository).should(never()).save(any());
        // truckDescRepository 조회는 saveOrDeleteTruckDesc 내부에서 호출됨
        then(houseBlTruckDescRepository).should().findByTruck_HouseBlTruckId(any());
    }

    // ── loadWithExt ──────────────────────────────────────────────────────

    @Test
    @DisplayName("loadWithExt(TRUCK): truckRepository 조회 후 truckDescRepository 조회 → toTruckDomain 호출")
    void loadWithExt_truck_callsTruckRepoThenTruckDescRepo() {
        HouseBlJpaEntity parent = new HouseBlJpaEntity();
        HouseBlTruckJpaEntity truckJpa = new HouseBlTruckJpaEntity();
        HouseBlTruck expected = HouseBlTruck.create(Bound.EXP);
        given(houseBlTruckRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.of(truckJpa));
        given(houseBlTruckDescRepository.findByTruck_HouseBlTruckId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toTruckDomain(any(), any(), any())).willReturn(expected);

        strategy.loadWithExt(parent);

        then(houseBlTruckRepository).should().findByHouseBlHouseBlId(any());
        then(houseBlTruckDescRepository).should().findByTruck_HouseBlTruckId(any());
        then(jpaToDomainMapper).should().toTruckDomain(any(), any(), any());
    }

    @Test
    @DisplayName("loadWithExt(TRUCK): truckRepository 조회 결과 없으면 truckDescRepository 미호출")
    void loadWithExt_truck_noTruckExt_doesNotCallTruckDescRepo() {
        HouseBlJpaEntity parent = new HouseBlJpaEntity();
        HouseBlTruck expected = HouseBlTruck.create(Bound.EXP);
        given(houseBlTruckRepository.findByHouseBlHouseBlId(any())).willReturn(Optional.empty());
        given(jpaToDomainMapper.toTruckDomain(any(), any(), any())).willReturn(expected);

        strategy.loadWithExt(parent);

        then(houseBlTruckDescRepository).should(never()).findByTruck_HouseBlTruckId(any());
    }

    // ── deleteExt ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteExt(TRUCK): truckDim → truckOrder → truckDesc → truckExt 순서로 삭제")
    void deleteExt_truck_deletesChildrenThenExtInOrder() {
        strategy.deleteExt(52L);

        InOrder order = inOrder(houseBlTruckDimRepository, houseBlTruckOrderRepository,
                houseBlTruckDescRepository, houseBlTruckRepository);
        order.verify(houseBlTruckDimRepository).deleteByParentHouseBlId(52L);
        order.verify(houseBlTruckOrderRepository).deleteByParentHouseBlId(52L);
        order.verify(houseBlTruckDescRepository).deleteByParentHouseBlId(52L);
        order.verify(houseBlTruckRepository).deleteByHouseBl_HouseBlId(52L);
    }

    // ── jobDiv 확인 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("jobDiv(): TRUCK 반환")
    void jobDiv_returnsTruck() {
        assert strategy.jobDiv() == JobDiv.TRUCK;
    }
}
