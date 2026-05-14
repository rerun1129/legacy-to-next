package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.ChangeHouseBlHblNoRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.CreateHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.FindHouseBlByHblNoRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.SearchHouseBlRequest;
import com.freightos.fms.adapter.in.web.housebl.dto.UpdateHouseBlRequest;
import com.freightos.fms.adapter.in.web.validation.SeaGroup;
import com.freightos.fms.adapter.in.web.validation.SeaImpGroup;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.housebl.command.ChangeHouseBlNoCommand;
import com.freightos.fms.application.housebl.command.UpdateHouseBlCommand;
import com.freightos.fms.application.housebl.port.in.HouseBlUseCase;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Tag(name = "House B/L", description = "House B/L CRUD — S-02/S-03")
@RestController
@RequestMapping("/api/house-bl")
@RequiredArgsConstructor
@Validated
public class HouseBlController {

    private final HouseBlUseCase houseBlUseCase;
    private final HouseBlAssembler houseBlAssembler;
    private final Validator validator;

    @Operation(summary = "House B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<HouseBlSummaryResponse>>> searchHouseBls(
            @Valid @RequestBody SearchHouseBlRequest req) {
        return ResponseEntity.ok(ApiResponse.of(houseBlAssembler.toSummaryPage(
                houseBlUseCase.searchHouseBls(houseBlAssembler.toSearchCommand(req), PageRequest.of(req.page(), req.size())))));
    }

    @Operation(summary = "House B/L hblNo EXACT 매칭으로 house_bl_id PK 목록 조회 (최대 2건)")
    @PostMapping("/find-by-hbl-no")
    public ResponseEntity<ApiResponse<List<Long>>> findHouseBlByHblNo(
            @Valid @RequestBody FindHouseBlByHblNoRequest req) {
        return ResponseEntity.ok(ApiResponse.of(
                houseBlUseCase.findHouseBlKeysByHblNoExact(req.hblNo(), JobDiv.valueOf(req.jobDiv()))));
    }

    @Operation(summary = "House B/L 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createHouseBl(
            @Valid @RequestBody CreateHouseBlRequest request,
            UriComponentsBuilder uriBuilder) {
        if ("SEA".equals(request.jobDiv())) {
            Class<?> group = "IMP".equals(request.bound()) ? SeaImpGroup.class : SeaGroup.class;
            Set<ConstraintViolation<CreateHouseBlRequest>> violations = validator.validate(request, group);
            if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        }
        Long id = houseBlUseCase.createHouseBl(houseBlAssembler.toCreateCommand(request));
        URI location = uriBuilder.path("/api/house-bl/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.HOUSE_BL_CREATED.message()));
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

    @Operation(summary = "House B/L 번호 변경 (전용 엔드포인트)")
    @PutMapping("/{id}/hbl-no")
    public ResponseEntity<ApiResponse<Void>> changeHblNo(
            @PathVariable Long id,
            @Valid @RequestBody ChangeHouseBlHblNoRequest req) {
        houseBlUseCase.changeHblNo(id, new ChangeHouseBlNoCommand(req.hblNo()));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.HOUSE_BL_UPDATED.message()));
    }

    @Operation(summary = "House B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteHouseBlById(@PathVariable Long id) {
        houseBlUseCase.deleteHouseBlById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.HOUSE_BL_DELETED.message()));
    }
}
