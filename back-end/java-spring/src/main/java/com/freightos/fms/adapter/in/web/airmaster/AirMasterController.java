package com.freightos.fms.adapter.in.web.airmaster;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.airmaster.dto.SearchAirMasterRequest;
import com.freightos.fms.adapter.in.web.airmaster.dto.AirMasterSummaryResponse;
import com.freightos.fms.application.airmaster.port.in.AirMasterSearchUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Air Master", description = "Air Master B/L 검색")
@RestController
@RequestMapping("/api/air-master")
@RequiredArgsConstructor
public class AirMasterController {

    private final AirMasterSearchUseCase airMasterSearchUseCase;
    private final AirMasterAssembler airMasterAssembler;

    @Operation(summary = "Air Master B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<AirMasterSummaryResponse>>> searchAirMasters(
            @Valid @RequestBody SearchAirMasterRequest req) {
        return ResponseEntity.ok(ApiResponse.of(airMasterAssembler.toSummaryPage(
                airMasterSearchUseCase.searchAirMasters(
                        airMasterAssembler.toSearchCommand(req),
                        PageRequest.of(req.page(), req.size())))));
    }
}
