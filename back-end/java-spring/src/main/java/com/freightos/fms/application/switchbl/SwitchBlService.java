package com.freightos.fms.application.switchbl;

import com.freightos.common.exception.FmsException;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.application.switchbl.command.CreateSwitchBlCommand;
import com.freightos.fms.application.switchbl.command.UpdateSwitchBlCommand;
import com.freightos.fms.application.switchbl.projection.SwitchBlDetailResult;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.switchbl.entity.SwitchBl;
import com.freightos.fms.application.switchbl.port.in.SwitchBlUseCase;
import com.freightos.fms.application.switchbl.port.out.SwitchBlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SwitchBlService implements SwitchBlUseCase {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SwitchBlService.class);

    private final SwitchBlPort switchBlPort;
    private final SwitchBlFactory switchBlFactory;

    @Override
    public SwitchBlDetailResult getSwitchBlByHouseBlId(Long houseBlId) {
        SwitchBl entity = switchBlPort.findSwitchBlByHouseBlId(houseBlId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.SWITCH_BL_NOT_FOUND));
        return switchBlFactory.toDetailResult(entity);
    }

    @Override
    public Optional<SwitchBlDetailResult> findOptionalByHouseBlId(Long houseBlId) {
        return switchBlPort.findSwitchBlByHouseBlId(houseBlId)
                .map(switchBlFactory::toDetailResult);
    }

    @Override
    public SwitchBlDetailResult findSwitchBlById(Long switchBlId) {
        return switchBlFactory.toDetailResult(findEntityById(switchBlId));
    }

    @Override
    @Transactional
    public SwitchBlDetailResult createSwitchBl(CreateSwitchBlCommand command) {
        // House B/L 1:1 UNIQUE 제약 — 동일 houseBlId에 이미 Switch B/L이 존재하면 중복 거부
        if (switchBlPort.findSwitchBlByHouseBlId(command.houseBlId()).isPresent()) {
            throw new FmsException(HttpStatus.CONFLICT, "DUPLICATE_SWITCH_BL",
                    "Switch B/L already exists for houseBlId: " + command.houseBlId());
        }
        SwitchBl saved = switchBlPort.saveSwitchBl(switchBlFactory.toEntity(command));
        log.info("Created SwitchBl id={} for houseBlId={}", saved.getSwitchBlId(), saved.getHouseBlId());
        return switchBlFactory.toDetailResult(saved);
    }

    @Override
    @Transactional
    public SwitchBlDetailResult updateSwitchBl(Long id, UpdateSwitchBlCommand command) {
        SwitchBl entity = findEntityById(id);
        switchBlFactory.applyToEntity(command, entity);
        SwitchBl saved = switchBlPort.saveSwitchBl(entity);
        log.info("Updated SwitchBl id={}", saved.getSwitchBlId());
        return switchBlFactory.toDetailResult(saved);
    }

    @Override
    @Transactional
    public void deleteSwitchBl(Long switchBlId) {
        switchBlPort.deleteSwitchBl(findEntityById(switchBlId));
        log.info("Deleted SwitchBl id={}", switchBlId);
    }

    private SwitchBl findEntityById(Long switchBlId) {
        return switchBlPort.findSwitchBlById(switchBlId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.SWITCH_BL_NOT_FOUND));
    }
}
