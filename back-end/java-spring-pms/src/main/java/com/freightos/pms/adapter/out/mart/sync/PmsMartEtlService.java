package com.freightos.pms.adapter.out.mart.sync;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mart ETL 아웃바운드 포트 구현체.
 *
 * AtomicBoolean running으로 동시 실행을 차단한다(스케줄러·엔드포인트 공유).
 * sync state는 pms_mart_sync_state 컬렉션에 "pms_bl_mart" 고정 키로 저장된다.
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
                        total.addAndGet(docs.size());
                    });
                    log.debug("Mart full range [{}, {}] 완료: {} 건", rLo, rHi, read);
                }));
            }
            // 모든 워커 완료 대기 — 예외 발생 시 즉시 전파
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

        log.info("Mart full rebuild 완료: {} 건, {} ms (워커 {}개)", total.get(), durationMs, n);
        return new MartSyncResult("full", total.get(), durationMs, runAt);
    }

    /**
     * [lo, hi] 범위를 최대 n개의 서로소 서브레인지로 등폭 분할한다.
     * 마지막 레인지는 hi를 정확히 포함하도록 상한을 고정한다.
     *
     * freight_header_id 레인지를 워커별로 분리해 Mongo _id 충돌 없이 병렬 upsert할 수 있다.
     */
    private List<long[]> splitRanges(long lo, long hi, int n) {
        long span = hi - lo + 1;
        long step = Math.max(1L, (span + n - 1) / n); // 올림 나눗셈으로 최소 1
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
            // 첫 동기화: full로 위임
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

        reader.readIncremental(changedIds, runAt, batch -> {
            long written = writer.upsertBatch(batch);
            entryWriter.ifPresent(w -> w.writeFromDocs(batch));
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
