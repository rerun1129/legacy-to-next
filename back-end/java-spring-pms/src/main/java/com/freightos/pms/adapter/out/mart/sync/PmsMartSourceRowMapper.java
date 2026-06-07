package com.freightos.pms.adapter.out.mart.sync;

import com.freightos.pms.adapter.out.mart.document.BasisAggEmbedded;
import com.freightos.pms.adapter.out.mart.document.DocumentAggEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/**
 * ETL 배치 ResultSet 한 행 → PmsBlMartDocument 변환기.
 * runAt은 생성 시 주입받아 모든 행에 동일한 martUpdatedAt을 설정한다(행마다 now() 호출 금지).
 *
 * HOUSE/MASTER nulling 규칙은 기존 OLTP mapRow와 동일하게 유지한다.
 */
class PmsMartSourceRowMapper {

    private final Instant runAt;

    PmsMartSourceRowMapper(Instant runAt) {
        this.runAt = runAt;
    }

    PmsBlMartDocument mapRow(ResultSet rs) throws SQLException {
        String blType = rs.getString("bl_type");
        long blId = rs.getLong("bl_id");
        boolean isHouse = "HOUSE".equals(blType);
        String id = blType + "#" + blId;

        // ── 식별자 ───────────────────────────────────────────────────────────
        String houseBlNo = isHouse ? rs.getString("hbl_no") : null;
        String masterBlNo = isHouse ? rs.getString("h_mbl_no") : rs.getString("m_mbl_no");
        String jobDiv = isHouse ? rs.getString("h_job_div") : rs.getString("m_job_div");
        String bound  = isHouse ? rs.getString("h_bound")   : rs.getString("m_bound");
        String etd    = isHouse ? rs.getString("h_etd")     : rs.getString("m_etd");
        String eta    = isHouse ? rs.getString("h_eta")     : rs.getString("m_eta");
        String polCode = isHouse ? rs.getString("h_pol")    : rs.getString("m_pol");
        String podCode = isHouse ? rs.getString("h_pod")    : rs.getString("m_pod");
        String salesManCode   = isHouse ? rs.getString("sales_man_code")   : null;
        String incoterms      = isHouse ? rs.getString("incoterms")        : null;
        String salesClass     = isHouse ? rs.getString("sales_class")      : null;
        String houseTeamCode  = isHouse ? rs.getString("house_team_code")  : null;

        // ── 코드명 ───────────────────────────────────────────────────────────
        String houseTeamName = isHouse ? rs.getString("house_team_name") : null;
        String salesManName  = isHouse ? rs.getString("sales_man_name")  : null;

        // ── cargo (HOUSE 전용) ────────────────────────────────────────────────
        Integer   pkgQty              = isHouse ? rs.getObject("pkg_qty", Integer.class)  : null;
        BigDecimal cbm                = isHouse ? rs.getBigDecimal("cbm")                 : null;
        BigDecimal grossWeightKg      = isHouse ? rs.getBigDecimal("gross_weight_kg")     : null;
        String     seaLoadType        = isHouse ? rs.getString("sea_load_type")            : null;
        BigDecimal airChargeWeightKg  = isHouse ? rs.getBigDecimal("air_cw")              : null;
        BigDecimal truckChargeWeightKg = isHouse ? rs.getBigDecimal("tr_cw")              : null;
        String     truckLoadType      = isHouse ? rs.getString("tr_load_type")             : null;
        BigDecimal nonBlRton          = isHouse ? rs.getBigDecimal("non_bl_rton")          : null;

        // ── freight basis 블록 ────────────────────────────────────────────────
        long fiCnt = nvlLong(rs, "fi_cnt");
        BasisAggEmbedded freightInput = new BasisAggEmbedded(
            rs.getString("fi_perf"),
            fiCnt,
            nvl(rs, "fi_inv_l"), nvl(rs, "fi_deb_l"), nvl(rs, "fi_pay_l"), nvl(rs, "fi_crd_l"),
            nvl(rs, "fi_inv_u"), nvl(rs, "fi_deb_u"), nvl(rs, "fi_pay_u"), nvl(rs, "fi_crd_u")
        );

        long txCnt = nvlLong(rs, "tx_cnt");
        BasisAggEmbedded taxIssued = new BasisAggEmbedded(
            rs.getString("tx_perf"),
            txCnt,
            nvl(rs, "tx_inv_l"), nvl(rs, "tx_deb_l"), nvl(rs, "tx_pay_l"), nvl(rs, "tx_crd_l"),
            nvl(rs, "tx_inv_u"), nvl(rs, "tx_deb_u"), nvl(rs, "tx_pay_u"), nvl(rs, "tx_crd_u")
        );

        long slCnt = nvlLong(rs, "sl_cnt");
        BasisAggEmbedded slipIssued = new BasisAggEmbedded(
            rs.getString("sl_perf"),
            slCnt,
            nvl(rs, "sl_inv_l"), nvl(rs, "sl_deb_l"), nvl(rs, "sl_pay_l"), nvl(rs, "sl_crd_l"),
            nvl(rs, "sl_inv_u"), nvl(rs, "sl_deb_u"), nvl(rs, "sl_pay_u"), nvl(rs, "sl_crd_u")
        );

        // ── document basis 블록 ───────────────────────────────────────────────
        long dcCnt = nvlLong(rs, "dc_cnt");
        DocumentAggEmbedded documentCreated = new DocumentAggEmbedded(
            rs.getString("dc_perf"),
            dcCnt,
            nvl(rs, "dc_inv_l"), nvl(rs, "dc_deb_l"), nvl(rs, "dc_pay_l"), nvl(rs, "dc_crd_l"),
            nvl(rs, "dc_inv_u"), nvl(rs, "dc_deb_u"), nvl(rs, "dc_pay_u"), nvl(rs, "dc_crd_u"),
            // DocumentAggEmbedded 전용 추가 필드
            dcCnt,
            rs.getString("dc_team"),
            rs.getString("doc_team_name"),
            rs.getString("dc_operator")
        );

        return PmsBlMartDocument.builder()
            .id(id)
            .blType(blType)
            .blId(blId)
            .houseBlId(isHouse ? blId : null)
            .houseBlNo(houseBlNo)
            .masterBlNo(masterBlNo)
            .jobDiv(jobDiv)
            .bound(bound)
            .etd(etd)
            .eta(eta)
            .polCode(polCode)
            .podCode(podCode)
            .actualCustomerCode(rs.getString("actual_customer_code"))
            .settlePartnerCode(rs.getString("settle_partner_code"))
            .linerCode(rs.getString("liner_code"))
            .salesManCode(salesManCode)
            .incoterms(incoterms)
            .salesClass(salesClass)
            .houseTeamCode(houseTeamCode)
            .accName(rs.getString("acc_name"))
            .spcName(rs.getString("spc_name"))
            .lcName(rs.getString("lc_name"))
            .houseTeamName(houseTeamName)
            .salesManName(salesManName)
            .pkgQty(pkgQty)
            .cbm(cbm)
            .grossWeightKg(grossWeightKg)
            .seaLoadType(seaLoadType)
            .airChargeWeightKg(airChargeWeightKg)
            .truckChargeWeightKg(truckChargeWeightKg)
            .truckLoadType(truckLoadType)
            .nonBlRton(nonBlRton)
            .hasFreightInput(fiCnt > 0)
            .hasTaxIssued(txCnt > 0)
            .hasSlipIssued(slCnt > 0)
            .hasDocumentCreated(dcCnt > 0)
            .freightInput(freightInput)
            .taxIssued(taxIssued)
            .slipIssued(slipIssued)
            .documentCreated(documentCreated)
            .martUpdatedAt(runAt)
            .build();
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private BigDecimal nvl(ResultSet rs, String col) throws SQLException {
        BigDecimal v = rs.getBigDecimal(col);
        return v != null ? v : BigDecimal.ZERO;
    }

    private long nvlLong(ResultSet rs, String col) throws SQLException {
        long v = rs.getLong(col);
        return rs.wasNull() ? 0L : v;
    }
}
