package com.freightos.fms.adapter.in.web.seahouse;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.seahouse.dto.SearchSeaHouseRequest;
import com.freightos.fms.adapter.in.web.seahouse.dto.SeaHouseSummaryResponse;
import com.freightos.fms.application.seahouse.port.in.SeaHouseSearchUseCase;
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

@Tag(name = "Sea House", description = "Sea House B/L 검색")
@RestController
@RequestMapping("/api/sea-house")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('MENU_FMS_HOUSE_BL')")
public class SeaHouseController {

    private final SeaHouseSearchUseCase seaHouseSearchUseCase;
    private final SeaHouseAssembler seaHouseAssembler;

    @Operation(summary = "Sea House B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<SeaHouseSummaryResponse>>> searchSeaHouses(
            @Valid @RequestBody SearchSeaHouseRequest req) {
        return ResponseEntity.ok(ApiResponse.of(seaHouseAssembler.toSummaryPage(
                seaHouseSearchUseCase.searchSeaHouses(
                        seaHouseAssembler.toSearchCommand(req),
                        PageRequest.of(req.page(), req.size())))));
    }
}
