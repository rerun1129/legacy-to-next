package com.freightos.pms.adapter.out.mart.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

/**
 * 특정 시각 이후 변경된 freight_header_id 목록을 탐지한다.
 * freight_header/freight_line/financial_document/house_bl/master_bl 중
 * 어느 하나라도 변경됐으면 포함한다.
 * <p>
 * 테이블별 분기 UNION으로 구성하여 각 테이블의 updated_at 인덱스 레인지 스캔을 사용한다.
 * (OR-join 단일 쿼리는 이종 테이블 OR을 인덱스로 풀지 못해 거대 조인 전체 materialize 발생)
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class PmsMartChangeDetector {

    private final NamedParameterJdbcTemplate jdbc;

    // UNION(ALL 아님)으로 5개 분기 결과 중복 제거 — 원본 SELECT DISTINCT 역할 대체
    private static final String CHANGE_SQL = """
        SELECT freight_header_id FROM bms.freight_header WHERE updated_at > :since
        UNION
        SELECT freight_header_id FROM bms.freight_line WHERE updated_at > :since
        UNION
        SELECT l.freight_header_id FROM bms.financial_document fd
          JOIN bms.freight_line l ON l.financial_document_id = fd.financial_document_id
          WHERE fd.updated_at > :since
        UNION
        SELECT h.freight_header_id FROM fms.house_bl hb
          JOIN bms.freight_header h ON h.bl_type = 'HOUSE' AND h.bl_id = hb.house_bl_id
          WHERE hb.updated_at > :since
        UNION
        SELECT h.freight_header_id FROM fms.master_bl mb
          JOIN bms.freight_header h ON h.bl_type = 'MASTER' AND h.bl_id = mb.master_bl_id
          WHERE mb.updated_at > :since
        """;

    /**
     * since 시각 이후 변경된 freight_header_id 목록을 반환한다.
     *
     * @param since 이 시각 이후(exclusive) 변경된 헤더만 포함
     */
    List<Long> changedHeaderIds(Instant since) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("since", Timestamp.from(since));
        return jdbc.queryForList(CHANGE_SQL, params, Long.class);
    }
}
