package com.freightos.pms.adapter.out.mart.sync;

/**
 * ETL 쿼리 공통 SQL 조각 상수.
 * page CTE 이후의 fl/dc/spine 본문은 full/incremental에서 동일하므로 여기에 격리한다.
 * PmsIdentityNameJoinSql 격리 패턴을 따른다 — 로직 없음, 순수 상수.
 */
final class PmsMartEtlSqlBody {

    private PmsMartEtlSqlBody() {}

    /** freight_line 집계 CTE (fl). page CTE 결과에 JOIN. */
    static final String FL_CTE = """
        fl AS (
            SELECT p.bl_type, p.bl_id,
                max(l.performance_dt) AS fi_perf,
                count(*)              AS fi_cnt,
                sum(CASE WHEN l.financial_doc_type='INVOICE' THEN l.local_amount ELSE 0 END) AS fi_inv_l,
                sum(CASE WHEN l.financial_doc_type='DEBIT'   THEN l.local_amount ELSE 0 END) AS fi_deb_l,
                sum(CASE WHEN l.financial_doc_type='PAYMENT' THEN l.local_amount ELSE 0 END) AS fi_pay_l,
                sum(CASE WHEN l.financial_doc_type='CREDIT'  THEN l.local_amount ELSE 0 END) AS fi_crd_l,
                sum(CASE WHEN l.financial_doc_type='INVOICE' THEN l.usd_amount   ELSE 0 END) AS fi_inv_u,
                sum(CASE WHEN l.financial_doc_type='DEBIT'   THEN l.usd_amount   ELSE 0 END) AS fi_deb_u,
                sum(CASE WHEN l.financial_doc_type='PAYMENT' THEN l.usd_amount   ELSE 0 END) AS fi_pay_u,
                sum(CASE WHEN l.financial_doc_type='CREDIT'  THEN l.usd_amount   ELSE 0 END) AS fi_crd_u,
                max(l.performance_dt) FILTER (WHERE l.tax_no IS NOT NULL) AS tx_perf,
                count(*)              FILTER (WHERE l.tax_no IS NOT NULL) AS tx_cnt,
                sum(CASE WHEN l.tax_no IS NOT NULL AND l.financial_doc_type='INVOICE' THEN l.local_amount ELSE 0 END) AS tx_inv_l,
                sum(CASE WHEN l.tax_no IS NOT NULL AND l.financial_doc_type='DEBIT'   THEN l.local_amount ELSE 0 END) AS tx_deb_l,
                sum(CASE WHEN l.tax_no IS NOT NULL AND l.financial_doc_type='PAYMENT' THEN l.local_amount ELSE 0 END) AS tx_pay_l,
                sum(CASE WHEN l.tax_no IS NOT NULL AND l.financial_doc_type='CREDIT'  THEN l.local_amount ELSE 0 END) AS tx_crd_l,
                sum(CASE WHEN l.tax_no IS NOT NULL AND l.financial_doc_type='INVOICE' THEN l.usd_amount   ELSE 0 END) AS tx_inv_u,
                sum(CASE WHEN l.tax_no IS NOT NULL AND l.financial_doc_type='DEBIT'   THEN l.usd_amount   ELSE 0 END) AS tx_deb_u,
                sum(CASE WHEN l.tax_no IS NOT NULL AND l.financial_doc_type='PAYMENT' THEN l.usd_amount   ELSE 0 END) AS tx_pay_u,
                sum(CASE WHEN l.tax_no IS NOT NULL AND l.financial_doc_type='CREDIT'  THEN l.usd_amount   ELSE 0 END) AS tx_crd_u,
                max(l.performance_dt) FILTER (WHERE l.slip_no IS NOT NULL) AS sl_perf,
                count(*)              FILTER (WHERE l.slip_no IS NOT NULL) AS sl_cnt,
                sum(CASE WHEN l.slip_no IS NOT NULL AND l.financial_doc_type='INVOICE' THEN l.local_amount ELSE 0 END) AS sl_inv_l,
                sum(CASE WHEN l.slip_no IS NOT NULL AND l.financial_doc_type='DEBIT'   THEN l.local_amount ELSE 0 END) AS sl_deb_l,
                sum(CASE WHEN l.slip_no IS NOT NULL AND l.financial_doc_type='PAYMENT' THEN l.local_amount ELSE 0 END) AS sl_pay_l,
                sum(CASE WHEN l.slip_no IS NOT NULL AND l.financial_doc_type='CREDIT'  THEN l.local_amount ELSE 0 END) AS sl_crd_l,
                sum(CASE WHEN l.slip_no IS NOT NULL AND l.financial_doc_type='INVOICE' THEN l.usd_amount   ELSE 0 END) AS sl_inv_u,
                sum(CASE WHEN l.slip_no IS NOT NULL AND l.financial_doc_type='DEBIT'   THEN l.usd_amount   ELSE 0 END) AS sl_deb_u,
                sum(CASE WHEN l.slip_no IS NOT NULL AND l.financial_doc_type='PAYMENT' THEN l.usd_amount   ELSE 0 END) AS sl_pay_u,
                sum(CASE WHEN l.slip_no IS NOT NULL AND l.financial_doc_type='CREDIT'  THEN l.usd_amount   ELSE 0 END) AS sl_crd_u
            FROM page p
            JOIN bms.freight_line l ON l.freight_header_id = p.freight_header_id
            GROUP BY p.bl_type, p.bl_id
        )""";

