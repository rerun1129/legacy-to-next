package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.HouseBlSummary;
import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
import lombok.RequiredArgsConstructor;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlCargoMapper;
import com.freightos.fms.adapter.out.persistence.housebl.HouseBlDocMapper;
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
    private final HouseBlMapper houseBlMapper;
    private final HouseBlCargoMapper houseBlCargoMapper;
    private final HouseBlDocMapper houseBlDocMapper;

    @Override
    public Optional<HouseBl> findHouseBlById(Long id) {
        return houseBlRepository.findById(id).map(this::loadWithExt);
    }

    @Override
    public PagedResult<HouseBlSummary> findHouseBlsByJobDivAndBound(JobDiv jobDiv, Bound bound, PageRequest pageRequest) {
        return houseBlRepository.findSummariesByJobDivAndBound(jobDiv, bound, pageRequest);
    }

    @Override
    public PagedResult<HouseBl> findHouseBlsBySchedule(JobDiv jobDiv, Bound bound, String from, String to, PageRequest pageRequest) {
        Page<HouseBlJpaEntity> page = houseBlRepository.findBySchedule(jobDiv, bound, from, to,
                org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize(),
                        pageRequest.getSortBy() != null ? Sort.by(Sort.Direction.valueOf(pageRequest.getSortDirection().name()), pageRequest.getSortBy()) : Sort.unsorted()));
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
        houseBlMapper.applyCommonFields(domain, parentJpa);
        final HouseBlJpaEntity savedJpa = houseBlRepository.save(parentJpa);

        // extension 엔티티 save/update
        switch (domain) {
            case HouseBlSea sea -> {
                HouseBlSeaJpaEntity seaJpa = houseBlSeaRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlSeaJpaEntity::new);
                seaJpa.setHouseBl(savedJpa);
                houseBlMapper.applySeaFields(sea, seaJpa);
                // 컨테이너 동기화 (SEA 전용)
                List<HouseBlContainerJpaEntity> jpaContainers = sea.getContainers().stream().map(c -> houseBlCargoMapper.toContainerJpa(c, savedJpa)).toList();
                savedJpa.syncContainers(jpaContainers);
                List<HouseBlLicenseJpaEntity> jpaLicenses = sea.getLicenses().stream()
                        .map(l -> houseBlCargoMapper.toLicenseJpa(l, savedJpa))
                        .toList();
                savedJpa.syncLicenses(jpaLicenses);
                HouseBlDescJpaEntity seaDescJpa = (sea.getDesc() != null)
                        ? houseBlDocMapper.toDescJpa(sea.getDesc(), savedJpa)
                        : null;
                savedJpa.replaceDesc(seaDescJpa);
                houseBlSeaRepository.save(seaJpa);
            }
            case HouseBlAir air -> {
                HouseBlAirJpaEntity airJpa = houseBlAirRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlAirJpaEntity::new);
                airJpa.setHouseBl(savedJpa);
                houseBlMapper.applyAirFields(air, airJpa);
                List<HouseBlDimJpaEntity> airDims = air.getDims().stream()
                        .map(d -> houseBlCargoMapper.toDimJpa(d, savedJpa))
                        .toList();
                savedJpa.syncDims(airDims);
                List<HouseBlScheduleLegJpaEntity> jpaLegs = air.getScheduleLegs().stream()
                        .map(l -> houseBlDocMapper.toScheduleLegJpa(l, savedJpa))
                        .toList();
                savedJpa.syncScheduleLegs(jpaLegs);
                List<HouseBlLicenseJpaEntity> jpaLicenses = air.getLicenses().stream()
                        .map(l -> houseBlCargoMapper.toLicenseJpa(l, savedJpa))
                        .toList();
                savedJpa.syncLicenses(jpaLicenses);
                List<HouseBlAirChargeJpaEntity> airCharges = air.getAirCharges().stream()
                        .map(c -> houseBlDocMapper.toAirChargeJpa(c, savedJpa))
                        .toList();
                savedJpa.syncAirCharges(airCharges);
                HouseBlDescJpaEntity airDescJpa = (air.getDesc() != null)
                        ? houseBlDocMapper.toDescJpa(air.getDesc(), savedJpa)
                        : null;
                savedJpa.replaceDesc(airDescJpa);
                houseBlAirRepository.save(airJpa);
            }
            case HouseBlTruck truck -> {
                HouseBlTruckJpaEntity truckJpa = houseBlTruckRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlTruckJpaEntity::new);
                truckJpa.setHouseBl(savedJpa);
                houseBlMapper.applyTruckFields(truck, truckJpa);
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
                houseBlMapper.applyNonBlFields(nonBl, nonBlJpa);
                List<HouseBlContainerJpaEntity> jpaContainers = nonBl.getContainers().stream().map(c -> houseBlCargoMapper.toContainerJpa(c, savedJpa)).toList();
                savedJpa.syncContainers(jpaContainers);
                List<HouseBlDimJpaEntity> nonBlDims = nonBl.getDims().stream()
                        .map(d -> houseBlCargoMapper.toDimJpa(d, savedJpa))
                        .toList();
                savedJpa.syncDims(nonBlDims);
                HouseBlDescJpaEntity nonBlDescJpa = (nonBl.getDesc() != null)
                        ? houseBlDocMapper.toDescJpa(nonBl.getDesc(), savedJpa)
                        : null;
                savedJpa.replaceDesc(nonBlDescJpa);
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
        houseBlRepository.deleteById(id);
    }

    private PagedResult<HouseBl> toPagedResult(Page<HouseBlJpaEntity> page) {
        List<HouseBl> content = page.getContent().stream().map(this::loadWithExt).toList();
        return PagedResult.of(content, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    private HouseBl loadWithExt(HouseBlJpaEntity jpa) {
        Long id = jpa.getHouseBlId();
        return switch (jpa.getJobDiv()) {
            case SEA    -> houseBlMapper.toSeaDomain(jpa, houseBlSeaRepository.findByHouseBlHouseBlId(id).orElse(null));
            case AIR    -> houseBlMapper.toAirDomain(jpa, houseBlAirRepository.findByHouseBlHouseBlId(id).orElse(null));
            case TRUCK  -> houseBlMapper.toTruckDomain(jpa, houseBlTruckRepository.findByHouseBlHouseBlId(id).orElse(null));
            case NON_BL -> houseBlMapper.toNonBlDomain(jpa, houseBlNonBlRepository.findByHouseBlHouseBlId(id).orElse(null));
        };
    }
}
