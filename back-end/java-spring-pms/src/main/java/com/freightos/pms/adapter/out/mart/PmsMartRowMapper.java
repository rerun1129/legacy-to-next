package com.freightos.pms.adapter.out.mart;

import com.freightos.pms.adapter.out.mart.document.BasisAggEmbedded;
import com.freightos.pms.adapter.out.mart.document.DocumentAggEmbedded;
import com.freightos.pms.adapter.out.mart.document.PmsBlMartDocument;
import com.freightos.pms.application.pms.projection.PmsRawBlRow;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * PmsBlMartDocument → PmsRawBlRow 변환기.
 * HOUSE/MASTER nulling 규칙은 OLTP mapRow와 1:1 동일하게 유지한다.
 */
@Component
public class PmsMartRowMapper {

    /**
     * freight_line basis(freightInput/taxIssued/slipIssued) 기반 행 변환.
     *
     * @param doc      Mart 문서
     * @param basisKey "freightInput" | "taxIssued" | "slipIssued"
     */
    public PmsRawBlRow toFreightRow(PmsBlMartDocument doc, String basisKey) {
        BasisAggEmbedded block = switch (basisKey) {
            case "freightInput" -> doc.getFreightInput();
            case "taxIssued"    -> doc.getTaxIssued();
            case "slipIssued"   -> doc.getSlipIssued();
            default -> throw new IllegalArgumentException("지원하지 않는 basisKey: " + basisKey);
        };

        boolean isHouse = "HOUSE".equals(doc.getBlType());
        // freight 경로: teamCode는 HOUSE 전용 houseTeamCode, operator는 없음
        String teamCode = isHouse ? doc.getHouseTeamCode() : null;
        String teamName = isHouse ? doc.getHouseTeamName() : null;

        return build(doc, block.getPerformanceDt(),
            block.getInvoiceLocalAmt(), block.getDebitLocalAmt(),
            block.getPaymentLocalAmt(), block.getCreditLocalAmt(),
            block.getInvoiceUsdAmt(), block.getDebitUsdAmt(),
            block.getPaymentUsdAmt(), block.getCreditUsdAmt(),
            teamCode, teamName, null);
    }

    /**
     * financial_document basis(documentCreated) 기반 행 변환.
     * teamCode/teamName/operator는 HOUSE·MASTER 모두 documentCreated 블록에서 읽는다.
     */
    public PmsRawBlRow toDocumentRow(PmsBlMartDocument doc) {
        DocumentAggEmbedded b = doc.getDocumentCreated();
        return build(doc, b.getPerformanceDt(),
            b.getInvoiceLocalAmt(), b.getDebitLocalAmt(),
            b.getPaymentLocalAmt(), b.getCreditLocalAmt(),
            b.getInvoiceUsdAmt(), b.getDebitUsdAmt(),
            b.getPaymentUsdAmt(), b.getCreditUsdAmt(),
            b.getTeamCode(), b.getTeamName(), b.getOperator());
    }

    // ── 공용 빌더 ─────────────────────────────────────────────────────────────

    /**
     * HOUSE/MASTER nulling 규칙을 적용하여 PmsRawBlRow를 조립한다.
     * MASTER이면 HOUSE 전용 필드(houseBlNo/salesManCode/incoterms/houseBlId/salesClass/cargo/salesManName)를 null 처리.
     * accName/spcName/lcName·masterBlNo/jobDiv/bound/etd/eta/polCode/podCode는 공통.
     */
    private PmsRawBlRow build(
            PmsBlMartDocument doc,
            String performanceDt,
            BigDecimal invoiceLocalAmt, BigDecimal debitLocalAmt,
            BigDecimal paymentLocalAmt, BigDecimal creditLocalAmt,
            BigDecimal invoiceUsdAmt, BigDecimal debitUsdAmt,
            BigDecimal paymentUsdAmt, BigDecimal creditUsdAmt,
            String teamCode, String teamName, String operator) {

        boolean isHouse = "HOUSE".equals(doc.getBlType());

        // HOUSE 전용 식별 필드
        String houseBlNo     = isHouse ? doc.getHouseBlNo()     : null;
        String salesManCode  = isHouse ? doc.getSalesManCode()  : null;
        String incoterms     = isHouse ? doc.getIncoterms()     : null;
        Long   houseBlId     = isHouse ? doc.getHouseBlId()     : null;
        String salesManName  = isHouse ? doc.getSalesManName()  : null;

        // HOUSE 전용 cargo
        Integer  pkgQty              = isHouse ? doc.getPkgQty()              : null;
        BigDecimal cbm               = isHouse ? doc.getCbm()                 : null;
        BigDecimal grossWeightKg     = isHouse ? doc.getGrossWeightKg()       : null;
        String   seaLoadType         = isHouse ? doc.getSeaLoadType()         : null;
        BigDecimal airChargeWeightKg = isHouse ? doc.getAirChargeWeightKg()   : null;
        BigDecimal truckChargeWeightKg = isHouse ? doc.getTruckChargeWeightKg() : null;
        String   truckLoadType       = isHouse ? doc.getTruckLoadType()       : null;
        BigDecimal nonBlRton         = isHouse ? doc.getNonBlRton()           : null;

        return new PmsRawBlRow(
            doc.getBlType(),
            doc.getBlId(),
            houseBlNo,
            doc.getMasterBlNo(),
            doc.getJobDiv(),
            doc.getBound(),
            doc.getEtd(),
            doc.getEta(),
            performanceDt,
            doc.getActualCustomerCode(),
            doc.getSettlePartnerCode(),
            doc.getLinerCode(),
            doc.getPolCode(),
            doc.getPodCode(),
            salesManCode,
            incoterms,
            houseBlId,
            teamCode,
            operator,
            invoiceLocalAmt,
            debitLocalAmt,
            paymentLocalAmt,
            creditLocalAmt,
            invoiceUsdAmt,
            debitUsdAmt,
            paymentUsdAmt,
            creditUsdAmt,
            pkgQty,
            cbm,
            grossWeightKg,
            seaLoadType,
            airChargeWeightKg,
            truckChargeWeightKg,
            truckLoadType,
            nonBlRton,
            doc.getAccName(),
            doc.getSpcName(),
            doc.getLcName(),
            teamName,
            salesManName
        );
    }
}
