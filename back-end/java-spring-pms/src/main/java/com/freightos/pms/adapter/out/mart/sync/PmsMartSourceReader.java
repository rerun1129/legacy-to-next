package com.freightos.pms.adapter.out.mart.sync;

import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * OLTP에서 Mart 문서를 배치 단위로 읽어 sink에 전달하는 리더.
 *
 * full: keyset 기반 커서 루프 (freight_header_id > :lastId, 빈 배치면 종료).
 *       ResultSet에서 freight_header_id를 직접 추출하여 keyset을 전진한다.
 * incremental: 변경된 freight_header_id 목록을 batchSize씩 IN 조회.
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class PmsMartSourceReader {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * OLTP 전체를 keyset 루프로 읽어 batchSink에 배치 단위로 전달한다.
     *
     * @param runAt     동기화 시작 시각 (모든 행의 martUpdatedAt에 공통 적용)
     * @param batchSink 배치 단위 처리 콜백
     * @param batchSize 한 번에 읽을 최대 행 수
     */
    void readFull(Instant runAt, Consumer<List<PmsBlMartDocument>> batchSink, int batchSize) {
        PmsMartSourceRowMapper rowMapper = new PmsMartSourceRowMapper(runAt);
        String sql = PmsMartEtlSql.fullBatchSql();

        long lastId = 0L;
        while (true) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lastId", lastId)
                .addValue("batch", batchSize);

            // freight_header_id를 keyset 전진에 사용하기 위해 ResultSet에서 직접 추출
            long[] maxHeaderId = {0L};
            List<PmsBlMartDocument> batch = new ArrayList<>();
            jdbc.query(sql, params, rs -> {
                try {
                    long headerId = rs.getLong("freight_header_id");
                    if (headerId > maxHeaderId[0]) {
                        maxHeaderId[0] = headerId;
                    }
                    batch.add(rowMapper.mapRow(rs));
                } catch (java.sql.SQLException e) {
                    throw new RuntimeException("Mart ETL full 행 매핑 실패: " + e.getMessage(), e);
                }
            });

            if (batch.isEmpty()) {
                break;
            }
            batchSink.accept(batch);
            lastId = maxHeaderId[0];
        }
    }

    /**
     * freight_header의 min/max ID 경계를 반환한다.
     * 테이블이 비어 있으면 null을 반환한다.
     *
     * @return [lo, hi] 또는 null (빈 테이블)
     */
    long[] headerIdBounds() {
        Long lo = jdbc.queryForObject(
            "SELECT min(freight_header_id) FROM bms.freight_header",
            new MapSqlParameterSource(), Long.class);
        if (lo == null) {
            return null;
        }
        Long hi = jdbc.queryForObject(
            "SELECT max(freight_header_id) FROM bms.freight_header",
            new MapSqlParameterSource(), Long.class);
        return new long[]{lo, hi};
    }

    /**
     * freight_header_id가 [loId, hiId] 범위에 속하는 행을 keyset 루프로 읽어 sink에 전달한다.
     * full rebuild 레인지 워커에서 호출된다.
     *
     * @param loId      레인지 하한 (포함)
     * @param hiId      레인지 상한 (포함)
     * @param batchSize 한 번에 읽을 최대 행 수
     * @param runAt     동기화 시작 시각
     * @param batchSink 배치 단위 처리 콜백
     * @return 이 레인지에서 읽은 총 행 수
     */
    long readRange(long loId, long hiId, int batchSize, Instant runAt,
                   Consumer<List<PmsBlMartDocument>> batchSink) {
        PmsMartSourceRowMapper rowMapper = new PmsMartSourceRowMapper(runAt);
        String sql = PmsMartEtlSql.rangeBatchSql();

        long lastId = loId - 1;
        long totalRead = 0L;
        while (true) {
            MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lastId", lastId)
                .addValue("hiId", hiId)
                .addValue("batch", batchSize);

            long[] maxHeaderId = {0L};
            List<PmsBlMartDocument> batch = new ArrayList<>();
            jdbc.query(sql, params, rs -> {
                try {
                    long headerId = rs.getLong("freight_header_id");
                    if (headerId > maxHeaderId[0]) {
                        maxHeaderId[0] = headerId;
                    }
                    batch.add(rowMapper.mapRow(rs));
                } catch (java.sql.SQLException e) {
                    throw new RuntimeException("Mart ETL range 행 매핑 실패: " + e.getMessage(), e);
                }
            });

            if (batch.isEmpty()) {
                break;
            }
            batchSink.accept(batch);
            totalRead += batch.size();
            lastId = maxHeaderId[0];
        }
        return totalRead;
    }

    /**
     * 변경된 freight_header_id 목록을 batchSize씩 잘라 IN 조회한다.
     *
     * @param headerIds 증분 변경 헤더 ID 목록
     * @param runAt     동기화 시작 시각
     * @param batchSink 배치 단위 처리 콜백
     * @param batchSize 한 번에 처리할 최대 IN 파라미터 수
     */
    void readIncremental(List<Long> headerIds, Instant runAt,
                         Consumer<List<PmsBlMartDocument>> batchSink, int batchSize) {
        if (headerIds.isEmpty()) {
            return;
        }
        PmsMartSourceRowMapper rowMapper = new PmsMartSourceRowMapper(runAt);
        String sql = PmsMartEtlSql.incrementalBatchSql();

        for (int from = 0; from < headerIds.size(); from += batchSize) {
            List<Long> slice = headerIds.subList(from, Math.min(from + batchSize, headerIds.size()));
            MapSqlParameterSource params = new MapSqlParameterSource().addValue("ids", slice);

            List<PmsBlMartDocument> batch = new ArrayList<>();
            jdbc.query(sql, params, rs -> {
                try {
                    batch.add(rowMapper.mapRow(rs));
                } catch (java.sql.SQLException e) {
                    throw new RuntimeException("Mart ETL incremental 행 매핑 실패: " + e.getMessage(), e);
                }
            });

            if (!batch.isEmpty()) {
                batchSink.accept(batch);
            }
        }
    }
}
