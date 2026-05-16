package com.freightos.fms.application.masterbl.port.out;

import com.freightos.fms.application.masterbl.command.UpdateMasterBlCommand;
import com.freightos.fms.domain.masterbl.entity.MasterBl;

/**
 * AIR Master B/L update 전용 outbound port.
 * House {@code SeaHblPersistencePort} / {@code SeaMasterPersistencePort} 패턴 정합.
 *
 * update는 dirty-checking 반영 후 attached entity로부터 도메인을 재구성해 반환한다.
 * 호출자(MasterBlService)가 응답 빌드 시 reload SELECT를 피하기 위함 (§6.63).
 */
public interface AirMasterPersistencePort {
    MasterBl update(Long id, UpdateMasterBlCommand command);
}
