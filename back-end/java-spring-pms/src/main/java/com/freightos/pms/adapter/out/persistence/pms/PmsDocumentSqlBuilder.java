package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * financial_document 기반 집계 native SQL 조각 빌더.
 *
 * document 경로에서는 FMS 조인 시 freight_line-driven (LEFT JOIN house/master).
 * ETD/ETA는 OR 조합으로 각 테이블 인덱스를 활용.
 * teamCode/operator는 financial_document 컬럼에서 직접 필터링 (FMS JOIN 트리거 아님).
 */
@Component
public class PmsDocumentSqlBuilder {

    // ── document 자체 필터 ────────────────────────────────────────────────────

    /**
     * financial_document(fd) + freight_header(h) 조건을 수집.
     * params에 값도 함께 바인딩.
     */
    public void addDocumentPredicates(SearchPmsPerformanceCommand c,
                                      List<String> where, MapSqlParameterSource params) {
        if (hasValue(c.performanceDtFrom())) {
            where.add("fd.performance_dt >= :perf_from");
            params.addValue("perf_from", c.performanceDtFrom());
        }
        if (hasValue(c.performanceDtTo())) {
            where.add("fd.performance_dt <= :perf_to");
            params.addValue("perf_to", c.performanceDtTo());
        }
        if (hasValue(c.teamCode())) {
            where.add("fd.team_code = :team_code");
            params.addValue("team_code", c.teamCode());
        }
        if (hasValue(c.operator())) {
            where.add("fd.operator = :operator");
            params.addValue("operator", c.operator());
        }
        if (c.documentTypes() != null && !c.documentTypes().isEmpty()) {
            where.add("fd.document_type IN (:doc_types)");
            params.addValue("doc_types", c.documentTypes());
        }
        if (hasValue(c.documentStatus())) {
            where.add("fd.document_status = :doc_status");
            params.addValue("doc_status", c.documentStatus());
        }
        if (hasValue(c.documentNoLike())) {
            where.add("fd.document_no ILIKE '%' || :doc_no || '%'");
            params.addValue("doc_no", c.documentNoLike());
        }
        if (hasValue(c.documentDtFrom())) {
            where.add("fd.document_dt >= :doc_dt_from");
            params.addValue("doc_dt_from", c.documentDtFrom());
        }
        if (hasValue(c.documentDtTo())) {
            where.add("fd.document_dt <= :doc_dt_to");
            params.addValue("doc_dt_to", c.documentDtTo());
        }
        if (hasValue(c.groupFinancialNo())) {
            where.add("fd.group_financial_no ILIKE '%' || :grp_no || '%'");
            params.addValue("grp_no", c.groupFinancialNo());
        }
        if (hasValue(c.grouped())) {
            where.add("Y".equalsIgnoreCase(c.grouped())
                ? "fd.group_financial_no IS NOT NULL"
                : "fd.group_financial_no IS NULL");
        }
    }

    /**
     * freight_header(h) 필터 조건 수집.
     */
    public void addHeaderPredicates(SearchPmsPerformanceCommand c,
                                    List<String> where, MapSqlParameterSource params) {
        if (hasValue(c.actualCustomerCode())) {
            where.add("h.actual_customer_code = :acc");
            params.addValue("acc", c.actualCustomerCode());
        }
        if (hasValue(c.settlePartnerCode())) {
            where.add("h.settle_partner_code = :spc");
            params.addValue("spc", c.settlePartnerCode());
        }
        if (hasValue(c.carrierCode())) {
            where.add("h.liner_code = :liner_code");
            params.addValue("liner_code", c.carrierCode());
        }
    }

    // ── FMS LEFT JOIN 조각 ────────────────────────────────────────────────────

