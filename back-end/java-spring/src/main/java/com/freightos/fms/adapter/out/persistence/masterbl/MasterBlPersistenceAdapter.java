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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MasterBlPersistenceAdapter implements MasterBlPort {

    private final MasterBlRepository masterBlRepository;
    private final MasterBlMapper masterBlMapper;

    @Override
    public Optional<MasterBl> findById(UUID id) {
        return masterBlRepository.findById(id)
                .map(masterBlMapper::toDomain);
    }

    @Override
    public PagedResult<MasterBl> findAllByBound(Bound bound, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPage =
                org.springframework.data.domain.PageRequest.of(
                        pageRequest.getPage(), pageRequest.getSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<MasterBlJpaEntity> page = masterBlRepository.findAllByBound(bound, springPage);
        List<MasterBl> content = page.getContent().stream()
                .map(masterBlMapper::toDomain)
                .toList();
        return PagedResult.of(content, page.getTotalElements(), page.getTotalPages(),
                page.getNumber(), page.getSize());
    }

    @Override
    public Optional<MasterBl> findByMblNo(String mblNo) {
        return masterBlRepository.findByMblNo(mblNo)
                .map(masterBlMapper::toDomain);
    }

    @Override
    public boolean existsByMblNo(String mblNo) {
        return masterBlRepository.existsByMblNo(mblNo);
    }

    @Override
    public void delete(MasterBl masterBl) {
        MasterBlJpaEntity jpa = masterBlMapper.toJpa(masterBl);
        masterBlRepository.delete(jpa);
    }
}
