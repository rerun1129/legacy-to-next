package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.in.HouseBlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Tag(name = "House B/L", description = "House B/L CRUD — S-02/S-03")
@RestController
@RequestMapping("/api/house-bl")
@RequiredArgsConstructor
@Validated
public class HouseBlController {

    private final HouseBlUseCase houseBlUseCase;
    private final HouseBlAssembler houseBlAssembler;

    @Operation(summary = "House B/L 리스트 조회 (운송 모드 + 방향)")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<HouseBlSummaryResponse>>> getHouseBlsByJobDivAndBound(
            @Parameter(description = "운송 모드") @RequestParam JobDiv jobDiv,
            @Parameter(description = "방향")     @RequestParam Bound  bound,
            @RequestParam(defaultValue = "0")  @Min(0)  int page,
            @RequestParam(defaultValue = "50") @Min(1)  int size) {

        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toSummaryPage(
                houseBlUseCase.getHouseBlsByJobDivAndBound(jobDiv, bound, PageRequest.of(page, size)))));
    }

    @Operation(summary = "House B/L 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<HouseBlDetailResponse>> createHouseBl(
            @Valid @RequestBody CreateHouseBlRequest request,
            UriComponentsBuilder uriBuilder) {
        HouseBl saved = houseBlUseCase.save(houseBlAssembler.toEntity(request));
        URI location = uriBuilder.path("/api/house-bl/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(houseBlAssembler.toDetail(saved), MessageCode.HOUSE_BL_CREATED.message()));
    }

    @Operation(summary = "House B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HouseBlDetailResponse>> getHouseBlById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toDetail(houseBlUseCase.findHouseBlById(id))));
    }

    @Operation(summary = "House B/L 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HouseBlDetailResponse>> updateHouseBl(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHouseBlRequest request) {
        HouseBl entity = houseBlUseCase.findHouseBlById(id);
        HouseBl saved = houseBlUseCase.save(entity);
        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toDetail(saved)));
    }

    @Operation(summary = "House B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHouseBlById(@PathVariable Long id) {
        houseBlUseCase.deleteHouseBlById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.HOUSE_BL_DELETED.message()));
    }
}
