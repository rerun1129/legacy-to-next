package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.adapter.out.persistence.masterbl.entity.MasterBlJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.port.out.MasterBlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        return masterBlRepository.findById(id).map(masterBlMapper::toDomain);
    }

    @Override
    public PagedResult<MasterBl> getMasterBlsByBound(Bound bound, PageRequest pageRequest) {
        Page<MasterBlJpaEntity> page = masterBlRepository.findAllByBound(bound,
                org.springframework.data.domain.PageRequest.of(pageRequest.getPage(), pageRequest.getSize(),
                        pageRequest.getSortBy() != null
                                ? Sort.by(Sort.Direction.valueOf(pageRequest.getSortDirection().name()), pageRequest.getSortBy())
                                : Sort.unsorted()));
        return PagedResult.of(page.getContent().stream()
                .map(masterBlMapper::toDomain)
                .toList(), page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize());
    }

    @Override
    public Optional<MasterBl> findMasterBlByMblNo(String mblNo) {
        return masterBlRepository.findByMblNo(mblNo).map(masterBlMapper::toDomain);
    }

    @Override
    public boolean existsByMblNo(String mblNo) {
        return masterBlRepository.existsByMblNo(mblNo);
    }

    @Override
    @Transactional
    public void deleteMasterBl(MasterBl masterBl) {
        Long id = masterBl.getId();
        masterBlSeaRepository.findByMasterBlMasterBlId(id).ifPresent(masterBlSeaRepository::delete);
        masterBlAirRepository.findByMasterBlMasterBlId(id).ifPresent(masterBlAirRepository::delete);
        masterBlRepository.deleteById(id);
    }
}
