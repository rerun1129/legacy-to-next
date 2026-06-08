package com.freightos.pms.application.mart;

import com.freightos.pms.adapter.out.mart.document.PmsMartSyncState;
import com.freightos.pms.application.mart.port.in.PmsMartMaintenanceUseCase;
import com.freightos.pms.application.mart.port.out.PmsMartReadinessPort;
import com.freightos.pms.application.mart.port.out.PmsMartSyncPort;
import com.freightos.pms.application.mart.result.MartSyncResult;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Mart 재빌드/상태 인바운드 UseCase 구현체.
 * PmsMartSyncPort(아웃바운드 포트)에 완전히 위임한다.
 */
@Service
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class PmsMartMaintenanceService implements PmsMartMaintenanceUseCase {

    private final PmsMartSyncPort syncPort;
    private final PmsMartReadinessPort readinessPort;

    @Override
    public MartSyncResult rebuildFull() {
        MartSyncResult result = syncPort.rebuildFull();
        readinessPort.markReady();
        return result;
    }

    @Override
    public MartSyncResult rebuildIncremental() {
        return syncPort.rebuildIncremental();
    }

    @Override
    public PmsMartSyncState status() {
        return syncPort.readState();
    }
}
