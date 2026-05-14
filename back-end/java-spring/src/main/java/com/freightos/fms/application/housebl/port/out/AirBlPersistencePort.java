package com.freightos.fms.application.housebl.port.out;

import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;

/** AIR House B/L update 전용 아웃바운드 포트 (§6.35 도메인 전용 Port+Adapter). */
public interface AirBlPersistencePort {
    void update(Long id, UpdateHouseBlCommand command);
}
