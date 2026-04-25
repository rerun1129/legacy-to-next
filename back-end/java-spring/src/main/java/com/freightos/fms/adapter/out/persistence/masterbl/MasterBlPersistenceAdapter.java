package com.freightos.fms.adapter.out.persistence.masterbl;

import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.port.out.MasterBlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MasterBlPersistenceAdapter implements MasterBlPort {

    private final MasterBlRepository masterBlRepository;

    @Override
    public Optional<MasterBl> findById(UUID id) {
        return masterBlRepository.findById(id);
    }

    @Override
    public Page<MasterBl> findAllByBound(Bound bound, Pageable pageable) {
        return masterBlRepository.findAllByBound(bound, pageable);
    }

    @Override
    public Optional<MasterBl> findByMblNo(String mblNo) {
        return masterBlRepository.findByMblNo(mblNo);
    }

    @Override
    public boolean existsByMblNo(String mblNo) {
        return masterBlRepository.existsByMblNo(mblNo);
    }

    @Override
    public void delete(MasterBl masterBl) {
        masterBlRepository.delete(masterBl);
    }
}
