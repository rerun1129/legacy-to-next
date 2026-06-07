package com.freightos.pms.application.mart.port.out;

import com.freightos.pms.adapter.out.mart.document.PmsMartSyncState;
import com.freightos.pms.application.mart.result.MartSyncResult;

/**
 * Mart ETL/동기화 아웃바운드 포트.
 * Application은 이 인터페이스만 알며, 구현체(PmsMartEtlService)는 adapter/out에 위치한다.
 */
public interface PmsMartSyncPort {

    MartSyncResult rebuildFull();

    MartSyncResult rebuildIncremental();

    PmsMartSyncState readState();
}
