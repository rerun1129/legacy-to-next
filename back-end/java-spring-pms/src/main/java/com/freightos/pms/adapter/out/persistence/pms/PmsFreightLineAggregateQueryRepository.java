package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * freight_line 기반 B/L 집계 쿼리 레포지토리 (native SQL, single-query).
 *
 * 단일 쿼리 구조:
 *   WITH page AS ( <inner aggregate + count(*) OVER() + OFFSET/LIMIT> )
 *   SELECT <page cols> + <identity cols> + <cargo cols> + <name cols>
 *   FROM page
 *   LEFT JOIN fms.house_bl … fms.master_bl … admin.*
 *   ORDER BY p.perf DESC
 *
 * page CTE의 ≤pageSize 행에만 JOIN이 적용 — 전체 결과 집합 스캔 없음.
 * Phase-2 keyed lookup (cargoRepo/masterRepo/codeNameResolver) 완전 제거.
 *
 * ETD/ETA 필터 활성 시: B/L-driven FROM (fms.house_bl / fms.master_bl 인덱스 우선).
 * 실적일자·BMS 전용 필터만 활성 시: BMS-only FROM (FMS JOIN 완전 생략).
 * house-only 필터 활성 시 UNION ALL master 브랜치 생략.
 */
@Repository
@RequiredArgsConstructor
public class PmsFreightLineAggregateQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final PmsFreightLineSqlBuilder sqlBuilder;
    private final PmsFmsJoinDecider fmsJoinDecider;

    // ── 내부 집계 SELECT 고정 컬럼 (page CTE 내부) ────────────────────────────

    private static final String INNER_SELECT = """
        SELECT
          h.bl_type,
          h.bl_id,
          max(l.performance_dt)                                                 AS perf,
          min(h.actual_customer_code)                                           AS acc,
          min(h.settle_partner_code)                                            AS spc,
          min(h.liner_code)                                                     AS lc,
          sum(CASE WHEN l.financial_doc_type = 'INVOICE' THEN l.local_amount ELSE 0 END) AS inv_l,
          sum(CASE WHEN l.financial_doc_type = 'DEBIT'   THEN l.local_amount ELSE 0 END) AS deb_l,
          sum(CASE WHEN l.financial_doc_type = 'PAYMENT' THEN l.local_amount ELSE 0 END) AS pay_l,
          sum(CASE WHEN l.financial_doc_type = 'CREDIT'  THEN l.local_amount ELSE 0 END) AS crd_l,
          sum(CASE WHEN l.financial_doc_type = 'INVOICE' THEN l.usd_amount   ELSE 0 END) AS inv_u,
          sum(CASE WHEN l.financial_doc_type = 'DEBIT'   THEN l.usd_amount   ELSE 0 END) AS deb_u,
          sum(CASE WHEN l.financial_doc_type = 'PAYMENT' THEN l.usd_amount   ELSE 0 END) AS pay_u,
          sum(CASE WHEN l.financial_doc_type = 'CREDIT'  THEN l.usd_amount   ELSE 0 END) AS crd_u,
          count(*) OVER()                                                       AS total_count
        """;

    private static final String INNER_GROUP_ORDER = """
        GROUP BY h.bl_type, h.bl_id
        ORDER BY max(l.performance_dt) DESC
        OFFSET :off LIMIT :lim
        """;

    // ── 단일 쿼리 실행 ────────────────────────────────────────────────────────

    public Page<PmsRawBlRow> search(SearchPmsPerformanceCommand command, Pageable pageable) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("off", pageable.getOffset());
        params.addValue("lim", pageable.getPageSize());

        boolean fmsJoin = fmsJoinDecider.fmsJoinNeeded(command);
        boolean houseOnly = fmsJoin && sqlBuilder.houseOnlyActive(command);

        String from = fmsJoin
            ? sqlBuilder.blDrivenFrom(command, params, houseOnly)
            : sqlBuilder.bmsOnlyFrom();

        List<String> outerWhere = sqlBuilder.buildOuterWhere(command, params);
        String innerAggregate = buildInnerAggregate(from, outerWhere);
        String sql = sqlBuilder.wrapWithPageCteAndJoins(innerAggregate);

        long[] totalHolder = {0L};
        List<PmsRawBlRow> rows = new ArrayList<>();
        jdbc.query(sql, params, rs -> {
            if (rows.isEmpty()) {
                totalHolder[0] = rs.getLong("total_count");
            }
            rows.add(mapRow(rs));
        });

        return new PageImpl<>(rows, pageable, totalHolder[0]);
    }

    // ── 내부 집계 SQL 조립 ─────────────────────────────────────────────────────

    private String buildInnerAggregate(String from, List<String> outerWhere) {
        StringBuilder sb = new StringBuilder(INNER_SELECT);
        sb.append(from);
        if (!outerWhere.isEmpty()) {
            sb.append("WHERE ").append(String.join("\n  AND ", outerWhere)).append("\n");
        }
        sb.append(INNER_GROUP_ORDER);
        return sb.toString();
    }

    // ── 행 매핑 ───────────────────────────────────────────────────────────────

    /**
     * HOUSE 행: hb.* 컬럼(hblNo/jobDiv 등)이 채워짐, mb.* 컬럼은 null.
     * MASTER 행: mb.* 컬럼이 채워짐, hb.* 컬럼(hblNo/cargo 등)은 null.
     */
    private PmsRawBlRow mapRow(ResultSet rs) throws SQLException {
        boolean isHouse = "HOUSE".equals(rs.getString("bl_type"));
        Long blId = rs.getLong("bl_id");

        String houseBlNo = isHouse ? rs.getString("hbl_no") : null;
        String masterBlNo = isHouse ? rs.getString("h_mbl_no") : rs.getString("m_mbl_no");
        String jobDiv = isHouse ? rs.getString("h_job_div") : rs.getString("m_job_div");
        String bound  = isHouse ? rs.getString("h_bound")   : rs.getString("m_bound");
        String etd    = isHouse ? rs.getString("h_etd")     : rs.getString("m_etd");
        String eta    = isHouse ? rs.getString("h_eta")     : rs.getString("m_eta");
        String pol    = isHouse ? rs.getString("h_pol")     : rs.getString("m_pol");
        String pod    = isHouse ? rs.getString("h_pod")     : rs.getString("m_pod");
        String salesManCode = isHouse ? rs.getString("sales_man_code") : null;
        String incoterms    = isHouse ? rs.getString("incoterms")      : null;
        String teamCode     = isHouse ? rs.getString("team_code")       : null;
        Long   houseBlId    = isHouse ? blId                            : null;

        return new PmsRawBlRow(
            rs.getString("bl_type"), blId,
            houseBlNo, masterBlNo, jobDiv, bound, etd, eta,
            rs.getString("perf"),
            rs.getString("acc"), rs.getString("spc"), rs.getString("lc"),
            pol, pod, salesManCode, incoterms, houseBlId,
            teamCode,
            null,   // operator — freight_line 경로에서는 사용 안 함
            nvl(rs, "inv_l"), nvl(rs, "deb_l"), nvl(rs, "pay_l"), nvl(rs, "crd_l"),
            nvl(rs, "inv_u"), nvl(rs, "deb_u"), nvl(rs, "pay_u"), nvl(rs, "crd_u"),
            // cargo (HOUSE만 의미 있음)
            isHouse ? rs.getObject("pkg_qty", Integer.class) : null,
            isHouse ? rs.getBigDecimal("cbm")                : null,
            isHouse ? rs.getBigDecimal("gross_weight_kg")    : null,
            isHouse ? rs.getString("sea_load_type")          : null,
            isHouse ? rs.getBigDecimal("air_cw")             : null,
            isHouse ? rs.getBigDecimal("tr_cw")              : null,
            isHouse ? rs.getString("tr_load_type")           : null,
            isHouse ? rs.getBigDecimal("rton")               : null,
            // 이름
            rs.getString("acc_name"), rs.getString("spc_name"), rs.getString("lc_name"),
            isHouse ? rs.getString("team_name")      : null,
            isHouse ? rs.getString("sales_man_name") : null
        );
    }

    private BigDecimal nvl(ResultSet rs, String col) throws SQLException {
        BigDecimal v = rs.getBigDecimal(col);
        return v != null ? v : BigDecimal.ZERO;
    }
}
