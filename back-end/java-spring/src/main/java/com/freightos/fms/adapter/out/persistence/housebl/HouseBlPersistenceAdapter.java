package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.*;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
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
    private final HouseBlMapper houseBlMapper;

    @Override
    public Optional<HouseBl> findHouseBlById(Long id) {
        return houseBlRepository.findById(id).map(houseBlMapper::toDomain);
    }

    @Override
    public PagedResult<HouseBl> findHouseBlsByJobDivAndBound(JobDiv jobDiv, Bound bound, PageRequest pageRequest) {
        Page<HouseBlJpaEntity> page = houseBlRepository.findAllByJobDivAndBoundOrderByCreatedAtDesc(jobDiv, bound,
                org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize(),
                        pageRequest.getSortBy() != null ? Sort.by(Sort.Direction.valueOf(pageRequest.getSortDirection().name()), pageRequest.getSortBy()) : Sort.unsorted()));
        return toPagedResult(page);
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
                List<HouseBlContainerJpaEntity> jpaContainers = sea.getContainers().stream().map(c -> houseBlMapper.toContainerJpa(c, savedJpa)).toList();
                savedJpa.syncContainers(jpaContainers);
                houseBlSeaRepository.save(seaJpa);
            }
            case HouseBlAir air -> {
                HouseBlAirJpaEntity airJpa = houseBlAirRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlAirJpaEntity::new);
                airJpa.setHouseBl(savedJpa);
                houseBlMapper.applyAirFields(air, airJpa);
                houseBlAirRepository.save(airJpa);
            }
            case HouseBlTruck truck -> {
                HouseBlTruckJpaEntity truckJpa = houseBlTruckRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlTruckJpaEntity::new);
                truckJpa.setHouseBl(savedJpa);
                houseBlMapper.applyTruckFields(truck, truckJpa);
                houseBlTruckRepository.save(truckJpa);
            }
            case HouseBlNonBl nonBl -> {
                HouseBlNonBlJpaEntity nonBlJpa = houseBlNonBlRepository.findByHouseBlHouseBlId(savedJpa.getHouseBlId()).orElseGet(HouseBlNonBlJpaEntity::new);
                nonBlJpa.setHouseBl(savedJpa);
                houseBlMapper.applyNonBlFields(nonBl, nonBlJpa);
                houseBlNonBlRepository.save(nonBlJpa);
            }
            default -> throw new IllegalArgumentException("Unsupported HouseBl type: " + domain.getClass().getSimpleName());
        }

        // reload (extension lazy 포함)
        HouseBlJpaEntity reloaded = houseBlRepository.findById(savedJpa.getHouseBlId()).orElseThrow(() -> new ResourceNotFoundException("HouseBl", savedJpa.getHouseBlId()));
        return houseBlMapper.toDomain(reloaded);
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
        List<HouseBl> content = page.getContent().stream().map(houseBlMapper::toDomain).toList();
        return PagedResult.of(content, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }
}
