package com.freightos.bms.adapter.in.web.financialdocument;

import com.freightos.bms.adapter.in.web.financialdocument.dto.AmendDocumentRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.AmendDocumentResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FinancialDocumentResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssuableLineResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueDocumentRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueDocumentResponse;
import com.freightos.bms.application.financialdocument.port.in.FinancialDocumentUseCase;
import com.freightos.bms.common.response.MessageCode;
import com.freightos.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 금융 서류 REST 컨트롤러.
 * domain.* import 금지(ARCH1) — UseCase/DTO/Assembler만 참조.
 */
@RestController
@RequestMapping("/api/bms/financial-documents")
@RequiredArgsConstructor
public class FinancialDocumentController {

    private final FinancialDocumentUseCase financialDocumentUseCase;
    private final FinancialDocumentAssembler assembler;

    /**
     * 금융 서류 발행.
     * POST /api/bms/financial-documents/issue
     */
    @PostMapping("/issue")
    public ApiResponse<IssueDocumentResponse> issueDocument(
            @RequestBody @Valid IssueDocumentRequest request) {
        IssueDocumentResponse response = assembler.toResponse(
            financialDocumentUseCase.issueDocument(assembler.toCommand(request))
        );
        return ApiResponse.of(response, MessageCode.FINANCIAL_DOCUMENT_CREATED.message());
    }

    /**
     * 금융 서류 편집(amend).
     * PATCH /api/bms/financial-documents/{id}
     * finalLineIds 빈 리스트 = 서류 자동 삭제.
     */
    @PatchMapping("/{id}")
    public ApiResponse<AmendDocumentResponse> amendDocument(
            @PathVariable Long id,
            @RequestBody @Valid AmendDocumentRequest request) {
        AmendDocumentResponse response = assembler.toResponse(
            financialDocumentUseCase.amendDocument(assembler.toCommand(id, request))
        );
        return ApiResponse.of(response, MessageCode.FINANCIAL_DOCUMENT_UPDATED.message());
    }

    /**
     * 금융 서류 삭제.
     * DELETE /api/bms/financial-documents/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long id) {
        financialDocumentUseCase.deleteDocument(id);
        return ApiResponse.ok(MessageCode.FINANCIAL_DOCUMENT_DELETED.message());
    }

    /**
     * B/L 기준 금융 서류 목록 조회.
     * GET /api/bms/financial-documents?blType=HOUSE&blId=xxx
     */
    @GetMapping
    public ApiResponse<List<FinancialDocumentResponse>> findDocumentsByBl(
            @RequestParam String blType,
            @RequestParam String blId) {
        List<FinancialDocumentResponse> responses = financialDocumentUseCase
            .findDocumentsByBl(blType, blId)
            .stream()
            .map(assembler::toResponse)
            .toList();
        return ApiResponse.of(responses);
    }

    /**
     * 발행 가능 운임 라인 목록 조회.
     * GET /api/bms/financial-documents/issuable-lines?blType=HOUSE&blId=xxx&freightType=SELLING
     */
    @GetMapping("/issuable-lines")
    public ApiResponse<List<IssuableLineResponse>> findIssuableLines(
            @RequestParam String blType,
            @RequestParam String blId,
            @RequestParam String freightType) {
        List<IssuableLineResponse> responses = financialDocumentUseCase
            .findIssuableLines(blType, blId, freightType)
            .stream()
            .map(assembler::toResponse)
            .toList();
        return ApiResponse.of(responses);
    }
}
