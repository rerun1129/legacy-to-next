package com.freightos.fms.application.masterbl.port.out;

import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;

/**
 * SEA Master B/L update 전용 outbound port (§6.35).
 * House {@code SeaHblPersistencePort} 패턴 정합.
 */
public interface SeaMasterPersistencePort {
    void update(Long id, UpdateMasterBlCommand command);
}
