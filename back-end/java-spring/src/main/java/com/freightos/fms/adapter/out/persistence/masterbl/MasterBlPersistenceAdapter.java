package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlDimJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlScheduleLegJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaDescJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlSeaJpaEntity;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.domain.masterbl.MasterBlFilter;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlDesc;
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
    private final MasterBlSeaDescRepository masterBlSeaDescRepository;
    private final MasterBlAirDescRepository masterBlAirDescRepository;
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
                List<MasterBlDimJpaEntity> jpaDims = air.getDims().stream()
                        .map(d -> masterBlMapper.toDimJpa(d, savedJpa))
                        .toList();
                savedJpa.syncDims(jpaDims);
                // airJpa를 먼저 영속화하여 master_bl_air_id PK 확보 후 scheduleLegs/airCharges/desc 저장
                MasterBlAirJpaEntity savedAirJpa = masterBlAirRepository.save(airJpa);
                List<MasterBlScheduleLegJpaEntity> jpaLegs = air.getScheduleLegs().stream()
                        .map(masterBlMapper::toScheduleLegJpa)
                        .toList();
                savedAirJpa.syncScheduleLegs(jpaLegs);
                List<MasterBlAirChargeJpaEntity> jpaCharges = air.getAirCharges().stream()
                        .map(masterBlMapper::toAirChargeJpa)
                        .toList();
                savedAirJpa.syncAirCharges(jpaCharges);
                saveOrDeleteAirDesc(air.getDesc(), savedAirJpa);
            }
            case MasterBlSea sea -> {
                MasterBlSeaJpaEntity seaJpa = masterBlSeaRepository
                        .findByMasterBlMasterBlId(savedJpa.getMasterBlId())
                        .orElseGet(MasterBlSeaJpaEntity::new);
                seaJpa.setMasterBl(savedJpa);
                masterBlMapper.applySeaFields(sea, seaJpa);
                // seaJpa를 먼저 영속화하여 master_bl_sea_id PK 확보 후 desc 저장
                MasterBlSeaJpaEntity savedSeaJpa = masterBlSeaRepository.save(seaJpa);
                saveOrDeleteSeaDesc(sea.getDesc(), savedSeaJpa);
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
        // sea_desc/air_desc는 ext FK ON DELETE CASCADE — ext 삭제 시 DB가 자동 정리
        masterBlSeaRepository.findByMasterBlMasterBlId(id).ifPresent(masterBlSeaRepository::delete);
        masterBlAirRepository.findByMasterBlMasterBlId(id).ifPresent(masterBlAirRepository::delete);
        masterBlRepository.deleteById(id);
    }

    private MasterBl loadWithExt(MasterBlJpaEntity jpa) {
        MasterBlJobDiv jobDiv = jpa.getJobDiv();
        if (jobDiv == null) throw new IllegalArgumentException("jobDiv is null for masterBlId: " + jpa.getMasterBlId());
        return switch (jobDiv) {
            case SEA -> {
                MasterBlSeaJpaEntity seaJpa = masterBlSeaRepository.findByMasterBlMasterBlId(jpa.getMasterBlId()).orElse(null);
                MasterBlSeaDescJpaEntity seaDescJpa = seaJpa != null
                        ? masterBlSeaDescRepository.findBySea_MasterBlSeaId(seaJpa.getMasterBlSeaId()).orElse(null)
                        : null;
                yield masterBlMapper.toSeaDomain(jpa, seaJpa, seaDescJpa);
            }
            case AIR -> {
                MasterBlAirJpaEntity airJpa = masterBlAirRepository
                        .findByMasterBlMasterBlId(jpa.getMasterBlId()).orElse(null);
                MasterBlAirDescJpaEntity airDescJpa = airJpa != null
                        ? masterBlAirDescRepository.findByAir_MasterBlAirId(airJpa.getMasterBlAirId()).orElse(null)
                        : null;
                yield masterBlMapper.toAirDomain(jpa, airJpa, airJpa != null ? airJpa.getAirCharges() : List.of(), airDescJpa);
            }
        };
    }

    /**
     * SEA desc 저장·삭제 처리. seaExt PK 확보 후 호출해야 한다.
     * 도메인 desc가 있으면 기존 row를 조회해 필드를 덮어쓰거나(UPDATE) 신규 insert한다.
     * 도메인 desc가 null이면 기존 row를 삭제한다(orphanRemoval 흉내).
     */
    private void saveOrDeleteSeaDesc(MasterBlDesc domainDesc, MasterBlSeaJpaEntity savedSeaJpa) {
        Long seaId = savedSeaJpa.getMasterBlSeaId();
        if (domainDesc == null) {
            masterBlSeaDescRepository.findBySea_MasterBlSeaId(seaId)
                    .ifPresent(masterBlSeaDescRepository::delete);
            return;
        }
        MasterBlSeaDescJpaEntity descJpa = masterBlSeaDescRepository.findBySea_MasterBlSeaId(seaId)
                .orElseGet(MasterBlSeaDescJpaEntity::new);
        masterBlMapper.applySeaDescFields(domainDesc, descJpa, savedSeaJpa);
        masterBlSeaDescRepository.save(descJpa);
    }

    /**
     * AIR desc 저장·삭제 처리. airExt PK 확보 후 호출해야 한다.
     * 도메인 desc가 있으면 기존 row를 조회해 필드를 덮어쓰거나(UPDATE) 신규 insert한다.
     * 도메인 desc가 null이면 기존 row를 삭제한다(orphanRemoval 흉내).
     */
    private void saveOrDeleteAirDesc(MasterBlDesc domainDesc, MasterBlAirJpaEntity savedAirJpa) {
        Long airId = savedAirJpa.getMasterBlAirId();
        if (domainDesc == null) {
            masterBlAirDescRepository.findByAir_MasterBlAirId(airId)
                    .ifPresent(masterBlAirDescRepository::delete);
            return;
        }
        MasterBlAirDescJpaEntity descJpa = masterBlAirDescRepository.findByAir_MasterBlAirId(airId)
                .orElseGet(MasterBlAirDescJpaEntity::new);
        masterBlMapper.applyAirDescFields(domainDesc, descJpa, savedAirJpa);
        masterBlAirDescRepository.save(descJpa);
    }
}
