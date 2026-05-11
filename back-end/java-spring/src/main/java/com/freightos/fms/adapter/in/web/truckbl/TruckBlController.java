package com.freightos.fms.adapter.in.web.truckbl;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.ChangeTruckBlHblNoRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.CreateTruckBlRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.FindTruckBlByHblNoRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.SearchTruckBlRequest;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlDetailResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.TruckBlSummaryResponse;
import com.freightos.fms.adapter.in.web.truckbl.dto.UpdateTruckBlRequest;
import com.freightos.fms.application.truckbl.port.in.TruckBlSearchUseCase;
import com.freightos.fms.application.truckbl.port.in.TruckBlUseCase;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.common.response.MessageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Tag(name = "Truck B/L", description = "Truck B/L 관리")
@RestController
@RequestMapping("/api/truck-bl")
@RequiredArgsConstructor
@Validated
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

    @Operation(summary = "Truck B/L hblNo EXACT 매칭으로 house_bl_id PK 목록 조회 (최대 2건)")
    @PostMapping("/find-by-hbl-no")
    public ResponseEntity<ApiResponse<List<Long>>> findTruckBlsByHblNoExact(
            @Valid @RequestBody FindTruckBlByHblNoRequest req) {
        return ResponseEntity.ok(ApiResponse.of(
                truckBlUseCase.findTruckBlKeysByHblNoExact(req.hblNo())));
    }

    @Operation(summary = "Truck B/L 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createTruckBl(
            @Valid @RequestBody CreateTruckBlRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = truckBlUseCase.createTruckBl(truckBlAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/truck-bl/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(
                        Map.of("id", id),
                        MessageCode.TRUCK_BL_CREATED.message()));
    }

    @Operation(summary = "Truck B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TruckBlDetailResponse>> getTruckBlById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(truckBlAssembler.toDetail(truckBlUseCase.findTruckBlById(id))));
    }

    @Operation(summary = "Truck B/L 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateTruckBl(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTruckBlRequest req) {
        truckBlUseCase.updateTruckBl(id, truckBlAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.TRUCK_BL_UPDATED.message()));
    }

    @Operation(summary = "Truck B/L 번호 변경 (전용 엔드포인트)")
    @PutMapping("/{id}/hbl-no")
    public ResponseEntity<ApiResponse<Void>> changeTruckBlHblNo(
            @PathVariable Long id,
            @Valid @RequestBody ChangeTruckBlHblNoRequest req) {
        truckBlUseCase.changeTruckBlHblNo(id, new ChangeHouseBlNoCommand(req.hblNo()));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.TRUCK_BL_UPDATED.message()));
    }

    @Operation(summary = "Truck B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTruckBlById(@PathVariable Long id) {
        truckBlUseCase.deleteTruckBlById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.TRUCK_BL_DELETED.message()));
    }
}
