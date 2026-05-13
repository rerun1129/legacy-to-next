package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.SearchHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

@Tag(name = "House B/L", description = "House B/L CRUD — S-02/S-03")
@RestController
@RequestMapping("/api/house-bl")
@RequiredArgsConstructor
@Validated
public class HouseBlController {

    private final HouseBlUseCase houseBlUseCase;
    private final HouseBlAssembler houseBlAssembler;

    @Operation(summary = "House B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<HouseBlSummaryResponse>>> searchHouseBls(
            @Valid @RequestBody SearchHouseBlRequest req) {
        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toSummaryPage(
                houseBlUseCase.searchHouseBls(houseBlAssembler.toSearchCommand(req), PageRequest.of(req.page(), req.size())))));
    }

    @Operation(summary = "House B/L 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<HouseBlDetailResponse>> createHouseBl(
            @Valid @RequestBody CreateHouseBlRequest request,
            UriComponentsBuilder uriBuilder) {
        Long id = houseBlUseCase.createHouseBl(houseBlAssembler.toCreateCommand(request));
        URI location = uriBuilder.path("/api/house-bl/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(houseBlAssembler.toDetail(houseBlUseCase.findHouseBlById(id)), MessageCode.HOUSE_BL_CREATED.message()));
    }

    @Operation(summary = "House B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HouseBlDetailResponse>> getHouseBlById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toDetail(houseBlUseCase.findHouseBlById(id))));
    }

    @Operation(summary = "House B/L 수정 (SEA: ApiResponse<Void>, 기타 jobDiv: ApiResponse<HouseBlDetailResponse>)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateHouseBl(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHouseBlRequest req) {
        UpdateHouseBlCommand cmd = houseBlAssembler.toUpdateCommand(req);
        // Sea jobDiv는 §6.35 전용 Port+Adapter — void 반환으로 Assembler.toDetail 호출 생략
        if (Objects.equals("SEA", req.jobDiv())) {
            houseBlUseCase.updateSeaHbl(id, cmd);
            return ResponseEntity.ok(ApiResponse.ok(MessageCode.SEA_HBL_UPDATED.message()));
        }
        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toDetail(houseBlUseCase.updateHouseBl(id, cmd))));
    }

    @Operation(summary = "House B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHouseBlById(@PathVariable Long id) {
        houseBlUseCase.deleteHouseBlById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.HOUSE_BL_DELETED.message()));
    }
}
