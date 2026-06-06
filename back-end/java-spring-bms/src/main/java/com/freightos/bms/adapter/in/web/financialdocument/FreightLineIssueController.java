package com.freightos.bms.adapter.in.web.financialdocument;

import com.freightos.bms.adapter.in.web.financialdocument.dto.FreightLineIssuePageResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueFreightLineRequest;
import com.freightos.bms.adapter.in.web.financialdocument.dto.IssueFreightLineResponse;
import com.freightos.bms.adapter.in.web.financialdocument.dto.SearchFreightLineRequest;
import com.freightos.bms.application.financialdocument.port.in.FreightLineIssueUseCase;
import com.freightos.bms.common.response.MessageCode;
import com.freightos.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 운임 행 발급 REST 컨트롤러.
 * @PreAuthorize 미적용 — 단계 D 정책 유지(plan §2).
 * domain.* import 금지(ARCH1) — UseCase/Command/DTO/Assembler만 참조.
 */
@RestController
@RequestMapping("/api/bms/freight-line-issues")
@RequiredArgsConstructor
public class FreightLineIssueController {

    private final FreightLineIssueUseCase freightLineIssueUseCase;
    private final FreightLineIssueAssembler assembler;

    /**
     * 운임 행 전역 조회.
     * POST /api/bms/freight-line-issues/search
     */
    @PostMapping("/search")
    public ApiResponse<FreightLineIssuePageResponse> searchFreightLines(
            @RequestBody SearchFreightLineRequest request) {
        int pageNo = request.page() != null ? request.page() : 0;
        int pageSize = request.size() != null ? request.size() : 20;
        return ApiResponse.of(assembler.toPageResponse(
            freightLineIssueUseCase.searchFreightLines(
                assembler.toCriteria(request), PageRequest.of(pageNo, pageSize))
        ));
    }

    /**
     * 세금계산서 발급.
     * POST /api/bms/freight-line-issues/tax
     */
    @PostMapping("/tax")
    public ApiResponse<IssueFreightLineResponse> issueTax(
            @RequestBody @Valid IssueFreightLineRequest request) {
        return ApiResponse.of(
            assembler.toResponse(freightLineIssueUseCase.issue(assembler.toCommand(request, "TAX"))),
            MessageCode.FREIGHT_LINE_TAX_ISSUED.message()
        );
    }

    /**
     * 전표 발급.
     * POST /api/bms/freight-line-issues/slip
     */
    @PostMapping("/slip")
    public ApiResponse<IssueFreightLineResponse> issueSlip(
            @RequestBody @Valid IssueFreightLineRequest request) {
        return ApiResponse.of(
            assembler.toResponse(freightLineIssueUseCase.issue(assembler.toCommand(request, "SLIP"))),
            MessageCode.FREIGHT_LINE_SLIP_ISSUED.message()
        );
    }
}
