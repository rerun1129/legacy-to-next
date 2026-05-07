package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlScheduleLegJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.application.masterbl.port.out.MasterBlPort;
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
public class MasterBlPersistenceAdapter implements MasterBlPort {

    private final MasterBlRepository masterBlRepository;
    private final MasterBlSeaRepository masterBlSeaRepository;
    private final MasterBlAirRepository masterBlAirRepository;
    private final MasterBlMapper masterBlMapper;

    @Override
    public Optional<MasterBl> findMasterBlById(Long id) {
        return masterBlRepository.findById(id).map(this::loadWithExt);
    }

    @Override
    public PagedResult<MasterBl> getMasterBlsByBound(Bound bound, PageRequest pageRequest) {
        Page<MasterBlJpaEntity> page = masterBlRepository.findAllByBound(bound,
                org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize(),
                        Nullables.mapOrElse(pageRequest.getSortBy(), s -> Sort.by(Sort.Direction.valueOf(pageRequest.getSortDirection().name()), s), Sort::unsorted)));
        return PagedResult.of(page.getContent().stream()
                .map(this::loadWithExt)
                .toList(), page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize());
    }

    @Override
    public PagedResult<MasterBlSummaryResult> searchMasterBls(MasterBlFilter filter, PageRequest pageRequest) {
        return masterBlRepository.searchByFilter(filter, pageRequest);
    }

    @Override
    public Optional<MasterBl> findMasterBlByMblNo(String mblNo) {
        return masterBlRepository.findByMblNo(mblNo).map(this::loadWithExt);
    }

    @Override
    public boolean existsByMblNo(String mblNo) {
        return masterBlRepository.existsByMblNo(mblNo);
    }

    @Override
    @Transactional
    public MasterBl saveMasterBl(MasterBl domain) {
        MasterBlJpaEntity parentJpa;
        if (domain.getId() != null) {
            parentJpa = masterBlRepository.findById(domain.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("MasterBl", domain.getId()));
        } else {
            parentJpa = new MasterBlJpaEntity();
        }
        masterBlMapper.applyCommonFields(domain, parentJpa);
        final MasterBlJpaEntity savedJpa = masterBlRepository.save(parentJpa);

        switch (domain) {
            case MasterBlAir air -> {
                MasterBlAirJpaEntity airJpa = masterBlAirRepository
                        .findByMasterBlMasterBlId(savedJpa.getMasterBlId())
                        .orElseGet(MasterBlAirJpaEntity::new);
                airJpa.setMasterBl(savedJpa);
                masterBlMapper.applyAirFields(air, airJpa);
                List<MasterBlAirChargeJpaEntity> jpaCharges = air.getAirCharges().stream()
                        .map(c -> masterBlMapper.toAirChargeJpa(c, savedJpa))
                        .toList();
                savedJpa.syncAirCharges(jpaCharges);
                List<MasterBlDimJpaEntity> jpaDims = air.getDims().stream()
                        .map(d -> masterBlMapper.toDimJpa(d, savedJpa))
                        .toList();
                savedJpa.syncDims(jpaDims);
                List<MasterBlScheduleLegJpaEntity> jpaLegs = air.getScheduleLegs().stream()
                        .map(l -> masterBlMapper.toScheduleLegJpa(l, savedJpa))
                        .toList();
                savedJpa.syncScheduleLegs(jpaLegs);
                MasterBlDescJpaEntity airDescJpa = Nullables.mapOrNull(air.getDesc(), d -> masterBlMapper.toDescJpa(d, savedJpa));
                savedJpa.replaceDesc(airDescJpa);
                masterBlAirRepository.save(airJpa);
            }
            case MasterBlSea sea -> {
                MasterBlSeaJpaEntity seaJpa = masterBlSeaRepository
                        .findByMasterBlMasterBlId(savedJpa.getMasterBlId())
                        .orElseGet(MasterBlSeaJpaEntity::new);
                seaJpa.setMasterBl(savedJpa);
                masterBlMapper.applySeaFields(sea, seaJpa);
                MasterBlDescJpaEntity seaDescJpa = Nullables.mapOrNull(sea.getDesc(), d -> masterBlMapper.toDescJpa(d, savedJpa));
                savedJpa.replaceDesc(seaDescJpa);
                masterBlSeaRepository.save(seaJpa);
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported MasterBl type: " + domain.getClass().getSimpleName());
        }

        return loadWithExt(savedJpa);
    }

    @Override
    @Transactional
    public void deleteMasterBl(MasterBl masterBl) {
        Long id = masterBl.getId();
        masterBlSeaRepository.findByMasterBlMasterBlId(id).ifPresent(masterBlSeaRepository::delete);
        masterBlAirRepository.findByMasterBlMasterBlId(id).ifPresent(masterBlAirRepository::delete);
        masterBlRepository.deleteById(id);
    }

    private MasterBl loadWithExt(MasterBlJpaEntity jpa) {
        MasterBlJobDiv jobDiv = jpa.getJobDiv();
        if (jobDiv == null) throw new IllegalArgumentException("jobDiv is null for masterBlId: " + jpa.getMasterBlId());
        return switch (jobDiv) {
            case SEA -> masterBlMapper.toSeaDomain(jpa,
                    masterBlSeaRepository.findByMasterBlMasterBlId(jpa.getMasterBlId()).orElse(null));
            case AIR -> masterBlMapper.toAirDomain(jpa,
                    masterBlAirRepository.findByMasterBlMasterBlId(jpa.getMasterBlId()).orElse(null));
        };
    }
}
