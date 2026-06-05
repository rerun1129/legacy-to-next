package com.freightos.bms.adapter.in.web.financialdocument;

import com.freightos.bms.adapter.in.web.financialdocument.dto.AmendDocumentRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.AmendDocumentResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.ApplyGroupingRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.ApplyGroupingResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FinancialDocumentPageResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FinancialDocumentResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.FreightLineDetailResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssuableLineResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueDocumentRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueDocumentResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.SearchFinancialDocumentRequest;
import com.freightos.bms.application.financialdocument.FinancialDocumentSearchView;
import com.freightos.bms.application.financialdocument.FreightLineDetailView;
import com.freightos.bms.application.financialdocument.SearchFinancialDocumentCriteria;
import com.freightos.bms.application.financialdocument.port.in.FinancialDocumentGroupUseCase;
import com.freightos.bms.application.financialdocument.port.in.FinancialDocumentUseCase;
import com.freightos.bms.common.response.MessageCode;
import com.freightos.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final FinancialDocumentGroupUseCase financialDocumentGroupUseCase;
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
            @RequestParam Long blId) {
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
            @RequestParam Long blId,
            @RequestParam String freightType) {
        List<IssuableLineResponse> responses = financialDocumentUseCase
            .findIssuableLines(blType, blId, freightType)
            .stream()
            .map(assembler::toResponse)
            .toList();
        return ApiResponse.of(responses);
    }

    /**
     * 금융 서류 전역 검색.
     * POST /api/bms/financial-documents/search
     * documentTypes IN 필수(최소 1개). 나머지 조건은 선택.
     */
    @PostMapping("/search")
    public ApiResponse<FinancialDocumentPageResponse> searchDocuments(
            @RequestBody @Valid SearchFinancialDocumentRequest request) {
        int pageNo = request.page() != null ? request.page() : 0;
        int pageSize = request.size() != null ? request.size() : 20;
        SearchFinancialDocumentCriteria criteria = assembler.toCriteria(request);
        Page<FinancialDocumentSearchView> page = financialDocumentUseCase.searchDocuments(
            criteria, PageRequest.of(pageNo, pageSize)
        );
        return ApiResponse.of(assembler.toPageResponse(page));
    }

    /**
     * 특정 금융 서류의 운임 라인 디테일 목록 조회.
     * GET /api/bms/financial-documents/{id}/lines
     */
    @GetMapping("/{id}/lines")
    public ApiResponse<List<FreightLineDetailResponse>> findDocumentLines(@PathVariable Long id) {
        List<FreightLineDetailResponse> responses = financialDocumentUseCase
            .findDocumentLines(id)
            .stream()
            .map(assembler::toDetailResponse)
            .toList();
        return ApiResponse.of(responses);
    }

    /**
     * 금융 서류 그룹 부여/해제.
     * POST /api/bms/financial-documents/group
     * groupedDocumentIds: 최종 그룹 포함 서류(모달 우측). scopeDocumentIds: 모달 전체 대상.
     */
    @PostMapping("/group")
    public ApiResponse<ApplyGroupingResponse> applyGrouping(
            @RequestBody @Valid ApplyGroupingRequest request) {
        ApplyGroupingResponse response = assembler.toResponse(
            financialDocumentGroupUseCase.applyGrouping(assembler.toCommand(request))
        );
        return ApiResponse.of(response, MessageCode.FINANCIAL_DOCUMENT_GROUPED.message());
    }
}
