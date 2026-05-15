package com.freightos.fms.application.masterbl;

import com.freightos.common.exception.FmsException;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.SearchMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSummary;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.application.masterbl.port.in.MasterBlUseCase;
import com.freightos.fms.application.masterbl.port.out.MasterBlPort;
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
    private final MasterBlFactory masterBlFactory;

    @Override
    public PagedResult<MasterBlSummaryResult> searchMasterBls(SearchMasterBlCommand cmd, PageRequest pageRequest) {
        return masterBlPort.searchMasterBls(masterBlFactory.toFilter(cmd), pageRequest);
    }

    @Override
    public MasterBlDetailResult findMasterBlById(Long id) {
        MasterBl entity = findEntityById(id);
        return masterBlFactory.toDetailResult(entity, loadConsolidatedHouseBls(id, entity));
    }

    @Override
    @Transactional
    public Long createMasterBl(CreateMasterBlCommand command) {
        if (command.mblNo() != null && masterBlPort.existsByMblNo(command.mblNo())) {
            throw FmsException.conflict("DUPLICATE_MBL_NO", "MBL No already exists: " + command.mblNo());
        }
        MasterBl saved = masterBlPort.saveMasterBl(masterBlFactory.toEntity(command));
        log.info("Created MasterBl id={}", saved.getId());
        return saved.getId();
    }

    @Override
    public List<Long> findMasterBlKeysByMblNoExact(String mblNo) {
        return masterBlPort.findMasterBlKeysByMblNoExact(mblNo);
    }

    @Override
    @Transactional
    public MasterBlDetailResult updateMasterBl(Long id, UpdateMasterBlCommand command) {
        MasterBl entity = findEntityById(id);
        masterBlFactory.applyToEntity(command, entity);
        MasterBl saved = masterBlPort.saveMasterBl(entity);
        log.info("Updated MasterBl id={}", saved.getId());
        return masterBlFactory.toDetailResult(saved, loadConsolidatedHouseBls(id, saved));
    }

    @Override
    @Transactional
    public void deleteMasterBlById(Long id) {
        MasterBlJobDiv jobDiv = masterBlPort.findJobDivById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND));
        masterBlPort.deleteByIdAndJobDiv(id, jobDiv);
        log.info("Deleted MasterBl id={}", id);
    }

    private MasterBl findEntityById(Long id) {
        return masterBlPort.findMasterBlById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND));
    }

    private List<ConsoledHouseBlSummary> loadConsolidatedHouseBls(Long id, MasterBl entity) {
        return switch (entity) {
            case MasterBlSea ignored -> new java.util.ArrayList<>(houseBlPort.findConsoledSeaSummariesByMasterBlId(id));
            case MasterBlAir ignored -> new java.util.ArrayList<>(houseBlPort.findConsoledAirSummariesByMasterBlId(id));
            default -> List.of();
        };
    }
}
