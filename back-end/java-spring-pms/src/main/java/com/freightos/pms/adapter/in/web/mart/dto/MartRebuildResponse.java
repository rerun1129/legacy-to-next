package com.freightos.pms.adapter.in.web.mart.dto;

import com.freightos.pms.application.mart.result.MartSyncResult;

import java.time.Instant;

/**
 * Mart 재빌드 응답 DTO.
 * MartSyncResult와 구조가 동일하지만 Adapter 계층 경계에서 분리한다.
 *
 * @param mode        실행 모드 ("full" | "incremental")
 * @param rowsWritten upsert된 문서 수
 * @param durationMs  소요 시간(ms)
 * @param lastSyncAt  동기화 완료 시각
 */
public record MartRebuildResponse(
        String mode,
        long rowsWritten,
        long durationMs,
        Instant lastSyncAt
) {
    public static MartRebuildResponse from(MartSyncResult result) {
        return new MartRebuildResponse(
                result.mode(),
                result.rowsWritten(),
                result.durationMs(),
                result.lastSyncAt()
        );
    }
}
