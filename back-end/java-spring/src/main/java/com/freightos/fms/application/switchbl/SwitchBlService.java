package com.freightos.fms.application.switchbl;

import com.freightos.common.exception.FmsException;
import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.switchbl.entity.SwitchBl;
import com.freightos.fms.application.switchbl.port.in.SwitchBlUseCase;
import com.freightos.fms.application.switchbl.port.out.SwitchBlPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SwitchBlService implements SwitchBlUseCase {

    private final SwitchBlPort switchBlPort;

    @Override
    public SwitchBl getSwitchBlByHouseBlId(Long houseBlId) {
        return switchBlPort.findSwitchBlByHouseBlId(houseBlId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.SWITCH_BL_NOT_FOUND));
    }

    @Override
    public Optional<SwitchBl> findOptionalByHouseBlId(Long houseBlId) {
        return switchBlPort.findSwitchBlByHouseBlId(houseBlId);
    }

    @Override
    public SwitchBl findSwitchBlById(Long switchBlId) {
        return switchBlPort.findSwitchBlById(switchBlId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageCode.SWITCH_BL_NOT_FOUND));
    }

    @Override
    @Transactional
    public SwitchBl createSwitchBl(SwitchBl switchBl) {
        // House B/L 1:1 UNIQUE 제약 — 동일 houseBlId에 이미 Switch B/L이 존재하면 중복 거부
        if (switchBlPort.findSwitchBlByHouseBlId(switchBl.getHouseBlId()).isPresent()) {
            throw new FmsException(HttpStatus.CONFLICT, "DUPLICATE_SWITCH_BL",
                    "Switch B/L already exists for houseBlId: " + switchBl.getHouseBlId());
        }
        SwitchBl saved = switchBlPort.saveSwitchBl(switchBl);
        log.info("Created SwitchBl id={} for houseBlId={}", saved.getSwitchBlId(), saved.getHouseBlId());
        return saved;
    }

    @Override
    @Transactional
    public SwitchBl updateSwitchBl(SwitchBl switchBl) {
        SwitchBl saved = switchBlPort.saveSwitchBl(switchBl);
        log.info("Updated SwitchBl id={}", saved.getSwitchBlId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteSwitchBl(Long switchBlId) {
        switchBlPort.deleteSwitchBl(findSwitchBlById(switchBlId));
        log.info("Deleted SwitchBl id={}", switchBlId);
    }
}
