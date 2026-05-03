package com.freightos.fms.domain.switchbl.port.in;

import com.freightos.fms.domain.switchbl.entity.SwitchBl;

import java.util.Optional;

public interface SwitchBlUseCase {
    SwitchBl getSwitchBlByHouseBlId(Long houseBlId);
    /** houseBlId에 매핑된 Switch B/L을 Optional로 조회한다. 미존재 시 empty를 반환한다. */
    Optional<SwitchBl> findOptionalByHouseBlId(Long houseBlId);
    SwitchBl findSwitchBlById(Long switchBlId);
    SwitchBl createSwitchBl(SwitchBl switchBl);
    SwitchBl updateSwitchBl(SwitchBl switchBl);
    void deleteSwitchBl(Long switchBlId);
}
