package com.freightos.bms.adapter.in.web.financialdocument;

import com.freightos.bms.adapter.in.web.financialdocument.dto.AmendDocumentRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.AmendDocumentResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.ApplyGroupingRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.ApplyGroupingResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FinancialDocumentPageResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FinancialDocumentResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FinancialDocumentSearchResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FreightLineDetailResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssuableLineResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueDocumentRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueDocumentResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.SearchFinancialDocumentRequest;
import com.freightos.bms.application.financialdocument.AmendResult;
import com.freightos.bms.application.financialdocument.FinancialDocumentSearchView;
import com.freightos.bms.application.financialdocument.FinancialDocumentView;
import com.freightos.bms.application.financialdocument.FreightLineDetailView;
import com.freightos.bms.application.financialdocument.GroupResult;
import com.freightos.bms.application.financialdocument.IssuableLineView;
import com.freightos.bms.application.financialdocument.IssueResult;
import com.freightos.bms.application.financialdocument.SearchFinancialDocumentCriteria;
import com.freightos.bms.application.financialdocument.command.AmendDocumentCommand;
import com.freightos.bms.application.financialdocument.command.ApplyGroupingCommand;
import com.freightos.bms.application.financialdocument.command.IssueDocumentCommand;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * FinancialDocument Adapter(in) 어셈블러.
 * Request → Command 변환 및 View → Response 변환 담당.
 * domain.* import 금지(ARCH1) — application의 command/VO만 사용.
 */
@Component
public class FinancialDocumentAssembler {

    public IssueDocumentCommand toCommand(IssueDocumentRequest req) {
        return new IssueDocumentCommand(
            req.blType(),
            req.blId(),
            req.freightType(),
            req.lineIds(),
            req.documentDt(),
            req.performanceDt(),
            req.teamCode(),
            req.operator()
        );
    }

    public AmendDocumentCommand toCommand(Long id, AmendDocumentRequest req) {
        List<Long> finalLineIds = req.finalLineIds() != null ? req.finalLineIds() : Collections.emptyList();
        return new AmendDocumentCommand(
            id, req.blType(), req.blId(), req.freightType(), finalLineIds,
            req.documentDt(), req.performanceDt(), req.teamCode(), req.operator()
        );
    }

    public IssueDocumentResponse toResponse(IssueResult result) {
        return new IssueDocumentResponse(result.financialDocumentId(), result.documentNo());
    }

    public AmendDocumentResponse toResponse(AmendResult result) {
        return new AmendDocumentResponse(result.financialDocumentId(), result.documentNo(), result.deleted());
    }

    public FinancialDocumentResponse toResponse(FinancialDocumentView view) {
        return new FinancialDocumentResponse(
            view.financialDocumentId(),
            view.documentNo(),
            view.documentType(),
            view.documentDt(),
            view.status(),
            view.customerCode(),
            view.customerName(),
            view.settleTotalAmount(),
            view.localTotalAmount(),
            view.settleTotalVat(),
            view.localTotalVat(),
            view.usdTotalAmount(),
            view.performanceDt(),
            view.teamCode(),
            view.operator(),
            view.groupFinancialNo()
        );
    }

    public IssuableLineResponse toResponse(IssuableLineView view) {
        return new IssuableLineResponse(
            view.freightLineId(),
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
            view.documentNo()
        );
    }

    public SearchFinancialDocumentCriteria toCriteria(SearchFinancialDocumentRequest req) {
        return new SearchFinancialDocumentCriteria(
            req.documentTypes(),
            req.documentStatus(),
            req.customerCode(),
            req.documentNoLike(),
            req.teamCode(),
            req.operator(),
            req.documentDtFrom(),
            req.documentDtTo(),
            req.performanceDtFrom(),
            req.performanceDtTo(),
            req.etdFrom(),
            req.etdTo(),
            req.etaFrom(),
            req.etaTo(),
            req.jobDiv(),
            req.bound(),
            req.groupFinancialNo(),
            req.grouped()
        );
    }

    public ApplyGroupingCommand toCommand(ApplyGroupingRequest req) {
        return new ApplyGroupingCommand(req.groupedDocumentIds(), req.scopeDocumentIds());
    }

    public ApplyGroupingResponse toResponse(GroupResult result) {
        return new ApplyGroupingResponse(
            result.groupFinancialNo(),
            result.groupedDocumentIds(),
            result.ungroupedDocumentIds()
        );
    }

    public FinancialDocumentPageResponse toPageResponse(Page<FinancialDocumentSearchView> page) {
        List<FinancialDocumentSearchResponse> content = page.getContent().stream()
            .map(this::toSearchResponse)
            .toList();
        return new FinancialDocumentPageResponse(
            content,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }

    public FinancialDocumentSearchResponse toSearchResponse(FinancialDocumentSearchView view) {
        return new FinancialDocumentSearchResponse(
            view.financialDocumentId(),
            view.documentNo(),
            view.documentType(),
            view.documentDt(),
            view.documentStatus(),
            view.customerCode(),
            view.customerName(),
            view.settleTotalAmount(),
            view.localTotalAmount(),
            view.settleTotalVat(),
            view.localTotalVat(),
            view.usdTotalAmount(),
            view.performanceDt(),
            view.teamCode(),
            view.teamName(),
            view.operator(),
            view.operatorName(),
            view.groupFinancialNo(),
            view.blType(),
            view.blId(),
            view.jobDiv(),
            view.bound(),
            view.blNo(),
            view.etd(),
            view.eta()
        );
    }

    public FreightLineDetailResponse toDetailResponse(FreightLineDetailView view) {
        return new FreightLineDetailResponse(
            view.freightLineId(),
            view.freightHeaderId(),
            view.freightType(),
            view.financialDocType(),
            view.freightCode(),
            view.freightName(),
            view.unitQuantity(),
            view.unitPrice(),
            view.per(),
            view.currency(),
            view.exchangeRate(),
            view.settleAmount(),
            view.localAmount(),
            view.settleTaxAmount(),
            view.localTaxAmount(),
            view.usdExchangeRate(),
            view.usdAmount(),
            view.customerCode(),
            view.customerName(),
            view.taxType(),
            view.taxNo(),
            view.taxDt(),
            view.slipNo(),
            view.slipDt(),
            view.performanceDt(),
            view.financialDocumentId()
        );
    }
}
