package com.freightos.pms.adapter.in.web.pms;

import com.freightos.pms.adapter.in.web.pms.dto.PmsPerformancePageResponse;
import com.freightos.pms.adapter.in.web.pms.dto.PmsPerformanceRowResponse;
import com.freightos.pms.adapter.in.web.pms.dto.SearchPmsPerformanceRequest;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.projection.PmsPerformanceRowView;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * PMS 실적 조회 어셈블러. Request DTO → Command 변환, View → Response DTO 변환.
 * Controller·Domain 계층 간 변환 책임을 단일화한다(ARCH1).
 */
@Component
public class PmsPerformanceAssembler {

    public SearchPmsPerformanceCommand toCommand(SearchPmsPerformanceRequest req) {
        AggregationBasis basis = parseBasis(req.basis());
        return new SearchPmsPerformanceCommand(
            basis, req.page(), req.size(),
            req.jobDiv(), req.bound(),
            req.dateKind(), req.dateFrom(), req.dateTo(),
            req.performanceDtFrom(), req.performanceDtTo(),
            req.hblNo(), req.mblNo(),
            req.partyKind(), req.partyCode(),
            req.actualCustomerCode(), req.settlePartnerCode(),
            req.carrierCode(),
            req.portKind(), req.portCode(),
            req.salesManCode(), req.salesClass(), req.incoterms(),
            req.vesselVoyage(), req.loadType(),
            req.teamCode(), req.operator(),
            req.documentTypes(), req.documentStatus(),
            req.documentNoLike(), req.documentDtFrom(), req.documentDtTo(),
            req.groupFinancialNo(), req.grouped(), req.issued(),
            req.financialDocType(), req.taxType(),
            req.exactCount()
        );
    }

    public PmsPerformancePageResponse toPageResponse(Page<PmsPerformanceRowView> page) {
        return new PmsPerformancePageResponse(
            page.getContent().stream().map(this::toRowResponse).toList(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }

    public PmsPerformanceRowResponse toRowResponse(PmsPerformanceRowView view) {
        return new PmsPerformanceRowResponse(
            view.blType(), view.blId(),
            view.houseBlNo(), view.masterBlNo(),
            view.teamCode(), view.teamName(),
            view.jobDiv(), view.bound(), view.etd(), view.eta(), view.performanceDt(),
            view.actualCustomerCode(), view.actualCustomerName(),
            view.settlePartnerCode(), view.settlePartnerName(),
            view.linerCode(), view.linerName(),
            view.polCode(), view.podCode(),
            view.salesManCode(), view.salesManName(),
            view.incoterms(),
            view.loadType(), view.pkgQty(), view.rton(),
            view.cbm(), view.chargeWeightKg(), view.grossWeightKg(),
            view.invoiceLocalAmt(), view.debitLocalAmt(),
            view.paymentLocalAmt(), view.creditLocalAmt(), view.localProfit(),
            view.invoiceUsdAmt(), view.debitUsdAmt(),
            view.paymentUsdAmt(), view.creditUsdAmt(), view.usdProfit(),
            view.blClosed(), view.freightClosed()
        );
    }

    private AggregationBasis parseBasis(String raw) {
        if (raw == null || raw.isBlank()) return AggregationBasis.FREIGHT_INPUT;
        try {
            return AggregationBasis.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            // 알 수 없는 basis 문자열 → 기본값 사용
            return AggregationBasis.FREIGHT_INPUT;
        }
    }
}
