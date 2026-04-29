package com.freightos.fms.domain.switchbl.port.out;

import com.freightos.fms.domain.switchbl.entity.SwitchBl;

import java.util.Optional;

public interface SwitchBlPort {
    Optional<SwitchBl> findSwitchBlByHouseBlId(Long houseBlId);
    Optional<SwitchBl> findSwitchBlById(Long switchBlId);
    SwitchBl saveSwitchBl(SwitchBl switchBl);
    void deleteSwitchBl(SwitchBl switchBl);
}
