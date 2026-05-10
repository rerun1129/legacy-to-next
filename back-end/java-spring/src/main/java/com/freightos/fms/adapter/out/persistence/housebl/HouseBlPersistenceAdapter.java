package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.adapter.out.persistence.nonbl.HouseBlNonBlRepository;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlContainerJpaEntity;
import com.freightos.fms.adapter.out.persistence.nonbl.entity.HouseBlNonBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.common.util.Nullables;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HouseBlPersistenceAdapter implements HouseBlPort {

    private final HouseBlRepository houseBlRepository;
    private final HouseBlSeaRepository houseBlSeaRepository;
    private final HouseBlAirRepository houseBlAirRepository;
    private final HouseBlTruckRepository houseBlTruckRepository;
    private final HouseBlNonBlRepository houseBlNonBlRepository;
    private final HouseBlSeaDescRepository houseBlSeaDescRepository;
    private final HouseBlAirDescRepository houseBlAirDescRepository;
    private final HouseBlTruckDescRepository houseBlTruckDescRepository;
    private final HouseBlJpaToDomainMapper jpaToDomainMapper;
    private final HouseBlDomainToJpaMapper domainToJpaMapper;
    private final HouseBlCargoMapper houseBlCargoMapper;
    private final HouseBlDocMapper houseBlDocMapper;

    @Override
    public Optional<HouseBl> findHouseBlById(Long id) {
        return houseBlRepository.findById(id).map(this::loadWithExt);
    }

    @Override
    public PagedResult<HouseBlSummary> searchHouseBls(HouseBlFilter filter, PageRequest pageRequest) {
        return houseBlRepository.searchSummaries(filter, pageRequest);
    }

    @Override
    public PagedResult<HouseBl> findHouseBlsBySchedule(JobDiv jobDiv, Bound bound, String from, String to, PageRequest pageRequest) {
        Page<HouseBlJpaEntity> page = houseBlRepository.findBySchedule(jobDiv, bound, from, to,
                org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize(),
                        Nullables.mapOrElse(pageRequest.getSortBy(), s -> Sort.by(Sort.Direction.valueOf(pageRequest.getSortDirection().name()), s), Sort::unsorted)));
        return toPagedResult(page);
    }

    @Override
    public long countHouseBlsByMasterBlId(Long masterBlId) {
        return houseBlRepository.countByMasterBlId(masterBlId);
    }

    @Override
    @Transactional
    public HouseBl saveHouseBl(HouseBl domain) {
        // 부모 엔티티 save/update
        HouseBlJpaEntity parentJpa;
        if (domain.getId() != null) {
            parentJpa = houseBlRepository.findById(domain.getId()).orElseThrow(() -> new ResourceNotFoundException("HouseBl", domain.getId()));
        } else {
            parentJpa = new HouseBlJpaEntity();
        }
        domainToJpaMapper.applyCommonFields(domain, parentJpa);
        final HouseBlJpaEntity savedJpa = houseBlRepository.save(parentJpa);

        // extension 엔티티 save/update
        switch (domain) {
            case HouseBlSea sea -> {
                HouseBlSeaJpaEntity seaJpa = houseBlSeaRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlSeaJpaEntity::new);
                seaJpa.setHouseBl(savedJpa);
                domainToJpaMapper.applySeaFields(sea, seaJpa);
                // seaExt를 먼저 영속화하여 house_bl_sea_id PK 확보 후 컨테이너·desc 저장
                HouseBlSeaJpaEntity savedSeaJpa = houseBlSeaRepository.save(seaJpa);
                // SEA 컨테이너 동기화 — seaJpa 소유 (house_bl_sea_container)
                List<HouseBlSeaContainerJpaEntity> seaContainers = sea.getContainers().stream()
                        .map(houseBlCargoMapper::toSeaContainerJpa)
                        .toList();
                savedSeaJpa.syncContainers(seaContainers);
                saveOrDeleteSeaDesc(sea.getDesc(), savedSeaJpa);
            }
            case HouseBlAir air -> {
                HouseBlAirJpaEntity airJpa = houseBlAirRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlAirJpaEntity::new);
                airJpa.setHouseBl(savedJpa);
                domainToJpaMapper.applyAirFields(air, airJpa);
                List<HouseBlDimJpaEntity> airDims = air.getDims().stream()
                        .map(d -> houseBlCargoMapper.toDimJpa(d, savedJpa))
                        .toList();
                savedJpa.syncDims(airDims);
                // airJpa를 먼저 영속화하여 house_bl_air_id PK 확보 후 scheduleLegs/airCharges 동기화
                HouseBlAirJpaEntity savedAirJpa = houseBlAirRepository.save(airJpa);
                List<HouseBlScheduleLegJpaEntity> jpaLegs = air.getScheduleLegs().stream()
                        .map(houseBlDocMapper::toScheduleLegJpa)
                        .toList();
                savedAirJpa.syncScheduleLegs(jpaLegs);
                List<HouseBlAirChargeJpaEntity> airCharges = air.getAirCharges().stream()
                        .map(houseBlDocMapper::toAirChargeJpa)
                        .toList();
                savedAirJpa.syncAirCharges(airCharges);
                saveOrDeleteAirDesc(air.getDesc(), savedAirJpa);
            }
            case HouseBlTruck truck -> {
                HouseBlTruckJpaEntity truckJpa = houseBlTruckRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlTruckJpaEntity::new);
                truckJpa.setHouseBl(savedJpa);
                domainToJpaMapper.applyTruckFields(truck, truckJpa);
                List<HouseBlDimJpaEntity> truckDims = truck.getDims().stream()
                        .map(d -> houseBlCargoMapper.toDimJpa(d, savedJpa))
                        .toList();
                savedJpa.syncDims(truckDims);
                // truckJpa를 먼저 영속화하여 house_bl_truck_id PK 확보 후 truckOrders 동기화
                HouseBlTruckJpaEntity savedTruckJpa = houseBlTruckRepository.save(truckJpa);
                List<HouseBlTruckOrderJpaEntity> truckOrders = truck.getTruckOrders().stream()
                        .map(houseBlDocMapper::toTruckOrderJpa)
                        .toList();
                savedTruckJpa.syncTruckOrders(truckOrders);
                saveOrDeleteTruckDesc(truck.getDesc(), savedTruckJpa);
            }
            case HouseBlNonBl nonBl -> {
                HouseBlNonBlJpaEntity nonBlJpa = houseBlNonBlRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlNonBlJpaEntity::new);
                nonBlJpa.setHouseBl(savedJpa);
                domainToJpaMapper.applyNonBlFields(nonBl, nonBlJpa);
                // NON_BL 컨테이너 merge — nonBlJpa 소유 (house_bl_nonbl_container)
                List<HouseBlNonBlContainerJpaEntity> nonBlContainers = nonBl.getContainers().stream()
                        .map(houseBlCargoMapper::toNonBlContainerJpa)
                        .toList();
                nonBlJpa.mergeContainers(nonBlContainers);
                List<HouseBlDimJpaEntity> nonBlDims = nonBl.getDims().stream()
                        .map(d -> houseBlCargoMapper.toDimJpa(d, savedJpa))
                        .toList();
                savedJpa.mergeDims(nonBlDims);
                // NON_BL은 desc를 사용하지 않음 — remark는 house_bl_non_bl 컬럼으로 저장됨
                houseBlNonBlRepository.save(nonBlJpa);
                // in-memory 도메인에 JPA save 결과(parent id, 감사 필드, 자식 PK)를 역방향 sync.
                // loadWithExt 제거로 응답용 SELECT 제거 — 직렬 저장 순서와 동일하므로 인덱스 매핑 안전.
                nonBl.assignIdentity(savedJpa.getHouseBlId(), savedJpa.getCreatedAt(), savedJpa.getUpdatedAt(),
                        savedJpa.getCreatedBy(), savedJpa.getUpdatedBy());
                syncChildIds(nonBl.getContainers(), nonBlJpa.getContainers());
                syncDimIds(nonBl.getDims(), savedJpa.getDims());
                return nonBl;
            }
            default -> throw new IllegalArgumentException("Unsupported HouseBl type: " + domain.getClass().getSimpleName());
        }

        return loadWithExt(savedJpa);
    }

    @Override
    @Transactional
    public void deleteHouseBl(HouseBl houseBl) {
        Long id = houseBl.getId();
        switch (houseBl) {
            case HouseBlSea ignored -> {
                // house_bl_sea_desc와 house_bl_sea_container는 house_bl_sea_id FK ON DELETE CASCADE — seaExt 삭제 시 DB 자동 정리
                houseBlSeaRepository.deleteByHouseBl_HouseBlId(id);
            }
            case HouseBlAir ignored -> {
                // house_bl_air_desc는 house_bl_air_id FK ON DELETE CASCADE — airExt 삭제 시 DB 자동 정리
                houseBlAirRepository.deleteByHouseBl_HouseBlId(id);
            }
            case HouseBlTruck ignored -> {
                // house_bl_truck_desc는 house_bl_truck_id FK ON DELETE CASCADE — truckExt 삭제 시 DB 자동 정리
                houseBlTruckRepository.deleteByHouseBl_HouseBlId(id);
            }
            case HouseBlNonBl ignored -> {
                // house_bl_nonbl_container는 house_bl_non_bl_id FK ON DELETE CASCADE — nonBlExt 삭제 시 DB 자동 정리
                houseBlNonBlRepository.deleteByHouseBl_HouseBlId(id);
            }
            default -> throw new IllegalArgumentException("Unsupported HouseBl type: " + houseBl.getClass().getSimpleName());
        }
        houseBlRepository.deleteById(id);
    }

    @Override
    @Transactional
    public long updateHblNoById(Long id, BlNumber newHblNo, JobDiv expectedJobDiv) {
        return houseBlRepository.updateHblNoById(id, newHblNo.value(), expectedJobDiv);
    }

    private PagedResult<HouseBl> toPagedResult(Page<HouseBlJpaEntity> page) {
        List<HouseBl> content = page.getContent().stream().map(this::loadWithExt).toList();
        return PagedResult.of(content, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    @Override
    public List<ConsoledHouseBlSeaSummary> findConsoledSeaSummariesByMasterBlId(Long masterBlId) {
        return houseBlRepository.findConsoledSeaSummariesByMasterBlId(masterBlId);
    }

    @Override
    public List<ConsoledHouseBlAirSummary> findConsoledAirSummariesByMasterBlId(Long masterBlId) {
        return houseBlRepository.findConsoledAirSummariesByMasterBlId(masterBlId);
    }

    private HouseBl loadWithExt(HouseBlJpaEntity jpa) {
        Long id = jpa.getHouseBlId();
        return switch (jpa.getJobDiv()) {
            // SEA/AIR/TRUCK은 ext id 기준으로 desc 조회 — NON_BL은 desc 미사용이므로 조회 생략
            case SEA -> {
                HouseBlSeaJpaEntity seaJpa = houseBlSeaRepository.findByHouseBlHouseBlId(id).orElse(null);
                HouseBlSeaDescJpaEntity seaDescJpa = seaJpa != null
                        ? houseBlSeaDescRepository.findBySea_HouseBlSeaId(seaJpa.getHouseBlSeaId()).orElse(null)
                        : null;
                yield jpaToDomainMapper.toSeaDomain(jpa, seaJpa, seaDescJpa);
            }
            case AIR -> {
                HouseBlAirJpaEntity airJpa = houseBlAirRepository.findByHouseBlHouseBlId(id).orElse(null);
                HouseBlAirDescJpaEntity airDescJpa = airJpa != null
                        ? houseBlAirDescRepository.findByAir_HouseBlAirId(airJpa.getHouseBlAirId()).orElse(null)
                        : null;
                yield jpaToDomainMapper.toAirDomain(jpa, airJpa, airDescJpa);
            }
            case TRUCK -> {
                HouseBlTruckJpaEntity truckJpa = houseBlTruckRepository.findByHouseBlHouseBlId(id).orElse(null);
                HouseBlTruckDescJpaEntity truckDescJpa = truckJpa != null
                        ? houseBlTruckDescRepository.findByTruck_HouseBlTruckId(truckJpa.getHouseBlTruckId()).orElse(null)
                        : null;
                yield jpaToDomainMapper.toTruckDomain(jpa, truckJpa, truckDescJpa);
            }
            case NON_BL -> jpaToDomainMapper.toNonBlDomain(jpa, houseBlNonBlRepository.findByHouseBlHouseBlId(id).orElse(null));
        };
    }

    /**
     * SEA desc 저장·삭제 처리. seaExt PK 확보 후 호출해야 한다.
     * 도메인 desc가 있으면 기존 row를 조회해 필드를 덮어쓰거나(UPDATE) 신규 insert한다.
     * 도메인 desc가 null이면 기존 row를 삭제한다(orphanRemoval 흉내).
     */
    private void saveOrDeleteSeaDesc(HouseBlDesc domainDesc, HouseBlSeaJpaEntity savedSeaJpa) {
        Long seaId = savedSeaJpa.getHouseBlSeaId();
        if (domainDesc == null) {
            houseBlSeaDescRepository.findBySea_HouseBlSeaId(seaId)
                    .ifPresent(houseBlSeaDescRepository::delete);
            return;
        }
        HouseBlSeaDescJpaEntity descJpa = houseBlSeaDescRepository.findBySea_HouseBlSeaId(seaId)
                .orElseGet(HouseBlSeaDescJpaEntity::new);
        houseBlDocMapper.applySeaDescFields(domainDesc, descJpa, savedSeaJpa);
        houseBlSeaDescRepository.save(descJpa);
    }

    /**
     * AIR desc 저장·삭제 처리. airExt PK 확보 후 호출해야 한다.
     * 도메인 desc가 있으면 기존 row를 조회해 필드를 덮어쓰거나(UPDATE) 신규 insert한다.
     * 도메인 desc가 null이면 기존 row를 삭제한다(orphanRemoval 흉내).
     */
    private void saveOrDeleteAirDesc(HouseBlDesc domainDesc, HouseBlAirJpaEntity savedAirJpa) {
        Long airId = savedAirJpa.getHouseBlAirId();
        if (domainDesc == null) {
            houseBlAirDescRepository.findByAir_HouseBlAirId(airId)
                    .ifPresent(houseBlAirDescRepository::delete);
            return;
        }
        HouseBlAirDescJpaEntity descJpa = houseBlAirDescRepository.findByAir_HouseBlAirId(airId)
                .orElseGet(HouseBlAirDescJpaEntity::new);
        houseBlDocMapper.applyAirDescFields(domainDesc, descJpa, savedAirJpa);
        houseBlAirDescRepository.save(descJpa);
    }

    /**
     * TRUCK desc 저장·삭제 처리. truckExt PK 확보 후 호출해야 한다.
     * 도메인 desc가 있으면 기존 row를 조회해 필드를 덮어쓰거나(UPDATE) 신규 insert한다.
     * 도메인 desc가 null이면 기존 row를 삭제한다(orphanRemoval 흉내).
     */
    private void saveOrDeleteTruckDesc(HouseBlDesc domainDesc, HouseBlTruckJpaEntity savedTruckJpa) {
        Long truckId = savedTruckJpa.getHouseBlTruckId();
        if (domainDesc == null) {
            houseBlTruckDescRepository.findByTruck_HouseBlTruckId(truckId)
                    .ifPresent(houseBlTruckDescRepository::delete);
            return;
        }
        HouseBlTruckDescJpaEntity descJpa = houseBlTruckDescRepository.findByTruck_HouseBlTruckId(truckId)
                .orElseGet(HouseBlTruckDescJpaEntity::new);
        houseBlDocMapper.applyTruckDescFields(domainDesc, descJpa, savedTruckJpa);
        houseBlTruckDescRepository.save(descJpa);
    }

    /**
     * NON_BL 컨테이너 도메인 자식에 JPA save 후 생성된 PK를 역방향 sync.
     * mergeContainers의 결과 순서(incoming 순) = 도메인 자식 순서이므로 인덱스 1:1 매핑이 안전하다.
     * 기존에 id가 있던 자식은 동일 id가 유지되므로 중복 할당 무해.
     */
    private void syncChildIds(List<HouseBlContainer> domainContainers, List<HouseBlNonBlContainerJpaEntity> jpaContainers) {
        int size = Math.min(domainContainers.size(), jpaContainers.size());
        for (int i = 0; i < size; i++) {
            HouseBlNonBlContainerJpaEntity jpa = jpaContainers.get(i);
            HouseBlContainer domainContainer = domainContainers.get(i);
            if (domainContainer.getId() == null && jpa.getHouseBlNonBlContainerId() != null) {
                domainContainer.assignIdentity(jpa.getHouseBlNonBlContainerId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                        jpa.getCreatedBy(), jpa.getUpdatedBy());
            }
        }
    }

    /**
     * NON_BL DIM 도메인 자식에 JPA save 후 생성된 PK를 역방향 sync.
     * mergeDims의 결과 순서(incoming 순) = 도메인 자식 순서이므로 인덱스 1:1 매핑이 안전하다.
     */
    private void syncDimIds(List<HouseBlDim> domainDims, List<HouseBlDimJpaEntity> jpaDims) {
        int size = Math.min(domainDims.size(), jpaDims.size());
        for (int i = 0; i < size; i++) {
            HouseBlDimJpaEntity jpa = jpaDims.get(i);
            HouseBlDim domainDim = domainDims.get(i);
            if (domainDim.getId() == null && jpa.getHouseBlDimId() != null) {
                domainDim.assignIdentity(jpa.getHouseBlDimId(), jpa.getCreatedAt(), jpa.getUpdatedAt(),
                        jpa.getCreatedBy(), jpa.getUpdatedBy());
            }
        }
    }
}
