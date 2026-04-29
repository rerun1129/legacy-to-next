package com.freightos.fms.domain.switchbl.port.in;

import com.freightos.fms.domain.switchbl.entity.SwitchBl;

public interface SwitchBlUseCase {
    SwitchBl getSwitchBlByHouseBlId(Long houseBlId);
    SwitchBl createSwitchBl(SwitchBl switchBl);
    SwitchBl updateSwitchBl(SwitchBl switchBl);
    void deleteSwitchBl(Long switchBlId);
}
