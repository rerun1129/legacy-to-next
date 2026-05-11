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
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlTruckOrderJpaEntity;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * TRUCK jobDiv 전용 영속성 전략.
 * house_bl_truck + house_bl_truck_dim + house_bl_truck_order + house_bl_truck_desc 처리 담당.
 */
@Component
@RequiredArgsConstructor
public class HouseBlTruckPersistenceStrategy implements HouseBlPersistenceStrategy<HouseBlTruck> {

    private final HouseBlTruckRepository houseBlTruckRepository;
    private final HouseBlTruckDescRepository houseBlTruckDescRepository;
    private final HouseBlTruckDimRepository houseBlTruckDimRepository;
    private final HouseBlTruckOrderRepository houseBlTruckOrderRepository;
    private final HouseBlJpaToDomainMapper jpaToDomainMapper;
    private final HouseBlDomainToJpaMapper domainToJpaMapper;
    private final HouseBlCargoMapper houseBlCargoMapper;
    private final HouseBlDocMapper houseBlDocMapper;

    @Override
    public JobDiv jobDiv() {
        return JobDiv.TRUCK;
    }

    @Override
    public HouseBlTruck saveExt(HouseBlTruck truck, HouseBlJpaEntity savedParent) {
        HouseBlTruckJpaEntity truckJpa = houseBlTruckRepository.findByHouseBlHouseBlId(savedParent.getHouseBlId())
                .orElseGet(HouseBlTruckJpaEntity::new);
        truckJpa.setHouseBl(savedParent);
        domainToJpaMapper.applyTruckFields(truck, truckJpa);
        // truckJpa를 먼저 영속화하여 house_bl_truck_id PK 확보 후 dims/truckOrders 동기화
        HouseBlTruckJpaEntity savedTruckJpa = houseBlTruckRepository.save(truckJpa);
        List<HouseBlTruckDimJpaEntity> truckDims = truck.getDims().stream()
                .map(houseBlCargoMapper::toTruckDimJpa)
                .toList();
        savedTruckJpa.syncDims(truckDims);
        List<HouseBlTruckOrderJpaEntity> truckOrders = truck.getTruckOrders().stream()
                .map(houseBlDocMapper::toTruckOrderJpa)
                .toList();
        savedTruckJpa.syncTruckOrders(truckOrders);
        HouseBlTruckDescJpaEntity savedDescJpa = saveOrDeleteTruckDesc(truck.getDesc(), savedTruckJpa);
        return jpaToDomainMapper.toTruckDomain(savedParent, savedTruckJpa, savedDescJpa);
    }

    @Override
    public HouseBl loadWithExt(HouseBlJpaEntity parent) {
        Long id = parent.getHouseBlId();
        HouseBlTruckJpaEntity truckJpa = houseBlTruckRepository.findByHouseBlHouseBlId(id).orElse(null);
        HouseBlTruckDescJpaEntity truckDescJpa = truckJpa != null
                ? houseBlTruckDescRepository.findByTruck_HouseBlTruckId(truckJpa.getHouseBlTruckId()).orElse(null)
                : null;
        return jpaToDomainMapper.toTruckDomain(parent, truckJpa, truckDescJpa);
    }

    @Override
    public void deleteExt(Long parentId) {
        houseBlTruckDimRepository.deleteByParentHouseBlId(parentId);
        houseBlTruckOrderRepository.deleteByParentHouseBlId(parentId);
        houseBlTruckDescRepository.deleteByParentHouseBlId(parentId);
        houseBlTruckRepository.deleteByHouseBl_HouseBlId(parentId);
    }

    /**
     * TRUCK desc 저장·삭제 처리. truckExt PK 확보 후 호출해야 한다.
     * 도메인 desc가 있으면 기존 row를 조회해 필드를 덮어쓰거나(UPDATE) 신규 insert한다.
     * 도메인 desc가 null이면 기존 row를 삭제하고 null을 반환한다(orphanRemoval 흉내).
     * saveExt 호출자가 재조회 없이 결과를 사용할 수 있도록 저장된 desc entity를 반환한다.
     */
    private HouseBlTruckDescJpaEntity saveOrDeleteTruckDesc(HouseBlDesc domainDesc, HouseBlTruckJpaEntity savedTruckJpa) {
        Long truckId = savedTruckJpa.getHouseBlTruckId();
        if (domainDesc == null) {
            houseBlTruckDescRepository.findByTruck_HouseBlTruckId(truckId)
                    .ifPresent(houseBlTruckDescRepository::delete);
            return null;
        }
        HouseBlTruckDescJpaEntity descJpa = houseBlTruckDescRepository.findByTruck_HouseBlTruckId(truckId)
                .orElseGet(HouseBlTruckDescJpaEntity::new);
        houseBlDocMapper.applyTruckDescFields(domainDesc, descJpa, savedTruckJpa);
        return houseBlTruckDescRepository.save(descJpa);
    }
}