    /**
     * FMS 조인 SQL 조각 생성. document 경로에서는 LEFT JOIN house/master.
     * ETD/ETA 범위 + jobDiv/bound/mblNo/portCode 만 FMS 필터.
     * (teamCode/operator는 financial_document에서 직접 필터링 — 여기 포함 안 함)
     *
     * @return LEFT JOIN 절 문자열 (빈 문자열이면 FMS JOIN 없음)
     */
    public String buildFmsJoinFragment(SearchPmsPerformanceCommand c,
                                       List<String> where, MapSqlParameterSource params) {
        StringBuilder joinSql = new StringBuilder();
        joinSql.append(
            "LEFT JOIN fms.house_bl hb ON h.bl_type = 'HOUSE' AND h.bl_id = hb.house_bl_id\n");
        joinSql.append(
            "LEFT JOIN fms.master_bl mb ON h.bl_type = 'MASTER' AND h.bl_id = mb.master_bl_id\n");

        // ETD/ETA 범위 — OR 결합으로 각 테이블 인덱스 활용 (LEFT JOIN 행 보존 필요)
        String dateKind = c.dateKind();
        if (hasValue(dateKind) && (hasValue(c.dateFrom()) || hasValue(c.dateTo()))) {
            String hCol = "ETD".equals(dateKind) ? "hb.etd" : "hb.eta";
            String mCol = "ETD".equals(dateKind) ? "mb.etd" : "mb.eta";
            if (hasValue(c.dateFrom())) {
                where.add("(" + hCol + " >= :fms_date_from OR " + mCol + " >= :fms_date_from)");
                params.addValue("fms_date_from", c.dateFrom());
            }
            if (hasValue(c.dateTo())) {
                where.add("(" + hCol + " <= :fms_date_to OR " + mCol + " <= :fms_date_to)");
                params.addValue("fms_date_to", c.dateTo());
            }
        }
        if (hasValue(c.jobDiv())) {
            where.add("(hb.job_div = :fms_job_div OR mb.job_div = :fms_job_div)");
            params.addValue("fms_job_div", c.jobDiv());
        }
        if (hasValue(c.bound())) {
            where.add("(hb.bound = :fms_bound OR mb.bound = :fms_bound)");
            params.addValue("fms_bound", c.bound());
        }
        if (hasValue(c.mblNo())) {
            where.add(
                "(hb.mbl_no LIKE :fms_mbl_no || '%' OR mb.mbl_no LIKE :fms_mbl_no || '%')");
            params.addValue("fms_mbl_no", c.mblNo());
        }
        if (hasValue(c.portKind()) && hasValue(c.portCode())) {
            String hCol = "POL".equals(c.portKind()) ? "hb.pol_code" : "hb.pod_code";
            String mCol = "POL".equals(c.portKind()) ? "mb.pol_code" : "mb.pod_code";
            where.add("(" + hCol + " = :fms_port_code OR " + mCol + " = :fms_port_code)");
            params.addValue("fms_port_code", c.portCode());
        }

        return joinSql.toString();
    }

    // ── page + amounts CTE 단일 쿼리 조립 ────────────────────────────────────

    // document 경로 전용 outer SELECT.
    // - 8개 금액 컬럼은 amounts CTE에서 읽는다 (page CTE에는 sum이 없음).
    // - team_code/team_name은 page CTE(fd.team_code)에서 읽는다 (hb.team_code 아님).
    // freight_line 경로는 p.inv_l…(page CTE inline sum)·hb.team_code를 그대로 유지.
    private static final String DOCUMENT_OUTER_SELECT = """
        SELECT
          p.bl_type, p.bl_id, p.perf, p.acc, p.spc, p.lc,
          amounts.inv_l AS inv_l, amounts.deb_l AS deb_l,
          amounts.pay_l AS pay_l, amounts.crd_l AS crd_l,
          amounts.inv_u AS inv_u, amounts.deb_u AS deb_u,
          amounts.pay_u AS pay_u, amounts.crd_u AS crd_u, p.total_count,
          hb.hbl_no, hb.mbl_no   AS h_mbl_no, hb.job_div  AS h_job_div,
          hb.bound AS h_bound, hb.etd AS h_etd, hb.eta AS h_eta,
          hb.pol_code AS h_pol,  hb.pod_code AS h_pod,
          hb.sales_man_code, hb.incoterms,
          p.team_code AS team_code,
          p.operator  AS operator,
          hb.pkg_qty, hb.cbm, hb.gross_weight_kg,
          sea.load_type AS sea_load_type, air.charge_weight_kg AS air_cw,
          tr.charge_weight_kg AS tr_cw, tr.load_type AS tr_load_type, nb.rton,
          mb.mbl_no   AS m_mbl_no, mb.job_div  AS m_job_div,
          mb.bound AS m_bound, mb.etd AS m_etd, mb.eta AS m_eta,
          mb.pol_code AS m_pol,  mb.pod_code AS m_pod,
          ac.name AS acc_name, sp.name AS spc_name, ca.name AS lc_name,
          tm.name AS team_name, au.user_eng_name AS sales_man_name
        FROM page p
        """;

