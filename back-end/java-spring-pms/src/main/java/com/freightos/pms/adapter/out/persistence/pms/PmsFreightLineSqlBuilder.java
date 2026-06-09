package com.freightos.pms.adapter.out.persistence.pms;

import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * freight_line 기반 집계 native SQL 조각 빌더.
 *
 * 두 가지 FROM 형태를 생성한다:
 * (A) BMS-only: FMS 조인 불필요 → FROM bms.freight_line … bms.freight_header
 * (B) B/L-driven: FMS 날짜·필터 활성 → UNION ALL CTE로 house/master 먼저 필터링 후 BMS JOIN.
 *
 * house-only 필터(hblNo/salesManCode/incoterms/salesClass/teamCode) 활성 시
 * UNION ALL master 브랜치를 완전히 생략한다 (master 행이 조건을 충족할 수 없음).
 *
 * 모든 사용자 값은 named parameter로 바인딩 — SQL 인젝션 위험 없음.
 */
@Component
public class PmsFreightLineSqlBuilder {

    // ── FROM 절 ───────────────────────────────────────────────────────────────

    /**
     * BMS-only FROM (fmsJoinNeeded = false).
     * 실적일자 필터 또는 BMS 전용 필터만 활성인 경우.
     */
    public String bmsOnlyFrom() {
        return """
            FROM bms.freight_line l
            JOIN bms.freight_header h ON h.freight_header_id = l.freight_header_id
            """;
    }

    /**
     * B/L-driven FROM (fmsJoinNeeded = true).
     * house_bl / master_bl 인덱스를 먼저 타게 한 뒤 BMS 테이블에 JOIN.
     * houseOnlyActive=true 시 master UNION ALL 브랜치를 생략.
     */
    public String blDrivenFrom(
            SearchPmsPerformanceCommand c,
            MapSqlParameterSource params,
            boolean houseOnlyActive) {

        StringBuilder sb = new StringBuilder();
        sb.append("FROM (\n");

        // ── HOUSE 브랜치 ────────────────────────────────────────────────────
        sb.append("  SELECT 'HOUSE' AS bl_type, house_bl_id AS bl_id\n");
        sb.append("  FROM fms.house_bl\n");
        List<String> houseWhere = buildHouseWhere(c, params);
        appendWhere(sb, houseWhere);

        if (!houseOnlyActive) {
            // ── MASTER 브랜치 ───────────────────────────────────────────────
            sb.append("  UNION ALL\n");
            sb.append("  SELECT 'MASTER' AS bl_type, master_bl_id AS bl_id\n");
            sb.append("  FROM fms.master_bl\n");
            List<String> masterWhere = buildMasterWhere(c, params);
            appendWhere(sb, masterWhere);
        }

        sb.append(") bl\n");
        sb.append("JOIN bms.freight_header h ON h.bl_type = bl.bl_type AND h.bl_id = bl.bl_id\n");
        sb.append("JOIN bms.freight_line l ON l.freight_header_id = h.freight_header_id\n");
        return sb.toString();
    }

    // ── HOUSE WHERE 술어 ──────────────────────────────────────────────────────

    private List<String> buildHouseWhere(SearchPmsPerformanceCommand c, MapSqlParameterSource params) {
        List<String> predicates = new ArrayList<>();

        // 날짜 범위 — house 테이블 자체 컬럼으로 BETWEEN (sargable)
        String dateKind = c.dateKind();
        if (hasValue(dateKind) && (hasValue(c.dateFrom()) || hasValue(c.dateTo()))) {
            String col = "ETD".equals(dateKind) ? "etd" : "eta";
            if (hasValue(c.dateFrom())) {
                predicates.add(col + " >= :fms_date_from");
                params.addValue("fms_date_from", c.dateFrom());
            }
            if (hasValue(c.dateTo())) {
                predicates.add(col + " <= :fms_date_to");
                params.addValue("fms_date_to", c.dateTo());
            }
        }

        addEq(predicates, params, "job_div", "fms_job_div", c.jobDiv());
        addEq(predicates, params, "bound", "fms_bound", c.bound());
        addLike(predicates, params, "hbl_no", "fms_hbl_no", c.hblNo());
        addLike(predicates, params, "mbl_no", "fms_mbl_no", c.mblNo());
        addPortFilter(predicates, params, c);
        addEq(predicates, params, "sales_man_code", "fms_sales_man_code", c.salesManCode());
        addEq(predicates, params, "sales_class", "fms_sales_class", c.salesClass());
        addEq(predicates, params, "incoterms", "fms_incoterms", c.incoterms());
        addEq(predicates, params, "team_code", "fms_team_code", c.teamCode());

        return predicates;
    }

