package com.freightos.fms.adapter.out.persistence.housebl;

import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlAirPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlNonBlPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlSeaPersistenceStrategy;
import com.freightos.fms.adapter.out.persistence.housebl.strategy.HouseBlTruckPersistenceStrategy;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.HouseBlFilter;
import com.freightos.fms.domain.housebl.entity.HouseBlAir;
import com.freightos.fms.domain.housebl.entity.HouseBlSea;
import com.freightos.fms.domain.housebl.entity.HouseBlTruck;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlAirSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSeaSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledSeaContainer;
import com.freightos.fms.application.housebl.projection.HouseBlSummary;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
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
    private final HouseBlDomainToJpaMapper domainToJpaMapper;
    private final HouseBlSeaPersistenceStrategy seaStrategy;
    private final HouseBlAirPersistenceStrategy airStrategy;
    private final HouseBlTruckPersistenceStrategy truckStrategy;
    private final HouseBlNonBlPersistenceStrategy nonBlStrategy;

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
    public Optional<JobDiv> findJobDivById(Long id) {
        return houseBlRepository.findJobDivById(id);
    }

    @Override
    @Transactional
    public HouseBl saveHouseBl(HouseBl domain) {
        // 부모 엔티티 save/update
        HouseBlJpaEntity parentJpa;
        if (domain.getId() != null) {
            // applyCommonFields는 setter만 사용하므로 프록시 초기화 없이 dirty-checking으로 UPDATE 가능.
            // 존재하지 않는 id는 ResourceNotFoundException으로 선행 차단.
            if (!houseBlRepository.existsById(domain.getId())) {
                throw new ResourceNotFoundException("HouseBl", domain.getId());
            }
            parentJpa = houseBlRepository.getReferenceById(domain.getId());
        } else {
            parentJpa = new HouseBlJpaEntity();
        }
        domainToJpaMapper.applyCommonFields(domain, parentJpa);
        HouseBlJpaEntity savedJpa = houseBlRepository.save(parentJpa);

        // jobDiv별 확장 엔티티 save/update — Strategy에 위임 (타입 안전 switch)
        return switch (domain) {
            case HouseBlSea sea -> seaStrategy.saveExt(sea, savedJpa);
            case HouseBlAir air -> airStrategy.saveExt(air, savedJpa);
            case HouseBlTruck truck -> truckStrategy.saveExt(truck, savedJpa);
            case HouseBlNonBl nonBl -> nonBlStrategy.saveExt(nonBl, savedJpa);
            default -> throw new IllegalArgumentException("Unsupported HouseBl type: " + domain.getClass().getSimpleName());
        };
    }

    @Override
    @Transactional
    public void deleteByIdAndJobDiv(Long id, JobDiv jobDiv) {
        switch (jobDiv) {
            case SEA -> seaStrategy.deleteExt(id);
            case AIR -> airStrategy.deleteExt(id);
            case TRUCK -> truckStrategy.deleteExt(id);
            case NON_BL -> nonBlStrategy.deleteExt(id);
        }
        houseBlRepository.deleteByIdBulk(id);
    }

    @Override
    @Transactional
    public long updateHblNoById(Long id, BlNumber newHblNo, JobDiv expectedJobDiv) {
        return houseBlRepository.updateHblNoById(id, newHblNo.value(), expectedJobDiv);
    }

    @Override
    @Transactional
    public int nullifyMasterRefByMasterBlId(Long masterBlId) {
        return houseBlRepository.nullifyMasterRefByMasterBlId(masterBlId);
    }

    @Override
    @Transactional
    public int updateMasterRefByMasterBlId(Long masterBlId, String newMblNo, String newMasterRefNo) {
        return houseBlRepository.updateMasterRefByMasterBlId(masterBlId, newMblNo, newMasterRefNo);
    }

    @Override
    public List<Long> findHouseBlKeysByHblNoExact(String hblNo, JobDiv jobDiv) {
        return houseBlRepository.findHouseBlKeysByHblNoExact(hblNo, jobDiv);
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
    public List<ConsoledSeaContainer> findConsoledSeaContainersByMasterBlId(Long masterBlId) {
        return houseBlRepository.findConsoledSeaContainersByMasterBlId(masterBlId);
    }

    @Override
    public List<ConsoledHouseBlAirSummary> findConsoledAirSummariesByMasterBlId(Long masterBlId) {
        return houseBlRepository.findConsoledAirSummariesByMasterBlId(masterBlId);
    }

    private HouseBl loadWithExt(HouseBlJpaEntity jpa) {
        return switch (jpa.getJobDiv()) {
            case SEA -> seaStrategy.loadWithExt(jpa);
            case AIR -> airStrategy.loadWithExt(jpa);
            case TRUCK -> truckStrategy.loadWithExt(jpa);
            case NON_BL -> nonBlStrategy.loadWithExt(jpa);
        };
    }
}
