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
 *
 * W1-B: 기간 필수 검증 — 세 쌍(ETD/ETA, 실적, 서류) 중 정확히 한 쌍이 양끝 모두 존재해야 한다.
 */
@Component
public class PmsPerformanceAssembler {

    public SearchPmsPerformanceCommand toCommand(SearchPmsPerformanceRequest req) {
        validateDatePair(req);
        AggregationBasis basis = parseBasis(req.basis());
        return new SearchPmsPerformanceCommand(
            basis, req.page(), req.size(),
            req.jobDiv(), req.bound(),
            req.dateKind(), req.dateFrom(), req.dateTo(),
            req.performanceDtFrom(), req.performanceDtTo(),
            req.documentDtFrom(), req.documentDtTo(),
            req.documentTypes(), req.documentStatus(),
            req.exactCount(), req.searchNonce()
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

    // ── 검증 ─────────────────────────────────────────────────────────────────

    /**
     * 기간 필수 검증(W1-B).
     * (dateFrom&&dateTo) / (performanceDtFrom&&performanceDtTo) / (documentDtFrom&&documentDtTo)
     * 세 쌍 중 정확히 한 쌍이 양끝 모두 존재해야 한다. 아니면 IllegalArgumentException → 400.
     */
    private void validateDatePair(SearchPmsPerformanceRequest req) {
        boolean hasEtdEta = hasText(req.dateFrom()) && hasText(req.dateTo());
        boolean hasPerf   = hasText(req.performanceDtFrom()) && hasText(req.performanceDtTo());
        boolean hasDoc    = hasText(req.documentDtFrom()) && hasText(req.documentDtTo());

        int pairCount = (hasEtdEta ? 1 : 0) + (hasPerf ? 1 : 0) + (hasDoc ? 1 : 0);
        if (pairCount != 1) {
            throw new IllegalArgumentException("조회 기간(From/To)은 필수입니다");
        }
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
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
