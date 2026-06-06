package com.freightos.bms.adapter.in.web.financialdocument;

import com.freightos.bms.adapter.in.web.financialdocument.dto.FreightLineIssuePageResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FreightLineIssueRowResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueFreightLineRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueFreightLineResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.SearchFreightLineRequest;
import com.freightos.bms.application.financialdocument.FreightLineIssueRowView;
import com.freightos.bms.application.financialdocument.IssueFreightLineResult;
import com.freightos.bms.application.financialdocument.SearchFreightLineCriteria;
import com.freightos.bms.application.financialdocument.command.IssueFreightLineCommand;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FreightLineIssue Adapter(in) 어셈블러.
 * Request → Command / Criteria 변환 및 View/Result → Response 변환 담당.
 * domain.* import 금지(ARCH1) — application command/view만 참조.
 */
@Component
public class FreightLineIssueAssembler {

    public SearchFreightLineCriteria toCriteria(SearchFreightLineRequest req) {
        return new SearchFreightLineCriteria(
            req.customerCode(),
            req.financialDocType(),
            req.jobDiv(),
            req.bound(),
            req.performanceDtFrom(),
            req.performanceDtTo(),
            req.issuedStatus(),
            req.page(),
            req.size()
        );
    }

    /**
     * URL 엔드포인트(/tax, /slip) 기반으로 issueType을 고정한다.
     * request body의 issueType은 무시 — URL SSOT.
     */
    public IssueFreightLineCommand toCommand(IssueFreightLineRequest req, String issueType) {
        return new IssueFreightLineCommand(
            issueType,
            req.issueDt(),
            req.lineIds()
        );
    }

    public FreightLineIssuePageResponse toPageResponse(Page<FreightLineIssueRowView> page) {
        List<FreightLineIssueRowResponse> content = page.getContent().stream()
            .map(this::toRowResponse)
            .toList();
        return new FreightLineIssuePageResponse(
            content,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }

    public FreightLineIssueRowResponse toRowResponse(FreightLineIssueRowView view) {
        return new FreightLineIssueRowResponse(
            view.freightLineId(),
            view.freightHeaderId(),
            view.blType(),
            view.blId(),
            view.blNo(),
            view.jobDiv(),
            view.bound(),
            view.etd(),
            view.freightType(),
            view.financialDocType(),
            view.freightCode(),
            view.customerCode(),
            view.customerName(),
            view.currency(),
            view.settleAmount(),
            view.localAmount(),
            view.settleTaxAmount(),
            view.localTaxAmount(),
            view.usdAmount(),
            view.performanceDt(),
            view.financialDocumentId(),
            view.documentNo(),
            view.documentStatus(),
            view.taxNo(),
            view.taxDt(),
            view.slipNo(),
            view.slipDt()
        );
    }

    public IssueFreightLineResponse toResponse(IssueFreightLineResult result) {
        // Map<Long, String> → Map<String, String> (JSON 직렬화 호환)
        Map<String, String> statuses = new HashMap<>();
        result.statusByDocumentId().forEach((k, v) -> statuses.put(String.valueOf(k), v));
        return new IssueFreightLineResponse(
            result.issueNo(),
            result.affectedDocumentIds(),
            statuses
        );
    }
}
