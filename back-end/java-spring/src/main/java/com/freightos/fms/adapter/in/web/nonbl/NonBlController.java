package com.freightos.fms.adapter.in.web.nonbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.NonBlSummaryResponse;
import com.freightos.fms.adapter.in.web.nonbl.dto.SearchNonBlRequest;
import com.freightos.fms.domain.nonbl.NonBlFilter;
import com.freightos.fms.domain.nonbl.port.in.NonBlSearchUseCase;
import com.freightos.fms.domain.nonbl.projection.NonBlSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Non B/L", description = "Non B/L 검색")
@RestController
@RequestMapping("/api/non-bl")
@RequiredArgsConstructor
public class NonBlController {

    private final NonBlSearchUseCase nonBlSearchUseCase;

    @Operation(summary = "Non B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<NonBlSummaryResponse>>> searchNonBls(
            @Valid @RequestBody SearchNonBlRequest req) {
        NonBlFilter filter = NonBlFilter.of(
                req.bound(),
                req.hblNo(),
                req.etdFrom(),
                req.etdTo(),
                req.linerCode(),
                req.partyCode(),
                req.portCode(),
                req.vessel(),
                req.voyage(),
                req.operatorCode(),
                req.teamCode())
                .withKinds(req.dateKind(), req.partyKind(), req.portKind());
        PagedResult<NonBlSummary> summaries = nonBlSearchUseCase.searchNonBls(
                filter, PageRequest.of(req.page(), req.size()));
        return ResponseEntity.ok(ApiResponse.of(summaries.map(NonBlSummaryResponse::from)));
    }
}
