package com.freightos.fms.application.masterbl;

import com.freightos.fms.common.exception.ResourceNotFoundException;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.SortDirection;
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
    public PagedResult<MasterBl> getMasterBlsByBound(Bound bound, PageRequest pageRequest) {
        return masterBlPort.getMasterBlsByBound(bound,PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), "createdAt", SortDirection.DESC));
    }

    @Override
    public MasterBl findMasterBlById(Long id) {
        return masterBlPort.findMasterBlById(id).orElseThrow(() -> new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND));
    }

    @Override
    @Transactional
    public void deleteMasterBlById(Long id) {
        masterBlPort.deleteMasterBl(findMasterBlById(id));
        log.info("Deleted MasterBl id={}", id);
    }
}
