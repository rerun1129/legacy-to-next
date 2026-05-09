package com.freightos.fms.adapter.in.web.truckbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.SearchTruckBlRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlDetailResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlSummaryResponse;
import com.freightos.fms.application.truckbl.port.in.TruckBlSearchUseCase;
import com.freightos.fms.application.truckbl.port.in.TruckBlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Truck B/L", description = "Truck B/L 검색")
@RestController
@RequestMapping("/api/truck-bl")
@RequiredArgsConstructor
public class TruckBlController {

    private final TruckBlSearchUseCase truckBlSearchUseCase;
    private final TruckBlUseCase truckBlUseCase;
    private final TruckBlAssembler truckBlAssembler;

    @Operation(summary = "Truck B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<TruckBlSummaryResponse>>> searchTruckBls(
            @Valid @RequestBody SearchTruckBlRequest req) {
        return ResponseEntity.ok(ApiResponse.of(truckBlAssembler.toSummaryPage(
                truckBlSearchUseCase.searchTruckBls(
                        truckBlAssembler.toSearchCommand(req),
                        PageRequest.of(req.page(), req.size())))));
    }

    @Operation(summary = "Truck B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TruckBlDetailResponse>> getTruckBlById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(truckBlAssembler.toDetail(truckBlUseCase.findTruckBlById(id))));
    }
}