    // ── MASTER WHERE 술어 ─────────────────────────────────────────────────────

    /**
     * master_bl은 hblNo/salesManCode/incoterms/salesClass/teamCode 컬럼이 없다.
     * 그 필터가 활성이면 이 브랜치 자체가 UNION ALL에서 제거되므로 여기서는 공통 필터만.
     */
    private List<String> buildMasterWhere(SearchPmsPerformanceCommand c, MapSqlParameterSource params) {
        List<String> predicates = new ArrayList<>();

        String dateKind = c.dateKind();
        if (hasValue(dateKind) && (hasValue(c.dateFrom()) || hasValue(c.dateTo()))) {
            String col = "ETD".equals(dateKind) ? "etd" : "eta";
            // params는 HOUSE 브랜치에서 이미 추가됨 — 같은 파라미터명 재사용
            if (hasValue(c.dateFrom())) predicates.add(col + " >= :fms_date_from");
            if (hasValue(c.dateTo()))   predicates.add(col + " <= :fms_date_to");
        }

        // job_div/bound/mbl_no/port — master에도 있는 공통 컬럼
        if (hasValue(c.jobDiv()))   predicates.add("job_div = :fms_job_div");
        if (hasValue(c.bound()))    predicates.add("bound = :fms_bound");
        if (hasValue(c.mblNo()))    predicates.add("mbl_no LIKE :fms_mbl_no || '%'");
        addPortFilter(predicates, params, c);   // params: fms_port_code 이미 추가됨

        return predicates;
    }

    // ── 외부 WHERE (l / h 테이블 — 두 FROM 형태 공통) ────────────────────────────

    /**
     * freight_line·freight_header 필터 + basis 술어를 수집한다.
     * params에 값도 함께 바인딩.
     */
    public List<String> buildOuterWhere(SearchPmsPerformanceCommand c, MapSqlParameterSource params) {
        List<String> predicates = new ArrayList<>();

        // basis 술어
        switch (c.effectiveBasis()) {
            case TAX_ISSUED  -> predicates.add("l.tax_no IS NOT NULL");
            case SLIP_ISSUED -> predicates.add("l.slip_no IS NOT NULL");
            default -> { /* FREIGHT_INPUT: 추가 조건 없음 */ }
        }

        // freight_line 필터
        if (hasValue(c.financialDocType())) {
            predicates.add("l.financial_doc_type = :financial_doc_type");
            params.addValue("financial_doc_type", c.financialDocType());
        }
        if (hasValue(c.taxType())) {
            predicates.add("l.tax_type = :tax_type");
            params.addValue("tax_type", c.taxType());
        }
        if (hasValue(c.issued())) {
            predicates.add("Y".equalsIgnoreCase(c.issued())
                ? "l.financial_document_id IS NOT NULL"
                : "l.financial_document_id IS NULL");
        }
        if (c.documentTypes() != null && !c.documentTypes().isEmpty()) {
            predicates.add("l.financial_doc_type IN (:doc_types)");
            params.addValue("doc_types", c.documentTypes());
        }

        // performanceDt 범위 (BMS-only FROM에서만 유효 — B/L-driven에서는 불필요하나 부여해도 무해)
        if (hasValue(c.performanceDtFrom())) {
            predicates.add("l.performance_dt >= :perf_from");
            params.addValue("perf_from", c.performanceDtFrom());
        }
        if (hasValue(c.performanceDtTo())) {
            predicates.add("l.performance_dt <= :perf_to");
            params.addValue("perf_to", c.performanceDtTo());
        }

        // freight_header 필터
        if (hasValue(c.actualCustomerCode())) {
            predicates.add("h.actual_customer_code = :acc");
            params.addValue("acc", c.actualCustomerCode());
        }
        if (hasValue(c.settlePartnerCode())) {
            predicates.add("h.settle_partner_code = :spc");
            params.addValue("spc", c.settlePartnerCode());
        }
        if (hasValue(c.carrierCode())) {
            predicates.add("h.liner_code = :liner_code");
            params.addValue("liner_code", c.carrierCode());
        }

        // partyKind + partyCode: 동적 거래처 코드 분기 (freight_header 컬럼)
        // PmsFmsJoinDecider.fmsJoinNeeded()에서 partyCode 활성 시 FMS JOIN을 트리거하지만
        // 필터 자체는 h.* 컬럼이므로 외부 WHERE에 추가한다.
        if (hasValue(c.partyKind()) && hasValue(c.partyCode())) {
            switch (c.partyKind()) {
                case "ACTUAL_CUSTOMER" -> {
                    predicates.add("h.actual_customer_code = :party_code");
                    params.addValue("party_code", c.partyCode());
                }
                case "SETTLE_PARTNER" -> {
                    predicates.add("h.settle_partner_code = :party_code");
                    params.addValue("party_code", c.partyCode());
                }
                default -> { /* 미인식 partyKind: 필터 무시 */ }
            }
        }

        return predicates;
    }

