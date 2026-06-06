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
 * financial_document 기반 B/L 집계 쿼리 레포지토리 (DOCUMENT_CREATED basis, single-query).
 *
 * 단일 쿼리 구조:
 *   WITH page AS ( <membership+sort+window count: GROUP BY bl_type,bl_id + ORDER BY + OFFSET/LIMIT> ),
 *   amounts AS (
 *     SELECT DISTINCT(bl_type,bl_id,doc_id,doc_type,amounts) → GROUP BY bl_type,bl_id
 *     FROM bms.financial_document … JOIN page  -- page 범위만 처리
 *   )
 *   SELECT page.* + amounts.8sums + identity + cargo + name
 *   FROM page JOIN amounts … LEFT JOIN fms.* … LEFT JOIN admin.*
 *   ORDER BY page.perf DESC
 *
 * DISTINCT inner SELECT가 1:N freight_line 팬아웃을 방지한다 (기존 전략 유지).
 * Phase-2 keyed lookup (= ANY(?) 왕복) 완전 제거.
 */
@Repository
@RequiredArgsConstructor
public class PmsDocumentAggregateQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final PmsDocumentSqlBuilder sqlBuilder;
    private final PmsFmsJoinDecider fmsJoinDecider;

    // ── page CTE 내부 SELECT (membership + sort + window count) ──────────────

    private static final String PAGE_INNER_SELECT = """
        SELECT
          h.bl_type,
          h.bl_id,
          max(fd.performance_dt)       AS perf,
          max(h.actual_customer_code)  AS acc,
          max(h.settle_partner_code)   AS spc,
          max(h.liner_code)            AS lc,
          max(fd.team_code)            AS team_code,
          max(fd.operator)             AS operator,
          count(*) OVER()              AS total_count
        """;

    private static final String PAGE_FROM = """
        FROM bms.financial_document fd
        JOIN bms.freight_line l ON l.financial_document_id = fd.financial_document_id
        JOIN bms.freight_header h ON h.freight_header_id = l.freight_header_id
        """;

    private static final String PAGE_GROUP_ORDER = """
        GROUP BY h.bl_type, h.bl_id
        ORDER BY max(fd.performance_dt) DESC
        OFFSET :off LIMIT :lim
        """;

    // ── 단일 쿼리 실행 ────────────────────────────────────────────────────────

    public Page<PmsRawBlRow> search(SearchPmsPerformanceCommand command, Pageable pageable) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("off", pageable.getOffset());
        params.addValue("lim", pageable.getPageSize());

        List<String> pageWhere = new ArrayList<>();
        sqlBuilder.addDocumentPredicates(command, pageWhere, params);
        sqlBuilder.addHeaderPredicates(command, pageWhere, params);

        boolean fmsJoin = fmsJoinDecider.fmsJoinNeededForDocument(command);
        String fmsJoinSql = fmsJoin ? sqlBuilder.buildFmsJoinFragment(command, pageWhere, params) : "";

        String pageInner = buildPageInner(fmsJoinSql, pageWhere);

        // amounts CTE 필터는 page CTE와 동일 술어 재사용 (파라미터 공유)
        // amounts DISTINCT inner는 page JOIN으로 범위 제한 — 추가 WHERE는 선택적
        List<String> amountWhere = buildAmountWhere(command);
        String sql = sqlBuilder.buildSingleQuery(pageInner, amountWhere);

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

    // ── SQL 조립 ──────────────────────────────────────────────────────────────

    private String buildPageInner(String fmsJoinSql, List<String> pageWhere) {
        StringBuilder sb = new StringBuilder(PAGE_INNER_SELECT);
        sb.append(PAGE_FROM);
        sb.append(fmsJoinSql);
        if (!pageWhere.isEmpty()) {
            sb.append("WHERE ").append(String.join("\n  AND ", pageWhere)).append("\n");
        }
        sb.append(PAGE_GROUP_ORDER);
        return sb.toString();
    }

    /**
     * amounts CTE DISTINCT inner에 적용할 필터. document_type IN 같은 집계에 영향을 주는
     * 필터만 포함한다 (page JOIN으로 범위는 이미 제한되므로 날짜/페이징은 불필요).
     *
     * 현재 구현: amounts는 페이지 범위 내 모든 서류를 집계하므로 추가 WHERE 없음.
     * 필요 시 document_type 필터를 명시적으로 추가해 amounts 범위를 좁힐 수 있다.
     */
    private List<String> buildAmountWhere(SearchPmsPerformanceCommand command) {
        return List.of();
    }

    // ── 행 매핑 ───────────────────────────────────────────────────────────────

    /**
     * HOUSE 행: hb.* 컬럼이 채워짐. MASTER 행: mb.* 컬럼이 채워짐.
     * teamCode/operator는 page CTE (fd.team_code/operator) 에서 직접 읽음.
     * cargo 필드: document 경로에서는 house_bl 확장 테이블 LEFT JOIN으로 제공.
     */
    private PmsRawBlRow mapRow(ResultSet rs) throws SQLException {
        boolean isHouse = "HOUSE".equals(rs.getString("bl_type"));
        Long blId = rs.getLong("bl_id");

        String houseBlNo = isHouse ? rs.getString("hbl_no")    : null;
        String masterBlNo = isHouse ? rs.getString("h_mbl_no") : rs.getString("m_mbl_no");
        String jobDiv = isHouse ? rs.getString("h_job_div") : rs.getString("m_job_div");
        String bound  = isHouse ? rs.getString("h_bound")   : rs.getString("m_bound");
        String etd    = isHouse ? rs.getString("h_etd")     : rs.getString("m_etd");
        String eta    = isHouse ? rs.getString("h_eta")     : rs.getString("m_eta");
        String pol    = isHouse ? rs.getString("h_pol")     : rs.getString("m_pol");
        String pod    = isHouse ? rs.getString("h_pod")     : rs.getString("m_pod");
        String salesManCode = isHouse ? rs.getString("sales_man_code") : null;
        String incoterms    = isHouse ? rs.getString("incoterms")      : null;
        Long   houseBlId    = isHouse ? blId                            : null;

        // document 경로: teamCode/operator는 page CTE (financial_document) 출처
        String teamCode = rs.getString("team_code");
        String operator = rs.getString("operator");

        return new PmsRawBlRow(
            rs.getString("bl_type"), blId,
            houseBlNo, masterBlNo, jobDiv, bound, etd, eta,
            rs.getString("perf"),
            rs.getString("acc"), rs.getString("spc"), rs.getString("lc"),
            pol, pod, salesManCode, incoterms, houseBlId,
            teamCode, operator,
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