    /**
     * financial_document 집계 CTE (dc).
     * DISTINCT로 freight_line 1:N 팬아웃을 제거한 후 합산한다.
     */
    static final String DC_CTE = """
        dc AS (
            SELECT d.bl_type, d.bl_id,
                max(d.performance_dt) AS dc_perf,
                count(*)              AS dc_cnt,
                max(d.team_code)      AS dc_team,
                max(d.operator)       AS dc_operator,
                sum(CASE WHEN d.document_type='INVOICE' THEN d.local_total_amount ELSE 0 END) AS dc_inv_l,
                sum(CASE WHEN d.document_type='DEBIT'   THEN d.local_total_amount ELSE 0 END) AS dc_deb_l,
                sum(CASE WHEN d.document_type='PAYMENT' THEN d.local_total_amount ELSE 0 END) AS dc_pay_l,
                sum(CASE WHEN d.document_type='CREDIT'  THEN d.local_total_amount ELSE 0 END) AS dc_crd_l,
                sum(CASE WHEN d.document_type='INVOICE' THEN d.usd_total_amount   ELSE 0 END) AS dc_inv_u,
                sum(CASE WHEN d.document_type='DEBIT'   THEN d.usd_total_amount   ELSE 0 END) AS dc_deb_u,
                sum(CASE WHEN d.document_type='PAYMENT' THEN d.usd_total_amount   ELSE 0 END) AS dc_pay_u,
                sum(CASE WHEN d.document_type='CREDIT'  THEN d.usd_total_amount   ELSE 0 END) AS dc_crd_u
            FROM (
                SELECT DISTINCT p.bl_type, p.bl_id, fd.financial_document_id,
                       fd.document_type, fd.local_total_amount, fd.usd_total_amount,
                       fd.team_code, fd.operator, fd.performance_dt
                FROM page p
                JOIN bms.freight_line l ON l.freight_header_id = p.freight_header_id
                JOIN bms.financial_document fd ON fd.financial_document_id = l.financial_document_id
            ) d
            GROUP BY d.bl_type, d.bl_id
        )""";