    // document 경로 전용 name JOIN 조각.
    // team은 financial_document의 team_code(= p.team_code)로 조인한다.
    private static final String DOCUMENT_NAME_JOINS = """
        LEFT JOIN admin.customer   ac ON ac.customer_code = p.acc AND ac.deleted_at IS NULL
        LEFT JOIN admin.customer   sp ON sp.customer_code = p.spc AND sp.deleted_at IS NULL
        LEFT JOIN admin.carrier    ca ON ca.carrier_code  = p.lc  AND ca.deleted_at IS NULL
        LEFT JOIN admin.team       tm ON tm.team_code = p.team_code
        LEFT JOIN admin.admin_user au ON au.username  = hb.sales_man_code
        """;

    /**
     * page CTE + amounts CTE + identity/name JOIN 단일 쿼리를 조립한다.
     *
     * amounts CTE는 DISTINCT (bl_type, bl_id, financial_document_id, document_type, amounts)로
     * freight_line 1:N 팬아웃을 제거한다. page CTE와 JOIN하여 페이지 범위만 집계.
     *
     * outer SELECT는 document 경로 전용(DOCUMENT_OUTER_SELECT)을 사용한다.
     * freight_line 경로는 PmsIdentityNameJoinSql.wrapWithPageCte()를 그대로 유지.
     *
     * @param pageInner  page CTE 내부 집계 SQL (GROUP BY + ORDER BY + OFFSET/LIMIT 포함)
     * @param amountWhere amounts CTE의 WHERE 조건 목록 (pageInner과 동일 필터 파라미터 재사용)
     */
    public String buildSingleQuery(String pageInner, List<String> amountWhere) {
        String amountsCte = buildAmountsCte(amountWhere);
        return "WITH page AS (\n" + pageInner + "\n),\namounts AS (\n" + amountsCte + "\n)\n"
            + DOCUMENT_OUTER_SELECT
            + "JOIN amounts ON amounts.bl_type = p.bl_type AND amounts.bl_id = p.bl_id\n"
            + PmsIdentityNameJoinSql.IDENTITY_JOINS
            + DOCUMENT_NAME_JOINS
            + "ORDER BY p.bl_id DESC, p.bl_type\n";
    }

    /**
     * amounts CTE SQL 조각. DISTINCT로 팬아웃 방지, page CTE에 JOIN하여 페이지 범위만 처리.
     */
    private String buildAmountsCte(List<String> amountWhere) {
        StringBuilder sb = new StringBuilder("""
            SELECT d.bl_type, d.bl_id,
              sum(CASE WHEN d.document_type = 'INVOICE' THEN d.local_total_amount ELSE 0 END) AS inv_l,
              sum(CASE WHEN d.document_type = 'DEBIT'   THEN d.local_total_amount ELSE 0 END) AS deb_l,
              sum(CASE WHEN d.document_type = 'PAYMENT' THEN d.local_total_amount ELSE 0 END) AS pay_l,
              sum(CASE WHEN d.document_type = 'CREDIT'  THEN d.local_total_amount ELSE 0 END) AS crd_l,
              sum(CASE WHEN d.document_type = 'INVOICE' THEN d.usd_total_amount   ELSE 0 END) AS inv_u,
              sum(CASE WHEN d.document_type = 'DEBIT'   THEN d.usd_total_amount   ELSE 0 END) AS deb_u,
              sum(CASE WHEN d.document_type = 'PAYMENT' THEN d.usd_total_amount   ELSE 0 END) AS pay_u,
              sum(CASE WHEN d.document_type = 'CREDIT'  THEN d.usd_total_amount   ELSE 0 END) AS crd_u
            FROM (
              SELECT DISTINCT h.bl_type, h.bl_id, fd.financial_document_id,
                fd.document_type, fd.local_total_amount, fd.usd_total_amount
              FROM bms.financial_document fd
              JOIN bms.freight_line l ON l.financial_document_id = fd.financial_document_id
              JOIN bms.freight_header h ON h.freight_header_id = l.freight_header_id
              JOIN page pg ON pg.bl_type = h.bl_type AND pg.bl_id = h.bl_id
            """);
        if (!amountWhere.isEmpty()) {
            sb.append("  WHERE ").append(String.join("\n  AND ", amountWhere)).append("\n");
        }
        sb.append(") d\nGROUP BY d.bl_type, d.bl_id\n");
        return sb.toString();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}
