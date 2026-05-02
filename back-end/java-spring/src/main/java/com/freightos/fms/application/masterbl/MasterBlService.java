package com.freightos.fms.application.masterbl;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.SortDirection;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.port.out.HouseBlPort;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSummary;
import com.freightos.fms.domain.masterbl.MasterBlDetail;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.port.in.MasterBlUseCase;
import com.freightos.fms.domain.masterbl.port.out.MasterBlPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MasterBlService implements MasterBlUseCase {

    private final MasterBlPort masterBlPort;
    private final HouseBlPort houseBlPort;

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
    public MasterBl save(MasterBl masterBl) {
        log.debug("Saving MasterBl: {}", masterBl.getMblNo());
        return masterBlPort.saveMasterBl(masterBl);
    }

    @Override
    @Transactional
    public void deleteMasterBlById(Long id) {
        masterBlPort.deleteMasterBl(findMasterBlById(id));
        log.info("Deleted MasterBl id={}", id);
    }

    @Override
    public MasterBlDetail findMasterBlDetailById(Long id) {
        MasterBl master = findMasterBlById(id);
        List<ConsoledHouseBlSummary> consolidatedList = switch (master) {
            case MasterBlSea ignored ->
                    new java.util.ArrayList<>(
                            houseBlPort.findConsoledSeaSummariesByMasterBlId(id));
            case MasterBlAir ignored ->
                    new java.util.ArrayList<>(
                            houseBlPort.findConsoledAirSummariesByMasterBlId(id));
            default -> List.of();
        };
        return new MasterBlDetail(master, consolidatedList);
    }
}