    // ── page CTE 래퍼 ────────────────────────────────────────────────────────

    /**
     * innerAggregate SQL을 page CTE로 감싸고 identity/cargo/name LEFT JOIN을 추가한다.
     * ORDER BY는 CTE 순서 보장 불가이므로 외부 SELECT에서 재적용한다.
     * JOIN 상수는 PmsIdentityNameJoinSql에 격리되어 document 경로와 공유한다.
     */
    public String wrapWithPageCteAndJoins(String innerAggregate) {
        return PmsIdentityNameJoinSql.wrapWithPageCte(innerAggregate);
    }

    // ── 공통 헬퍼 ─────────────────────────────────────────────────────────────

    /** houseOnlyActive: house-only 필터(hblNo/salesManCode 등)가 하나라도 활성인지 판단. */
    public boolean houseOnlyActive(SearchPmsPerformanceCommand c) {
        return hasValue(c.hblNo())
            || hasValue(c.salesManCode())
            || hasValue(c.incoterms())
            || hasValue(c.salesClass())
            || hasValue(c.teamCode());
    }

    private void addEq(List<String> predicates, MapSqlParameterSource params,
                       String col, String paramName, String value) {
        if (!hasValue(value)) return;
        predicates.add(col + " = :" + paramName);
        params.addValue(paramName, value);
    }

    private void addLike(List<String> predicates, MapSqlParameterSource params,
                         String col, String paramName, String value) {
        if (!hasValue(value)) return;
        predicates.add(col + " LIKE :" + paramName + " || '%'");
        params.addValue(paramName, value);
    }

    private void addPortFilter(List<String> predicates, MapSqlParameterSource params,
                               SearchPmsPerformanceCommand c) {
        if (!hasValue(c.portKind()) || !hasValue(c.portCode())) return;
        String col = "POL".equals(c.portKind()) ? "pol_code" : "pod_code";
        predicates.add(col + " = :fms_port_code");
        // 중복 추가 방지: HOUSE/MASTER 모두 같은 파라미터명을 공유
        if (!params.hasValue("fms_port_code")) {
            params.addValue("fms_port_code", c.portCode());
        }
    }

    private void appendWhere(StringBuilder sb, List<String> predicates) {
        if (!predicates.isEmpty()) {
            sb.append("  WHERE ").append(String.join("\n    AND ", predicates)).append("\n");
        }
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}