    /** 외부 SELECT + JOIN 조각 (page CTE 이후). */
    static final String OUTER_SELECT_AND_JOINS = """
        SELECT
            p.bl_type, p.bl_id, p.freight_header_id,
            fl.fi_perf, fl.fi_cnt, fl.fi_inv_l, fl.fi_deb_l, fl.fi_pay_l, fl.fi_crd_l, fl.fi_inv_u, fl.fi_deb_u, fl.fi_pay_u, fl.fi_crd_u,
            fl.tx_perf, fl.tx_cnt, fl.tx_inv_l, fl.tx_deb_l, fl.tx_pay_l, fl.tx_crd_l, fl.tx_inv_u, fl.tx_deb_u, fl.tx_pay_u, fl.tx_crd_u,
            fl.sl_perf, fl.sl_cnt, fl.sl_inv_l, fl.sl_deb_l, fl.sl_pay_l, fl.sl_crd_l, fl.sl_inv_u, fl.sl_deb_u, fl.sl_pay_u, fl.sl_crd_u,
            dc.dc_perf, dc.dc_cnt, dc.dc_team, dc.dc_operator,
            dc.dc_inv_l, dc.dc_deb_l, dc.dc_pay_l, dc.dc_crd_l, dc.dc_inv_u, dc.dc_deb_u, dc.dc_pay_u, dc.dc_crd_u,
            hb.hbl_no, hb.mbl_no AS h_mbl_no, hb.job_div AS h_job_div, hb.bound AS h_bound, hb.etd AS h_etd, hb.eta AS h_eta,
            hb.pol_code AS h_pol, hb.pod_code AS h_pod, hb.sales_man_code, hb.incoterms, hb.sales_class, hb.team_code AS house_team_code,
            hb.pkg_qty, hb.cbm, hb.gross_weight_kg,
            sea.load_type AS sea_load_type, air.charge_weight_kg AS air_cw, tr.charge_weight_kg AS tr_cw, tr.load_type AS tr_load_type, nb.rton AS non_bl_rton,
            mb.mbl_no AS m_mbl_no, mb.job_div AS m_job_div, mb.bound AS m_bound, mb.etd AS m_etd, mb.eta AS m_eta, mb.pol_code AS m_pol, mb.pod_code AS m_pod,
            h.actual_customer_code, h.settle_partner_code, h.liner_code,
            ac.name AS acc_name, sp.name AS spc_name, ca.name AS lc_name, tmh.name AS house_team_name, tmd.name AS doc_team_name, au.user_eng_name AS sales_man_name
        FROM page p
        JOIN bms.freight_header h ON h.freight_header_id = p.freight_header_id
        LEFT JOIN fl ON fl.bl_type = p.bl_type AND fl.bl_id = p.bl_id
        LEFT JOIN dc ON dc.bl_type = p.bl_type AND dc.bl_id = p.bl_id
        LEFT JOIN fms.house_bl        hb  ON p.bl_type='HOUSE'  AND p.bl_id = hb.house_bl_id
        LEFT JOIN fms.house_bl_sea    sea ON sea.house_bl_id = hb.house_bl_id
        LEFT JOIN fms.house_bl_air    air ON air.house_bl_id = hb.house_bl_id
        LEFT JOIN fms.house_bl_truck  tr  ON tr.house_bl_id  = hb.house_bl_id
        LEFT JOIN fms.house_bl_non_bl nb  ON nb.house_bl_id  = hb.house_bl_id
        LEFT JOIN fms.master_bl       mb  ON p.bl_type='MASTER' AND p.bl_id = mb.master_bl_id
        LEFT JOIN admin.customer   ac  ON ac.customer_code = h.actual_customer_code AND ac.deleted_at IS NULL
        LEFT JOIN admin.customer   sp  ON sp.customer_code = h.settle_partner_code AND sp.deleted_at IS NULL
        LEFT JOIN admin.carrier    ca  ON ca.carrier_code  = h.liner_code           AND ca.deleted_at IS NULL
        LEFT JOIN admin.team       tmh ON tmh.team_code = hb.team_code
        LEFT JOIN admin.team       tmd ON tmd.team_code = dc.dc_team
        LEFT JOIN admin.admin_user au  ON au.username   = hb.sales_man_code
        ORDER BY p.freight_header_id
        """;
}
