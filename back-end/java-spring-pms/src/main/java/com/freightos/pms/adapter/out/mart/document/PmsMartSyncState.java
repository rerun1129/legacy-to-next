package com.freightos.pms.adapter.out.mart.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Mart 동기화 상태 추적 문서.
 * 컬렉션: pms_mart_sync_state.
 * id는 대상 컬렉션명을 고정 키로 사용("pms_bl_mart" 등).
 */
@Document("pms_mart_sync_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PmsMartSyncState {

    /** 대상 컬렉션명을 고정 키로 사용. */
    @Id
    private String id;

    /** 마지막 증분 동기화 완료 시각. */
    private Instant lastSyncAt;

    /** 마지막 전체 재빌드 완료 시각. */
    private Instant lastFullRebuildAt;

    /** 마지막 동기화에서 처리된 총 문서 수. */
    private long lastRowCount;

    /** 마지막 동기화 소요 시간(ms). */
    private long lastDurationMs;
}
