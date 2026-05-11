package com.freightos.fms.adapter.out.persistence.housebl.strategy;

import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDocMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDomainToJpaMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlJpaToDomainMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlSeaContainerRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlSeaDescRepository;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlSeaRepository;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlSeaJpaEntity;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.entity.HouseBlDesc;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * SEA jobDiv 전용 영속성 전략.
 * house_bl_sea + house_bl_sea_container + house_bl_sea_desc 처리 담당.
 */
@Component
@RequiredArgsConstructor
public class HouseBlSeaPersistenceStrategy implements HouseBlPersistenceStrategy<HouseBlSea> {

    private final HouseBlSeaRepository houseBlSeaRepository;
    private final HouseBlSeaDescRepository houseBlSeaDescRepository;
    private final HouseBlSeaContainerRepository houseBlSeaContainerRepository;
    private final HouseBlJpaToDomainMapper jpaToDomainMapper;
    private final HouseBlDomainToJpaMapper domainToJpaMapper;
    private final HouseBlCargoMapper houseBlCargoMapper;
    private final HouseBlDocMapper houseBlDocMapper;

    @Override
    public JobDiv jobDiv() {
        return JobDiv.SEA;
    }

    @Override
    public HouseBlSea saveExt(HouseBlSea sea, HouseBlJpaEntity savedParent) {
        HouseBlSeaJpaEntity seaJpa = houseBlSeaRepository.findByHouseBlHouseBlId(savedParent.getHouseBlId())
                .orElseGet(HouseBlSeaJpaEntity::new);
        seaJpa.setHouseBl(savedParent);
        domainToJpaMapper.applySeaFields(sea, seaJpa);
        // seaExt를 먼저 영속화하여 house_bl_sea_id PK 확보 후 컨테이너·desc 저장
        HouseBlSeaJpaEntity savedSeaJpa = houseBlSeaRepository.save(seaJpa);
        // SEA 컨테이너 동기화 — seaJpa 소유 (house_bl_sea_container)
        List<HouseBlSeaContainerJpaEntity> seaContainers = sea.getContainers().stream()
                .map(houseBlCargoMapper::toSeaContainerJpa)
                .toList();
        savedSeaJpa.syncContainers(seaContainers);
        HouseBlSeaDescJpaEntity savedDescJpa = saveOrDeleteSeaDesc(sea.getDesc(), savedSeaJpa);
        return jpaToDomainMapper.toSeaDomain(savedParent, savedSeaJpa, savedDescJpa);
    }

    @Override
    public HouseBl loadWithExt(HouseBlJpaEntity parent) {
        Long id = parent.getHouseBlId();
        HouseBlSeaJpaEntity seaJpa = houseBlSeaRepository.findByHouseBlHouseBlId(id).orElse(null);
        HouseBlSeaDescJpaEntity seaDescJpa = seaJpa != null
                ? houseBlSeaDescRepository.findBySea_HouseBlSeaId(seaJpa.getHouseBlSeaId()).orElse(null)
                : null;
        return jpaToDomainMapper.toSeaDomain(parent, seaJpa, seaDescJpa);
    }

    @Override
    public void deleteExt(Long parentId) {
        houseBlSeaContainerRepository.deleteByParentHouseBlId(parentId);
        houseBlSeaDescRepository.deleteByParentHouseBlId(parentId);
        houseBlSeaRepository.deleteByHouseBl_HouseBlId(parentId);
    }

    /**
     * SEA desc 저장·삭제 처리. seaExt PK 확보 후 호출해야 한다.
     * 도메인 desc가 있으면 기존 row를 조회해 필드를 덮어쓰거나(UPDATE) 신규 insert한다.
     * 도메인 desc가 null이면 기존 row를 삭제하고 null을 반환한다(orphanRemoval 흉내).
     * saveExt 호출자가 재조회 없이 결과를 사용할 수 있도록 저장된 desc entity를 반환한다.
     */
    private HouseBlSeaDescJpaEntity saveOrDeleteSeaDesc(HouseBlDesc domainDesc, HouseBlSeaJpaEntity savedSeaJpa) {
        Long seaId = savedSeaJpa.getHouseBlSeaId();
        if (domainDesc == null) {
            houseBlSeaDescRepository.findBySea_HouseBlSeaId(seaId)
                    .ifPresent(houseBlSeaDescRepository::delete);
            return null;
        }
        HouseBlSeaDescJpaEntity descJpa = houseBlSeaDescRepository.findBySea_HouseBlSeaId(seaId)
                .orElseGet(HouseBlSeaDescJpaEntity::new);
        houseBlDocMapper.applySeaDescFields(domainDesc, descJpa, savedSeaJpa);
        return houseBlSeaDescRepository.save(descJpa);
    }
}
