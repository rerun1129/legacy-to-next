package com.freightos.pms.adapter.out.mart.sync;

import com.freightos.pms.adapter.out.mart.countindex.PmsCountIndexBulkBuilder;
import com.freightos.pms.adapter.out.mart.countindex.PmsCountIndexMaintainer;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.adapter.out.mart.document.PmsMartSyncState;
import com.freightos.pms.application.mart.port.out.PmsMartSyncPort;
import com.freightos.pms.application.mart.result.MartSyncResult;
import com.freightos.common.config.PmsMartProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Mart ETL 아웃바운드 포트 구현체.
 *
 * <p>AtomicBoolean running으로 동시 실행을 차단한다(스케줄러·엔드포인트 공유).
 * <p>sync state는 pms_mart_sync_state 컬렉션에 "pms_bl_mart" 고정 키로 저장된다.
 *
 * <p>Count Index 훅:
 * <ul>
 *   <li>doFull: per-batch applyBatch 제거. full 완료 후 bulkBuilder.rebuildFromMart() 동기 호출.
 *   <li>doIncremental: upsert 전 oldDocs 조회 → applyChanges(oldDocs, newDocs).
 * </ul>
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class PmsMartEtlService implements PmsMartSyncPort {

    static final String STATE_ID = "pms_bl_mart";

    private final PmsMartSourceReader reader;
    private final PmsMartUpsertWriter writer;
    private final PmsMartChangeDetector changeDetector;
    private final MongoTemplate mongoTemplate;
    private final PmsMartProperties props;
    /** line-accel OFF면 빈 Optional — @ConditionalOnProperty로 빈 미등록. */
    private final Optional<PmsMartEntryWriter> entryWriter;
    /** count-index OFF면 빈 Optional — @ConditionalOnProperty로 빈 미등록. */
    private final Optional<PmsCountIndexMaintainer> countIndex;
    /** count-index OFF면 빈 Optional — @ConditionalOnProperty로 빈 미등록. */
    private final Optional<PmsCountIndexBulkBuilder> countIndexBulkBuilder;

    /** 동시 실행 방지 플래그. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    // ── PmsMartSyncPort 구현 ──────────────────────────────────────────────────

    @Override
    public MartSyncResult rebuildFull() {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Mart ETL이 이미 실행 중입니다. 동시 실행 불가.");
        }
        try {
            return doFull();
        } finally {
            running.set(false);
        }
    }

    @Override
    public MartSyncResult rebuildIncremental() {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Mart ETL이 이미 실행 중입니다. 동시 실행 불가.");
        }
        try {
            return doIncremental();
        } finally {
            running.set(false);
        }
    }

    @Override
    public PmsMartSyncState readState() {
        PmsMartSyncState state = findState();
        return state != null ? state : emptyState();
    }

    // ── 내부 실행 ─────────────────────────────────────────────────────────────

    private MartSyncResult doFull() {
        Instant runAt = Instant.now();
        long startMs = System.currentTimeMillis();
        log.info("Mart full rebuild 시작: runAt={}", runAt);

        long[] bounds = reader.headerIdBounds();
        if (bounds == null) {
            long durationMs = System.currentTimeMillis() - startMs;
            saveState(PmsMartSyncState.builder()
                .id(STATE_ID)
                .lastFullRebuildAt(runAt)
                .lastSyncAt(runAt)
                .lastRowCount(0L)
                .lastDurationMs(durationMs)
                .build());
            log.info("Mart full rebuild 완료: 대상 없음(빈 테이블), {} ms", durationMs);
            return new MartSyncResult("full", 0L, durationMs, runAt);
        }

        int n = Math.max(1, props.getRebuild().getParallelism());
        int batchSize = props.getRebuild().getBatchSize();
        List<long[]> ranges = splitRanges(bounds[0], bounds[1], n);
        log.info("Mart full rebuild: 레인지 {}개 (lo={}, hi={}, parallelism={})", ranges.size(), bounds[0], bounds[1], n);

        AtomicLong total = new AtomicLong();
        ExecutorService pool = Executors.newFixedThreadPool(n);
        try {
            List<Future<?>> futures = new ArrayList<>(ranges.size());
            for (long[] range : ranges) {
                long rLo = range[0];
                long rHi = range[1];
                futures.add(pool.submit(() -> {
                    long read = reader.readRange(rLo, rHi, batchSize, runAt, docs -> {
                        writer.upsertBatch(docs);
                        entryWriter.ifPresent(w -> w.writeFromDocs(docs));
                        // count-index 훅: per-batch 적재 제거. full 완료 후 bulk rebuild가 담당
                        total.addAndGet(docs.size());
                    });
                    log.debug("Mart full range [{}, {}] 완료: {} 건", rLo, rHi, read);
                }));
            }
            for (Future<?> f : futures) {
                try {
                    f.get();
                } catch (ExecutionException e) {
                    pool.shutdownNow();
                    throw new RuntimeException("Mart full rebuild 워커 실패: " + e.getCause().getMessage(), e.getCause());
                } catch (InterruptedException e) {
                    pool.shutdownNow();
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Mart full rebuild 인터럽트 발생", e);
                }
            }
        } finally {
            pool.shutdown();
        }

        long durationMs = System.currentTimeMillis() - startMs;
        saveState(PmsMartSyncState.builder()
            .id(STATE_ID)
            .lastFullRebuildAt(runAt)
            .lastSyncAt(runAt)
            .lastRowCount(total.get())
            .lastDurationMs(durationMs)
            .build());

        // Count Index: full rebuild 성공 완료 후 bulk rebuild 실행(동기)
        // 4개 병렬 워커가 동일 비트맵 키에 RMW하는 lost-update를 방지한다
        countIndexBulkBuilder.ifPresent(b -> {
            try {
                b.rebuildFromMart();
            } catch (Exception e) {
                log.warn("Count Index bulk rebuild 실패 — Mongo 폴백 유지: {}", e.toString());
            }
        });

        log.info("Mart full rebuild 완료: {} 건, {} ms (워커 {}개)", total.get(), durationMs, n);
        return new MartSyncResult("full", total.get(), durationMs, runAt);
    }

    /**
     * [lo, hi] 범위를 최대 n개의 서로소 서브레인지로 등폭 분할한다.
     */
    private List<long[]> splitRanges(long lo, long hi, int n) {
        long span = hi - lo + 1;
        long step = Math.max(1L, (span + n - 1) / n);
        List<long[]> ranges = new ArrayList<>();
        long cur = lo;
        while (cur <= hi) {
            long end = Math.min(cur + step - 1, hi);
            ranges.add(new long[]{cur, end});
            cur = end + 1;
        }
        return ranges;
    }

    private MartSyncResult doIncremental() {
        PmsMartSyncState state = findState();
        if (state == null || state.getLastSyncAt() == null) {
            log.info("Mart 동기화 이력 없음. full rebuild로 위임.");
            return doFull();
        }

        Instant runAt = Instant.now();
        long startMs = System.currentTimeMillis();
        int overlapSeconds = props.getScheduler().getWatermarkOverlapSeconds();
        Instant since = state.getLastSyncAt().minusSeconds(overlapSeconds);
        log.info("Mart incremental 시작: since={} (overlap={}s)", since, overlapSeconds);

        List<Long> changedIds = changeDetector.changedHeaderIds(since);
        if (changedIds.isEmpty()) {
            long durationMs = System.currentTimeMillis() - startMs;
            log.info("Mart incremental: 변경 없음. {} ms", durationMs);
            return new MartSyncResult("incremental", 0L, durationMs, runAt);
        }

        int batchSize = props.getRebuild().getBatchSize();
        long[] totalWritten = {0L};
        boolean countIndexPresent = countIndex.isPresent();

        reader.readIncremental(changedIds, runAt, batch -> {
            // Count Index present: upsert 전 oldDocs 조회 → diff 적용
            List<PmsBlMartDocument> oldDocs = countIndexPresent
                ? fetchOldDocs(batch)
                : List.of();

            long written = writer.upsertBatch(batch);
            entryWriter.ifPresent(w -> w.writeFromDocs(batch));

            if (countIndexPresent) {
                countIndex.get().applyChanges(oldDocs, batch);
            }

            totalWritten[0] += written;
        }, batchSize);

        long durationMs = System.currentTimeMillis() - startMs;
        saveState(PmsMartSyncState.builder()
            .id(STATE_ID)
            .lastFullRebuildAt(state.getLastFullRebuildAt())
            .lastSyncAt(runAt)
            .lastRowCount(totalWritten[0])
            .lastDurationMs(durationMs)
            .build());

        log.info("Mart incremental 완료: {} 건, {} ms", totalWritten[0], durationMs);
        return new MartSyncResult("incremental", totalWritten[0], durationMs, runAt);
    }

    /**
     * 증분 배치에 해당하는 blKey 목록으로 기존 Mart 문서를 조회한다.
     * Count Index applyChanges의 oldDocs 인자로 전달된다.
     */
    private List<PmsBlMartDocument> fetchOldDocs(List<PmsBlMartDocument> batch) {
        Set<String> blKeys = batch.stream()
            .map(PmsBlMartDocument::getId)
            .collect(Collectors.toSet());
        return mongoTemplate.find(
            Query.query(Criteria.where("_id").in(blKeys)),
            PmsBlMartDocument.class);
    }

    // ── sync state R/W ────────────────────────────────────────────────────────

    private PmsMartSyncState findState() {
        return mongoTemplate.findOne(
            Query.query(Criteria.where("_id").is(STATE_ID)),
            PmsMartSyncState.class);
    }

    private void saveState(PmsMartSyncState state) {
        mongoTemplate.save(state);
    }

    private PmsMartSyncState emptyState() {
        return PmsMartSyncState.builder().id(STATE_ID).build();
    }
}
