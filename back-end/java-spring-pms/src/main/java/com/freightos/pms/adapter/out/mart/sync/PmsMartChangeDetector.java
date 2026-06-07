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
 */
@Component
@ConditionalOnProperty(prefix = "pms.mart", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
class PmsMartChangeDetector {

    private final NamedParameterJdbcTemplate jdbc;

    private static final String CHANGE_SQL = """
        SELECT DISTINCT h.freight_header_id
        FROM bms.freight_header h
        LEFT JOIN bms.freight_line l ON l.freight_header_id = h.freight_header_id
        LEFT JOIN bms.financial_document fd ON fd.financial_document_id = l.financial_document_id
        LEFT JOIN fms.house_bl  hb ON h.bl_type='HOUSE'  AND h.bl_id = hb.house_bl_id
        LEFT JOIN fms.master_bl mb ON h.bl_type='MASTER' AND h.bl_id = mb.master_bl_id
        WHERE h.updated_at > :since OR l.updated_at > :since OR fd.updated_at > :since
           OR hb.updated_at > :since OR mb.updated_at > :since
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
