package com.freightos.pms.application.mart.port.in;

import com.freightos.pms.adapter.out.mart.document.PmsMartSyncState;
import com.freightos.pms.application.mart.result.MartSyncResult;

/**
 * Mart 재빌드 및 상태 조회 인바운드 포트.
 * 컨트롤러는 이 UseCase만 의존한다.
 */
public interface PmsMartMaintenanceUseCase {

    /** OLTP 전체를 읽어 Mart를 완전 재빌드한다. */
    MartSyncResult rebuildFull();

    /** 마지막 동기화 이후 변경된 헤더만 증분 업데이트한다. */
    MartSyncResult rebuildIncremental();

    /** 현재 동기화 상태를 반환한다. 아직 동기화한 적 없으면 빈 상태 문서를 반환한다. */
    PmsMartSyncState status();
}
