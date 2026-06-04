package com.freightos.bms.adapter.in.web.financialdocument;

import com.freightos.bms.adapter.in.web.financialdocument.dto.AmendDocumentRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.AmendDocumentResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FinancialDocumentResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssuableLineResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueDocumentRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueDocumentResponse;
import com.freightos.bms.application.financialdocument.AmendResult;
import com.freightos.bms.application.financialdocument.FinancialDocumentView;
import com.freightos.bms.application.financialdocument.IssuableLineView;
import com.freightos.bms.application.financialdocument.IssueResult;
import com.freightos.bms.application.financialdocument.command.AmendDocumentCommand;
import com.freightos.bms.application.financialdocument.command.IssueDocumentCommand;
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
        return new AmendDocumentCommand(id, req.blType(), req.blId(), req.freightType(), finalLineIds);
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
            view.operator()
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
}
