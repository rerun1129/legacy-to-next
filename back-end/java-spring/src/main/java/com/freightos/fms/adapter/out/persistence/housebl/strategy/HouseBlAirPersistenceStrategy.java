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
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlScheduleLegJpaEntity;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AIR jobDiv 전용 영속성 전략.
 * house_bl_air + house_bl_air_dim + house_bl_schedule_leg + house_bl_air_charge + house_bl_air_desc 처리 담당.
 */
@Component
@RequiredArgsConstructor
public class HouseBlAirPersistenceStrategy implements HouseBlPersistenceStrategy<HouseBlAir> {

    private final HouseBlAirRepository houseBlAirRepository;
    private final HouseBlAirDescRepository houseBlAirDescRepository;
    private final HouseBlAirDimRepository houseBlAirDimRepository;
    private final HouseBlScheduleLegRepository houseBlScheduleLegRepository;
    private final HouseBlAirChargeRepository houseBlAirChargeRepository;
    private final HouseBlJpaToDomainMapper jpaToDomainMapper;
    private final HouseBlDomainToJpaMapper domainToJpaMapper;
    private final HouseBlCargoMapper houseBlCargoMapper;
    private final HouseBlDocMapper houseBlDocMapper;

    @Override
    public JobDiv jobDiv() {
        return JobDiv.AIR;
    }

    @Override
    public HouseBlAir saveExt(HouseBlAir air, HouseBlJpaEntity savedParent) {
        HouseBlAirJpaEntity airJpa = houseBlAirRepository.findByHouseBlHouseBlId(savedParent.getHouseBlId())
                .orElseGet(HouseBlAirJpaEntity::new);
        airJpa.setHouseBl(savedParent);
        domainToJpaMapper.applyAirFields(air, airJpa);
        // airJpa를 먼저 영속화하여 house_bl_air_id PK 확보 후 dims/scheduleLegs/airCharges 동기화
        HouseBlAirJpaEntity savedAirJpa = houseBlAirRepository.save(airJpa);
        List<HouseBlAirDimJpaEntity> airDims = air.getDims().stream()
                .map(houseBlCargoMapper::toAirDimJpa)
                .toList();
        savedAirJpa.syncDims(airDims);
        List<HouseBlScheduleLegJpaEntity> jpaLegs = air.getScheduleLegs().stream()
                .map(houseBlDocMapper::toScheduleLegJpa)
                .toList();
        savedAirJpa.syncScheduleLegs(jpaLegs);
        List<HouseBlAirChargeJpaEntity> airCharges = air.getAirCharges().stream()
                .map(houseBlDocMapper::toAirChargeJpa)
                .toList();
        savedAirJpa.syncAirCharges(airCharges);
        HouseBlAirDescJpaEntity savedDescJpa = saveOrDeleteAirDesc(air.getDesc(), savedAirJpa);
        return jpaToDomainMapper.toAirDomain(savedParent, savedAirJpa, savedDescJpa);
    }

    @Override
    public HouseBl loadWithExt(HouseBlJpaEntity parent) {
        Long id = parent.getHouseBlId();
        HouseBlAirJpaEntity airJpa = houseBlAirRepository.findByHouseBlHouseBlId(id).orElse(null);
        HouseBlAirDescJpaEntity airDescJpa = airJpa != null
                ? houseBlAirDescRepository.findByAir_HouseBlAirId(airJpa.getHouseBlAirId()).orElse(null)
                : null;
        return jpaToDomainMapper.toAirDomain(parent, airJpa, airDescJpa);
    }

    @Override
    public void deleteExt(Long parentId) {
        houseBlAirDimRepository.deleteByParentHouseBlId(parentId);
        houseBlScheduleLegRepository.deleteByParentHouseBlId(parentId);
        houseBlAirChargeRepository.deleteByParentHouseBlId(parentId);
        houseBlAirDescRepository.deleteByParentHouseBlId(parentId);
        houseBlAirRepository.deleteByHouseBl_HouseBlId(parentId);
    }

    /**
     * AIR desc 저장·삭제 처리. airExt PK 확보 후 호출해야 한다.
     * 도메인 desc가 있으면 기존 row를 조회해 필드를 덮어쓰거나(UPDATE) 신규 insert한다.
     * 도메인 desc가 null이면 기존 row를 삭제하고 null을 반환한다(orphanRemoval 흉내).
     * saveExt 호출자가 재조회 없이 결과를 사용할 수 있도록 저장된 desc entity를 반환한다.
     */
    private HouseBlAirDescJpaEntity saveOrDeleteAirDesc(HouseBlDesc domainDesc, HouseBlAirJpaEntity savedAirJpa) {
        Long airId = savedAirJpa.getHouseBlAirId();
        if (domainDesc == null) {
            houseBlAirDescRepository.findByAir_HouseBlAirId(airId)
                    .ifPresent(houseBlAirDescRepository::delete);
            return null;
        }
        HouseBlAirDescJpaEntity descJpa = houseBlAirDescRepository.findByAir_HouseBlAirId(airId)
                .orElseGet(HouseBlAirDescJpaEntity::new);
        houseBlDocMapper.applyAirDescFields(domainDesc, descJpa, savedAirJpa);
        return houseBlAirDescRepository.save(descJpa);
    }
}
