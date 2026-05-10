package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.adapter.out.persistence.nonbl.HouseBlNonBlRepository;
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
    private final HouseBlDescRepository houseBlDescRepository;
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
                // 컨테이너 동기화 (SEA 전용)
                List<HouseBlContainerJpaEntity> jpaContainers = sea.getContainers().stream().map(c -> houseBlCargoMapper.toContainerJpa(c, savedJpa)).toList();
                savedJpa.syncContainers(jpaContainers);
                saveOrDeleteDesc(sea.getDesc(), savedJpa);
                houseBlSeaRepository.save(seaJpa);
            }
            case HouseBlAir air -> {
                HouseBlAirJpaEntity airJpa = houseBlAirRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlAirJpaEntity::new);
                airJpa.setHouseBl(savedJpa);
                domainToJpaMapper.applyAirFields(air, airJpa);
                List<HouseBlDimJpaEntity> airDims = air.getDims().stream()
                        .map(d -> houseBlCargoMapper.toDimJpa(d, savedJpa))
                        .toList();
                savedJpa.syncDims(airDims);
                List<HouseBlScheduleLegJpaEntity> jpaLegs = air.getScheduleLegs().stream()
                        .map(l -> houseBlDocMapper.toScheduleLegJpa(l, savedJpa))
                        .toList();
                savedJpa.syncScheduleLegs(jpaLegs);
                List<HouseBlAirChargeJpaEntity> airCharges = air.getAirCharges().stream()
                        .map(c -> houseBlDocMapper.toAirChargeJpa(c, savedJpa))
                        .toList();
                savedJpa.syncAirCharges(airCharges);
                saveOrDeleteDesc(air.getDesc(), savedJpa);
                houseBlAirRepository.save(airJpa);
            }
            case HouseBlTruck truck -> {
                HouseBlTruckJpaEntity truckJpa = houseBlTruckRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlTruckJpaEntity::new);
                truckJpa.setHouseBl(savedJpa);
                domainToJpaMapper.applyTruckFields(truck, truckJpa);
                List<HouseBlDimJpaEntity> truckDims = truck.getDims().stream()
                        .map(d -> houseBlCargoMapper.toDimJpa(d, savedJpa))
                        .toList();
                savedJpa.syncDims(truckDims);
                List<HouseBlTruckOrderJpaEntity> truckOrders = truck.getTruckOrders().stream()
                        .map(o -> houseBlDocMapper.toTruckOrderJpa(o, savedJpa))
                        .toList();
                savedJpa.syncTruckOrders(truckOrders);
                houseBlTruckRepository.save(truckJpa);
            }
            case HouseBlNonBl nonBl -> {
                HouseBlNonBlJpaEntity nonBlJpa = houseBlNonBlRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlNonBlJpaEntity::new);
                nonBlJpa.setHouseBl(savedJpa);
                domainToJpaMapper.applyNonBlFields(nonBl, nonBlJpa);
                List<HouseBlContainerJpaEntity> jpaContainers = nonBl.getContainers().stream().map(c -> houseBlCargoMapper.toContainerJpa(c, savedJpa)).toList();
                savedJpa.mergeContainers(jpaContainers);
                List<HouseBlDimJpaEntity> nonBlDims = nonBl.getDims().stream()
                        .map(d -> houseBlCargoMapper.toDimJpa(d, savedJpa))
                        .toList();
                savedJpa.mergeDims(nonBlDims);
                // NON_BL은 desc를 사용하지 않음 — remark는 house_bl_non_bl 컬럼으로 저장됨
                houseBlNonBlRepository.save(nonBlJpa);
            }
            default -> throw new IllegalArgumentException("Unsupported HouseBl type: " + domain.getClass().getSimpleName());
        }

        return loadWithExt(savedJpa);
    }

    @Override
    @Transactional
    public void deleteHouseBl(HouseBl houseBl) {
        Long id = houseBl.getId();
        houseBlSeaRepository.findByHouseBlHouseBlId(id).ifPresent(houseBlSeaRepository::delete);
        houseBlAirRepository.findByHouseBlHouseBlId(id).ifPresent(houseBlAirRepository::delete);
        houseBlTruckRepository.findByHouseBlHouseBlId(id).ifPresent(houseBlTruckRepository::delete);
        houseBlNonBlRepository.findByHouseBlHouseBlId(id).ifPresent(houseBlNonBlRepository::delete);
        // house_bl_desc FK에 ON DELETE CASCADE 없으므로 명시 삭제
        houseBlDescRepository.deleteByHouseBl_HouseBlId(id);
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
            // SEA/AIR는 desc 별도 SELECT 1회 — NON_BL/TRUCK은 desc 미사용이므로 조회 생략
            case SEA    -> jpaToDomainMapper.toSeaDomain(jpa,
                    houseBlSeaRepository.findByHouseBlHouseBlId(id).orElse(null),
                    houseBlDescRepository.findByHouseBl_HouseBlId(id).orElse(null));
            case AIR    -> jpaToDomainMapper.toAirDomain(jpa,
                    houseBlAirRepository.findByHouseBlHouseBlId(id).orElse(null),
                    houseBlDescRepository.findByHouseBl_HouseBlId(id).orElse(null));
            case TRUCK  -> jpaToDomainMapper.toTruckDomain(jpa, houseBlTruckRepository.findByHouseBlHouseBlId(id).orElse(null));
            case NON_BL -> jpaToDomainMapper.toNonBlDomain(jpa, houseBlNonBlRepository.findByHouseBlHouseBlId(id).orElse(null));
        };
    }

    /**
     * SEA/AIR desc 저장·삭제 처리.
     * 도메인 desc가 있으면 기존 row를 조회해 필드를 덮어쓰거나(UPDATE) 신규 insert한다.
     * 도메인 desc가 null이면 기존 row를 삭제한다(orphanRemoval 흉내).
     */
    private void saveOrDeleteDesc(HouseBlDesc domainDesc, HouseBlJpaEntity parentJpa) {
        Long parentId = parentJpa.getHouseBlId();
        if (domainDesc == null) {
            houseBlDescRepository.findByHouseBl_HouseBlId(parentId)
                    .ifPresent(houseBlDescRepository::delete);
            return;
        }
        HouseBlDescJpaEntity descJpa = houseBlDescRepository.findByHouseBl_HouseBlId(parentId)
                .orElseGet(HouseBlDescJpaEntity::new);
        houseBlDocMapper.applyDescFields(domainDesc, descJpa, parentJpa);
        houseBlDescRepository.save(descJpa);
    }
}
