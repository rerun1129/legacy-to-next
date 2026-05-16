package com.freightos.fms.application.masterbl.port.out;

import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;

/**
 * AIR Master B/L update 전용 outbound port.
 * House {@code SeaHblPersistencePort} / {@code SeaMasterPersistencePort} 패턴 정합.
 *
 * update는 dirty-checking으로 DB 반영만 수행한다. 응답은 호출자가 별도 GET으로 조회한다.
 */
public interface AirMasterPersistencePort {
    void update(Long id, UpdateMasterBlCommand command);
}
