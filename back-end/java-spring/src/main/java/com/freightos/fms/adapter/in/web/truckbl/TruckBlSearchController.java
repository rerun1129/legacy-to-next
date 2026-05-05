package com.freightos.fms.adapter.in.web.truckbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.SearchTruckBlRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlSummaryResponse;
import com.freightos.fms.domain.truckbl.TruckBlFilter;
import com.freightos.fms.domain.truckbl.port.in.TruckBlSearchUseCase;
import com.freightos.fms.domain.truckbl.projection.TruckBlSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Truck B/L", description = "Truck B/L 검색")
@RestController
@RequestMapping("/api/truck-bl")
@RequiredArgsConstructor
public class TruckBlSearchController {

    private final TruckBlSearchUseCase truckBlSearchUseCase;

    @Operation(summary = "Truck B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<TruckBlSummaryResponse>>> searchTruckBls(
            @Valid @RequestBody SearchTruckBlRequest req) {
        TruckBlFilter filter = TruckBlFilter.of(
                req.bound(),
                req.truckBlNo(),
                req.etdFrom(),
                req.etdTo(),
                req.truckerCode(),
                req.docPartnerCode(),
                req.partyCode(),
                req.portCode(),
                req.operatorCode(),
                req.teamCode())
                .withKinds(req.dateKind(), req.partyKind(), req.portKind());
        PagedResult<TruckBlSummary> summaries = truckBlSearchUseCase.searchTruckBls(
                filter, PageRequest.of(req.page(), req.size()));
        return ResponseEntity.ok(ApiResponse.of(summaries.map(TruckBlSummaryResponse::from)));
    }
}
