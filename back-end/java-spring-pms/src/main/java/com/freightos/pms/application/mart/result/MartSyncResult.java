package com.freightos.pms.application.mart.result;

import java.time.Instant;

/**
 * Mart 동기화 실행 결과.
 *
 * @param mode         실행 모드 ("full" | "incremental")
 * @param rowsWritten  MongoDB에 upsert된 문서 수
 * @param durationMs   실행 소요 시간(ms)
 * @param lastSyncAt   이 동기화가 완료된 시각(runAt 기준)
 */
public record MartSyncResult(
        String mode,
        long rowsWritten,
        long durationMs,
        Instant lastSyncAt
) {
}
