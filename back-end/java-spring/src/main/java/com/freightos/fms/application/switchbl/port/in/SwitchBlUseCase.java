package com.freightos.fms.application.switchbl.port.in;

import com.freightos.fms.application.switchbl.command.CreateSwitchBlCommand;
import com.freightos.fms.application.switchbl.command.UpdateSwitchBlCommand;
import com.freightos.fms.application.switchbl.projection.SwitchBlDetailResult;

import java.util.Optional;

public interface SwitchBlUseCase {
    SwitchBlDetailResult getSwitchBlByHouseBlId(Long houseBlId);
    /** houseBlId에 매핑된 Switch B/L을 Optional로 조회한다. 미존재 시 empty를 반환한다. */
    Optional<SwitchBlDetailResult> findOptionalByHouseBlId(Long houseBlId);
    SwitchBlDetailResult findSwitchBlById(Long switchBlId);
    SwitchBlDetailResult createSwitchBl(CreateSwitchBlCommand command);
    SwitchBlDetailResult updateSwitchBl(Long id, UpdateSwitchBlCommand command);
    void deleteSwitchBl(Long switchBlId);
}
