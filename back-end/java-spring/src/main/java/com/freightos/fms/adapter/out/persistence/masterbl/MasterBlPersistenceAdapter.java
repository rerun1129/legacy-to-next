package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirChargeJpaEntity;
import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlAirDescJpaEntity;
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
    private final MasterBlDimRepository masterBlDimRepository;
    private final MasterBlScheduleLegRepository masterBlScheduleLegRepository;
    private final MasterBlAirChargeRepository masterBlAirChargeRepository;
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
    public Optional<MasterBlJobDiv> findJobDivById(Long id) {
        return masterBlRepository.findJobDivById(id);
    }

    @Override
    @Transactional
    public Long createMasterBl(MasterBl domain) {
        MasterBlJpaEntity parentJpa = new MasterBlJpaEntity();
        masterBlMapper.applyCommonFields(domain, parentJpa);
        MasterBlJpaEntity savedJpa = masterBlRepository.save(parentJpa);
        switch (domain) {
            case MasterBlAir air -> {
                MasterBlAirJpaEntity airJpa = new MasterBlAirJpaEntity();
                airJpa.setMasterBl(savedJpa);
                masterBlMapper.applyAirFields(air, airJpa);
                MasterBlAirJpaEntity savedAirJpa = masterBlAirRepository.save(airJpa);
                savedAirJpa.syncDims(air.getDims().stream().map(masterBlMapper::toDimJpa).toList());
                savedAirJpa.syncScheduleLegs(air.getScheduleLegs().stream().map(masterBlMapper::toScheduleLegJpa).toList());
                savedAirJpa.syncAirCharges(air.getAirCharges().stream().map(masterBlMapper::toAirChargeJpa).toList());
                createAirDescIfPresent(air.getDesc(), savedAirJpa);
            }
            case MasterBlSea sea -> {
                MasterBlSeaJpaEntity seaJpa = new MasterBlSeaJpaEntity();
                seaJpa.setMasterBl(savedJpa);
                masterBlMapper.applySeaFields(sea, seaJpa);
                MasterBlSeaJpaEntity savedSeaJpa = masterBlSeaRepository.save(seaJpa);
                createSeaDescIfPresent(sea.getDesc(), savedSeaJpa);
            }
            default -> throw new IllegalArgumentException("Unsupported MasterBl type: " + domain.getClass().getSimpleName());
        }
        return savedJpa.getMasterBlId();
    }

    @Override
    @Transactional
    public void updateMasterBl(MasterBl domain) {
        Long id = domain.getId();
        if (id == null) throw new IllegalArgumentException("updateMasterBl requires domain.getId(): null");
        MasterBlJpaEntity parentJpa = masterBlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterBl", id));
        masterBlMapper.applyCommonFields(domain, parentJpa);
        MasterBlJpaEntity savedJpa = masterBlRepository.save(parentJpa);
        switch (domain) {
            case MasterBlAir air -> {
                MasterBlAirJpaEntity airJpa = masterBlAirRepository
                        .findByMasterBlMasterBlId(savedJpa.getMasterBlId())
                        .orElseGet(MasterBlAirJpaEntity::new);
                airJpa.setMasterBl(savedJpa);
                masterBlMapper.applyAirFields(air, airJpa);
                MasterBlAirJpaEntity savedAirJpa = masterBlAirRepository.save(airJpa);
                savedAirJpa.syncDims(air.getDims().stream().map(masterBlMapper::toDimJpa).toList());
                savedAirJpa.syncScheduleLegs(air.getScheduleLegs().stream().map(masterBlMapper::toScheduleLegJpa).toList());
                savedAirJpa.syncAirCharges(air.getAirCharges().stream().map(masterBlMapper::toAirChargeJpa).toList());
                updateOrDeleteAirDesc(air.getDesc(), savedAirJpa);
            }
            case MasterBlSea sea -> {
                MasterBlSeaJpaEntity seaJpa = masterBlSeaRepository
                        .findByMasterBlMasterBlId(savedJpa.getMasterBlId())
                        .orElseGet(MasterBlSeaJpaEntity::new);
                seaJpa.setMasterBl(savedJpa);
                masterBlMapper.applySeaFields(sea, seaJpa);
                MasterBlSeaJpaEntity savedSeaJpa = masterBlSeaRepository.save(seaJpa);
                updateOrDeleteSeaDesc(sea.getDesc(), savedSeaJpa);
            }
            default -> throw new IllegalArgumentException("Unsupported MasterBl type: " + domain.getClass().getSimpleName());
        }
    }

    @Override
    public List<Long> findMasterBlKeysByMblNoExact(String mblNo) {
        return masterBlRepository.findMasterBlKeysByMblNoExact(mblNo);
    }

    @Override
    @Transactional
    public long updateMblNoAndMasterRefById(Long id, String newMblNo, String newMasterRefNo) {
        return masterBlRepository.updateMblNoAndMasterRefById(id, newMblNo, newMasterRefNo);
    }

    @Override
    @Transactional
    public void deleteByIdAndJobDiv(Long id, MasterBlJobDiv jobDiv) {
        switch (jobDiv) {
            case SEA -> {
                masterBlSeaDescRepository.deleteByParentMasterBlId(id);
                masterBlSeaRepository.deleteByMasterBl_MasterBlId(id);
            }
            case AIR -> {
                masterBlDimRepository.deleteByParentMasterBlId(id);
                masterBlScheduleLegRepository.deleteByParentMasterBlId(id);
                masterBlAirChargeRepository.deleteByParentMasterBlId(id);
                masterBlAirDescRepository.deleteByParentMasterBlId(id);
                masterBlAirRepository.deleteByMasterBl_MasterBlId(id);
            }
        }
        masterBlRepository.deleteByIdBulk(id);
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

    /** create 흐름 전용: desc가 존재하는 경우에만 신규 insert. DB에 기존 row가 없으므로 조회 SELECT 불필요. */
    private void createSeaDescIfPresent(MasterBlDesc domainDesc, MasterBlSeaJpaEntity savedSeaJpa) {
        if (domainDesc == null) return;
        MasterBlSeaDescJpaEntity descJpa = new MasterBlSeaDescJpaEntity();
        masterBlMapper.applySeaDescFields(domainDesc, descJpa, savedSeaJpa);
        masterBlSeaDescRepository.save(descJpa);
    }

    /** update 흐름 전용: desc null이면 기존 row 삭제, non-null이면 upsert. */
    private void updateOrDeleteSeaDesc(MasterBlDesc domainDesc, MasterBlSeaJpaEntity savedSeaJpa) {
        Long seaId = savedSeaJpa.getMasterBlSeaId();
        if (domainDesc == null) {
            masterBlSeaDescRepository.findBySea_MasterBlSeaId(seaId).ifPresent(masterBlSeaDescRepository::delete);
            return;
        }
        MasterBlSeaDescJpaEntity descJpa = masterBlSeaDescRepository.findBySea_MasterBlSeaId(seaId).orElseGet(MasterBlSeaDescJpaEntity::new);
        masterBlMapper.applySeaDescFields(domainDesc, descJpa, savedSeaJpa);
        masterBlSeaDescRepository.save(descJpa);
    }

    /** create 흐름 전용: desc가 존재하는 경우에만 신규 insert. DB에 기존 row가 없으므로 조회 SELECT 불필요. */
    private void createAirDescIfPresent(MasterBlDesc domainDesc, MasterBlAirJpaEntity savedAirJpa) {
        if (domainDesc == null) return;
        MasterBlAirDescJpaEntity descJpa = new MasterBlAirDescJpaEntity();
        masterBlMapper.applyAirDescFields(domainDesc, descJpa, savedAirJpa);
        masterBlAirDescRepository.save(descJpa);
    }

    /** update 흐름 전용: desc null이면 기존 row 삭제, non-null이면 upsert. */
    private void updateOrDeleteAirDesc(MasterBlDesc domainDesc, MasterBlAirJpaEntity savedAirJpa) {
        Long airId = savedAirJpa.getMasterBlAirId();
        if (domainDesc == null) {
            masterBlAirDescRepository.findByAir_MasterBlAirId(airId).ifPresent(masterBlAirDescRepository::delete);
            return;
        }
        MasterBlAirDescJpaEntity descJpa = masterBlAirDescRepository.findByAir_MasterBlAirId(airId).orElseGet(MasterBlAirDescJpaEntity::new);
        masterBlMapper.applyAirDescFields(domainDesc, descJpa, savedAirJpa);
        masterBlAirDescRepository.save(descJpa);
    }
}
