package com.freightos.pms.adapter.in.web.pms;

import com.freightos.common.response.ApiResponse;
import com.freightos.pms.adapter.in.web.pms.dto.PmsPerformancePageResponse;
import com.freightos.pms.adapter.in.web.pms.dto.SearchPmsPerformanceRequest;
import com.freightos.pms.application.pms.PmsPerformanceSearchResult;
import com.freightos.pms.application.pms.command.SearchPmsPerformanceCommand;
import com.freightos.pms.application.pms.port.in.PmsPerformanceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * PMS 실적 조회 REST 컨트롤러.
 * domain.* import 금지(ARCH1) — UseCase/Command/Assembler만 참조.
 */
@RestController
@RequestMapping("/api/pms/performance")
@RequiredArgsConstructor
public class PmsPerformanceController {

    private final PmsPerformanceUseCase pmsPerformanceUseCase;
    private final PmsPerformanceAssembler assembler;

    /**
     * PS-01 실적 조회.
     * POST /api/pms/performance/search
     * 모든 조건은 선택. basis 미지정 시 FREIGHT_INPUT 기본.
     */
    @PostMapping("/search")
    public ApiResponse<PmsPerformancePageResponse> search(@RequestBody SearchPmsPerformanceRequest request) {
        int pageNo = request.page() != null ? request.page() : 0;
        int pageSize = request.size() != null ? request.size() : 20;
        SearchPmsPerformanceCommand command = assembler.toCommand(request);
        PmsPerformanceSearchResult result = pmsPerformanceUseCase.search(command, PageRequest.of(pageNo, pageSize));
        return ApiResponse.of(assembler.toPageResponse(result));
    }
}
