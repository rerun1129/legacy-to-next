package com.freightos.fms.adapter.in.web.airhouse;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.airhouse.dto.SearchAirHouseRequest;
import com.freightos.fms.adapter.in.web.airhouse.dto.AirHouseSummaryResponse;
import com.freightos.fms.application.airhouse.port.in.AirHouseSearchUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Air House", description = "Air House B/L 검색")
@RestController
@RequestMapping("/api/air-house")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MENU_FMS_HOUSE_BL')")
public class AirHouseController {

    private final AirHouseSearchUseCase airHouseSearchUseCase;
    private final AirHouseAssembler airHouseAssembler;

    @Operation(summary = "Air House B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<AirHouseSummaryResponse>>> searchAirHouses(
            @Valid @RequestBody SearchAirHouseRequest req) {
        return ResponseEntity.ok(ApiResponse.of(airHouseAssembler.toSummaryPage(
                airHouseSearchUseCase.searchAirHouses(
                        airHouseAssembler.toSearchCommand(req),
                        PageRequest.of(req.page(), req.size())))));
    }
}
