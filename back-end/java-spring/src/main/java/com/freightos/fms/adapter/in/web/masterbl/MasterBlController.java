package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.CreateMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.FindMasterBlByMblNoRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.SearchMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.UpdateMasterBlRequest;
import com.freightos.fms.adapter.in.web.validation.SeaImpMasterGroup;
import com.freightos.fms.adapter.in.web.validation.SeaMasterGroup;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.masterbl.port.in.MasterBlUseCase;
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
import java.util.Set;

@Tag(name = "Master B/L", description = "Master B/L CRUD — S-04/S-05")
@RestController
@RequestMapping("/api/master-bl")
@RequiredArgsConstructor
@Validated
public class MasterBlController {

    private final MasterBlUseCase masterBlUseCase;
    private final MasterBlAssembler masterBlAssembler;
    private final Validator validator;

    @Operation(summary = "Master B/L 검색")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<MasterBlSummaryResponse>>> searchMasterBls(
            @Valid @ModelAttribute SearchMasterBlRequest req) {
        return ResponseEntity.ok(ApiResponse.of(masterBlAssembler.toSummaryPage(
                masterBlUseCase.searchMasterBls(masterBlAssembler.toSearchCommand(req), PageRequest.of(req.page(), req.size())))));
    }

    @Operation(summary = "Master B/L 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createMasterBl(
            @Valid @RequestBody CreateMasterBlRequest request,
            UriComponentsBuilder uriBuilder) {
        if ("SEA".equals(request.jobDiv())) {
            Class<?> group = "IMP".equals(request.bound()) ? SeaImpMasterGroup.class : SeaMasterGroup.class;
            Set<ConstraintViolation<CreateMasterBlRequest>> violations = validator.validate(request, group);
            if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        }
        Long id = masterBlUseCase.createMasterBl(masterBlAssembler.toCreateCommand(request));
        URI location = uriBuilder.path("/api/master-bl/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.MASTER_BL_CREATED.message()));
    }

    @Operation(summary = "Master B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterBlDetailResponse>> findMasterBlById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(masterBlAssembler.toDetail(masterBlUseCase.findMasterBlById(id))));
    }

    @Operation(summary = "Master B/L 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterBlDetailResponse>> updateMasterBl(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMasterBlRequest req) {
        return ResponseEntity.ok(ApiResponse.of(
                masterBlAssembler.toDetail(masterBlUseCase.updateMasterBl(id, masterBlAssembler.toUpdateCommand(req))),
                MessageCode.MASTER_BL_UPDATED.message()));
    }

    @Operation(summary = "Master B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMasterBlById(@PathVariable Long id) {
        masterBlUseCase.deleteMasterBlById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MASTER_BL_DELETED.message()));
    }

    @Operation(summary = "Master B/L mblNo EXACT 매칭으로 master_bl_id PK 목록 조회 (최대 2건)")
    @PostMapping("/find-by-mbl-no")
    public ResponseEntity<ApiResponse<List<Long>>> findMasterBlKeysByMblNo(
            @Valid @RequestBody FindMasterBlByMblNoRequest req) {
        return ResponseEntity.ok(ApiResponse.of(masterBlUseCase.findMasterBlKeysByMblNoExact(req.mblNo())));
    }
}
