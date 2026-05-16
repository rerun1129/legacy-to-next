package com.freightos.fms.application.masterbl;

import com.freightos.common.exception.FmsException;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.masterbl.command.ChangeMasterBlNoCommand;
import com.freightos.fms.application.masterbl.command.CreateMasterBlCommand;
import com.freightos.fms.application.masterbl.command.SearchMasterBlCommand;
import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.application.masterbl.projection.MasterBlSummaryResult;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.port.out.HouseBlPort;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.housebl.projection.ConsoledHouseBlSummary;
import com.freightos.fms.domain.housebl.projection.ConsoledSeaContainer;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.entity.MasterBlAir;
import com.freightos.fms.domain.masterbl.entity.MasterBlSea;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.application.masterbl.port.in.MasterBlUseCase;
import com.freightos.fms.application.masterbl.port.out.MasterBlPort;
import com.freightos.fms.application.masterbl.port.out.SeaMasterPersistencePort;
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
    private final SeaMasterPersistencePort seaMasterPersistencePort;
    private final HouseBlPort houseBlPort;
    private final MasterBlFactory masterBlFactory;

    @Override
    public PagedResult<MasterBlSummaryResult> searchMasterBls(SearchMasterBlCommand cmd, PageRequest pageRequest) {
        return masterBlPort.searchMasterBls(masterBlFactory.toFilter(cmd), pageRequest);
    }

    @Override
    public MasterBlDetailResult findMasterBlById(Long id) {
        MasterBl entity = findEntityById(id);
        List<ConsoledHouseBlSummary> consoled = loadConsolidatedHouseBls(id, entity);
        List<ConsoledSeaContainer> containers = loadConsoledSeaContainers(id, entity);
        return masterBlFactory.toDetailResult(entity, consoled, containers);
    }

    @Override
    @Transactional
    public Long createMasterBl(CreateMasterBlCommand command) {
        if (command.mblNo() != null && masterBlPort.existsByMblNo(command.mblNo())) {
            throw FmsException.conflict("DUPLICATE_MBL_NO", "MBL No already exists: " + command.mblNo());
        }
        Long id = masterBlPort.createMasterBl(masterBlFactory.toEntity(command));
        log.info("Created MasterBl id={}", id);
        return id;
    }

    @Override
    public List<Long> findMasterBlKeysByMblNoExact(String mblNo) {
        return masterBlPort.findMasterBlKeysByMblNoExact(mblNo);
    }

    @Override
    @Transactional
    public MasterBlDetailResult updateMasterBl(Long id, UpdateMasterBlCommand command) {
        // jobDiv 분기는 PUT body 의 cmd.jobDiv() 신뢰 — FE variant 로 이미 결정되어 송신 (§6.63).
        // 변조 시 어댑터(SEA: parentJpa.jobDiv 검증) / saveMasterBl(AIR: switch(domain) default throw) 에서 fail.
        MasterBlJobDiv jobDiv = MasterBlJobDiv.valueOf(command.jobDiv());
        MasterBl updated = switch (jobDiv) {
            case SEA ->
                    // SEA: 어댑터가 dirty-checking 반영 후 attached entity로 도메인 재구성 반환 — 응답 reload 회피 (§6.63)
                    seaMasterPersistencePort.update(id, command);
            case AIR -> {
                // AIR: 기존 경로 유지 (AIR 격리 원칙) — Air Master 마이그레이션 시 동일 패턴 적용 (§6.63 잔여)
                MasterBl entity = findEntityById(id);
                masterBlFactory.applyToEntity(command, entity);
                masterBlPort.updateMasterBl(entity);
                yield findEntityById(id);
            }
        };
        log.info("Updated MasterBl id={}", id);
        List<ConsoledHouseBlSummary> consoled = loadConsolidatedHouseBls(id, updated);
        List<ConsoledSeaContainer> containers = loadConsoledSeaContainers(id, updated);
        return masterBlFactory.toDetailResult(updated, consoled, containers);
    }

    @Override
    @Transactional
    public void changeMblNo(Long id, ChangeMasterBlNoCommand command) {
        BlNumber newMbl = BlNumber.of(command.mblNo());
        BlNumber newRef = BlNumber.of(command.masterRefNo());
        if (newMbl == null) throw new IllegalArgumentException("mblNo must not be null or blank");
        if (newRef == null) throw new IllegalArgumentException("masterRefNo must not be null or blank");
        int hblAffected = houseBlPort.updateMasterRefByMasterBlId(id, newMbl.value(), newRef.value());
        long mblAffected = masterBlPort.updateMblNoAndMasterRefById(id, newMbl.value(), newRef.value());
        if (mblAffected == 0) throw new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND);
        log.info("Changed MasterBl mblNo/masterRefNo: id={} (consoled {} house_bl rows)", id, hblAffected);
    }

    @Override
    @Transactional
    public void deleteMasterBlById(Long id) {
        MasterBlJobDiv jobDiv = masterBlPort.findJobDivById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.MASTER_BL_NOT_FOUND));
        int unlinked = houseBlPort.nullifyMasterRefByMasterBlId(id);
        masterBlPort.deleteByIdAndJobDiv(id, jobDiv);
        log.info("Deleted MasterBl id={} (unlinked {} house_bl rows)", id, unlinked);
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

    private List<ConsoledSeaContainer> loadConsoledSeaContainers(Long id, MasterBl entity) {
        return switch (entity) {
            case MasterBlSea ignored -> houseBlPort.findConsoledSeaContainersByMasterBlId(id);
            default -> List.of();
        };
    }
}
