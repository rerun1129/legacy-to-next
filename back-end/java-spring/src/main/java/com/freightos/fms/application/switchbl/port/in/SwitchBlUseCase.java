package com.freightos.fms.application.switchbl.port.in;

import com.freightos.fms.application.switchbl.command.CreateSwitchBlCommand;
import com.freightos.fms.application.switchbl.command.UpdateSwitchBlCommand;
import com.freightos.fms.application.switchbl.projection.SwitchBlDetailResult;
import java.util.Optional;

public interface SwitchBlUseCase {
    Optional<SwitchBlDetailResult> findSwitchBlByHouseBlId(Long houseBlId);
    SwitchBlDetailResult findSwitchBlById(Long switchBlId);
    SwitchBlDetailResult createSwitchBl(CreateSwitchBlCommand command);
    SwitchBlDetailResult updateSwitchBl(Long id, UpdateSwitchBlCommand command);
    void deleteSwitchBl(Long switchBlId);
}
