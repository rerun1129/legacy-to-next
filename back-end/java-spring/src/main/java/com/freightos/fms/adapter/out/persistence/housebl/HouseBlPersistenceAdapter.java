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
    public Optional<HouseBl> findById(Long id) {
        return houseBlRepository.findById(id)
                .map(houseBlMapper::toDomain);
    }

    @Override
    public PagedResult<HouseBl> findAllByJobDivAndBoundOrderByCreatedAtDesc(
            JobDiv jobDiv, Bound bound, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPage =
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage(), pageRequest.getSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<HouseBlJpaEntity> page = houseBlRepository
                .findAllByJobDivAndBoundOrderByCreatedAtDesc(jobDiv, bound, springPage);
        return toPagedResult(page);
    }

    @Override
    public PagedResult<HouseBl> findBySchedule(JobDiv jobDiv, Bound bound, String from, String to,
                                                PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPage =
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage(), pageRequest.getSize(),
                        Sort.by(Sort.Direction.ASC, "etd"));
        Page<HouseBlJpaEntity> page = houseBlRepository.findBySchedule(jobDiv, bound, from, to, springPage);
        return toPagedResult(page);
    }

    @Override
    public long countByMasterBlId(Long masterBlId) {
        return houseBlRepository.countByMasterBlId(masterBlId);
    }

    @Override
    @Transactional
    public HouseBl save(HouseBl domain) {
        // 부모 엔티티 save/update
        HouseBlJpaEntity parentJpa;
        if (domain.getId() != null) {
            parentJpa = houseBlRepository.findById(domain.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("HouseBl", domain.getId()));
        } else {
            parentJpa = new HouseBlJpaEntity();
        }
        houseBlMapper.applyCommonFields(domain, parentJpa);
        final HouseBlJpaEntity savedJpa = houseBlRepository.save(parentJpa);

        // extension 엔티티 save/update
        if (domain instanceof HouseBlSea sea) {
            HouseBlSeaJpaEntity seaJpa = houseBlSeaRepository
                    .findByHouseBlHouseBlId(savedJpa.getHouseBlId())
                    .orElseGet(HouseBlSeaJpaEntity::new);
            seaJpa.setHouseBl(savedJpa);
            houseBlMapper.applySeaFields(sea, seaJpa);
            // 컨테이너 동기화 (SEA 전용)
            List<HouseBlContainerJpaEntity> jpaContainers = sea.getContainers().stream()
                    .map(c -> houseBlMapper.toContainerJpa(c, savedJpa))
                    .toList();
            savedJpa.syncContainers(jpaContainers);
            houseBlSeaRepository.save(seaJpa);
        } else if (domain instanceof HouseBlAir air) {
            HouseBlAirJpaEntity airJpa = houseBlAirRepository
                    .findByHouseBlHouseBlId(savedJpa.getHouseBlId())
                    .orElseGet(HouseBlAirJpaEntity::new);
            airJpa.setHouseBl(savedJpa);
            houseBlMapper.applyAirFields(air, airJpa);
            houseBlAirRepository.save(airJpa);
        } else if (domain instanceof HouseBlTruck truck) {
            HouseBlTruckJpaEntity truckJpa = houseBlTruckRepository
                    .findByHouseBlHouseBlId(savedJpa.getHouseBlId())
                    .orElseGet(HouseBlTruckJpaEntity::new);
            truckJpa.setHouseBl(savedJpa);
            houseBlMapper.applyTruckFields(truck, truckJpa);
            houseBlTruckRepository.save(truckJpa);
        } else if (domain instanceof HouseBlNonBl nonBl) {
            HouseBlNonBlJpaEntity nonBlJpa = houseBlNonBlRepository
                    .findByHouseBlHouseBlId(savedJpa.getHouseBlId())
                    .orElseGet(HouseBlNonBlJpaEntity::new);
            nonBlJpa.setHouseBl(savedJpa);
            houseBlMapper.applyNonBlFields(nonBl, nonBlJpa);
            houseBlNonBlRepository.save(nonBlJpa);
        }

        // reload (extension lazy 포함)
        HouseBlJpaEntity reloaded = houseBlRepository.findById(savedJpa.getHouseBlId())
                .orElseThrow(() -> new ResourceNotFoundException("HouseBl", savedJpa.getHouseBlId()));
        return houseBlMapper.toDomain(reloaded);
    }

    @Override
    @Transactional
    public void delete(HouseBl houseBl) {
        Long id = houseBl.getId();
        houseBlSeaRepository.findByHouseBlHouseBlId(id).ifPresent(houseBlSeaRepository::delete);
        houseBlAirRepository.findByHouseBlHouseBlId(id).ifPresent(houseBlAirRepository::delete);
        houseBlTruckRepository.findByHouseBlHouseBlId(id).ifPresent(houseBlTruckRepository::delete);
        houseBlNonBlRepository.findByHouseBlHouseBlId(id).ifPresent(houseBlNonBlRepository::delete);
        houseBlRepository.deleteById(id);
    }

    private PagedResult<HouseBl> toPagedResult(Page<HouseBlJpaEntity> page) {
        List<HouseBl> content = page.getContent().stream()
                .map(houseBlMapper::toDomain)
                .toList();
        return PagedResult.of(content, page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize());
    }
}
