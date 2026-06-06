package com.freightos.pms.adapter.out.persistence.pms;

/**
 * page CTE 기반 외부 SELECT / JOIN 상수 저장소.
 * freight_line 경로와 document 경로가 공유하는 identity + cargo + name LEFT JOIN 조각.
 * 단순 상수 모음 — 로직 없음.
 */
final class PmsIdentityNameJoinSql {

    private PmsIdentityNameJoinSql() {}

    /** page CTE 외부 SELECT 컬럼 목록. */
    static final String PAGE_SELECT = """
        SELECT
          p.bl_type, p.bl_id, p.perf, p.acc, p.spc, p.lc,
          p.inv_l, p.deb_l, p.pay_l, p.crd_l,
          p.inv_u, p.deb_u, p.pay_u, p.crd_u, p.total_count,
          hb.hbl_no, hb.mbl_no   AS h_mbl_no, hb.job_div  AS h_job_div,
          hb.bound AS h_bound, hb.etd AS h_etd, hb.eta AS h_eta,
          hb.pol_code AS h_pol,  hb.pod_code AS h_pod,
          hb.sales_man_code, hb.incoterms, hb.team_code,
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

    /** FMS identity + cargo LEFT JOIN 조각 (HOUSE→확장 테이블, MASTER). */
    static final String IDENTITY_JOINS = """
        LEFT JOIN fms.house_bl        hb  ON p.bl_type = 'HOUSE'  AND p.bl_id = hb.house_bl_id
        LEFT JOIN fms.house_bl_sea    sea ON sea.house_bl_id = hb.house_bl_id
        LEFT JOIN fms.house_bl_air    air ON air.house_bl_id = hb.house_bl_id
        LEFT JOIN fms.house_bl_truck  tr  ON tr.house_bl_id  = hb.house_bl_id
        LEFT JOIN fms.house_bl_non_bl nb  ON nb.house_bl_id  = hb.house_bl_id
        LEFT JOIN fms.master_bl       mb  ON p.bl_type = 'MASTER' AND p.bl_id = mb.master_bl_id
        """;

    /** admin name LEFT JOIN 조각. */
    static final String NAME_JOINS = """
        LEFT JOIN admin.customer   ac ON ac.customer_code = p.acc AND ac.deleted_at IS NULL
        LEFT JOIN admin.customer   sp ON sp.customer_code = p.spc AND sp.deleted_at IS NULL
        LEFT JOIN admin.carrier    ca ON ca.carrier_code  = p.lc  AND ca.deleted_at IS NULL
        LEFT JOIN admin.team       tm ON tm.team_code = hb.team_code
        LEFT JOIN admin.admin_user au ON au.username  = hb.sales_man_code
        """;

    /** page CTE 래퍼 + 외부 SELECT/JOIN + 정렬 조합. */
    static String wrapWithPageCte(String innerAggregate) {
        return "WITH page AS (\n" + innerAggregate + "\n)\n"
            + PAGE_SELECT + IDENTITY_JOINS + NAME_JOINS + "ORDER BY p.bl_id DESC, p.bl_type\n";
    }
}
