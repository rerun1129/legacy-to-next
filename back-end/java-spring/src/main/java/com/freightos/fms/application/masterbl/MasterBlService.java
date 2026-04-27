package com.freightos.fms.application.masterbl;

import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.port.in.MasterBlUseCase;
import com.freightos.fms.domain.masterbl.port.out.MasterBlPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MasterBlService implements MasterBlUseCase {

    private final MasterBlPort masterBlPort;

    @Override
    public PagedResult<MasterBl> list(Bound bound, PageRequest pageRequest) {
        return masterBlPort.findAllByBound(bound, pageRequest);
    }

    @Override
    public MasterBl getById(Long id) {
        return masterBlPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MasterBl", id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MasterBl entity = getById(id);
        masterBlPort.delete(entity);
        log.info("Deleted MasterBl id={}", id);
    }
}
