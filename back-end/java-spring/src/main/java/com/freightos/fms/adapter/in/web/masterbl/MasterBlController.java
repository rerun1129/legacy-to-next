package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.CreateMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.SearchMasterBlRequest;
import com.freightos.fms.adapter.in.web.masterbl.dto.UpdateMasterBlRequest;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.application.masterbl.projection.MasterBlDetailResult;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.fms.application.masterbl.port.in.MasterBlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Tag(name = "Master B/L", description = "Master B/L CRUD — S-04/S-05")
@RestController
@RequestMapping("/api/master-bl")
@RequiredArgsConstructor
@Validated
public class MasterBlController {

    private final MasterBlUseCase masterBlUseCase;
    private final MasterBlAssembler masterBlAssembler;

    @Operation(summary = "Master B/L 검색")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<MasterBlSummaryResponse>>> searchMasterBls(
            @Valid @ModelAttribute SearchMasterBlRequest req) {
        return ResponseEntity.ok(ApiResponse.of(masterBlAssembler.toSummaryPage(
                masterBlUseCase.searchMasterBls(masterBlAssembler.toSearchCommand(req), PageRequest.of(req.page(), req.size())))));
    }

    @Operation(summary = "Master B/L 생성")
    @PostMapping
    public ResponseEntity<ApiResponse<MasterBlDetailResponse>> createMasterBl(
            @Valid @RequestBody CreateMasterBlRequest request,
            UriComponentsBuilder uriBuilder) {
        MasterBlDetailResult result = masterBlUseCase.createMasterBl(masterBlAssembler.toCreateCommand(request));
        URI location = uriBuilder.path("/api/master-bl/{id}").buildAndExpand(result.id()).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(masterBlAssembler.toDetail(result), MessageCode.MASTER_BL_CREATED.message()));
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
        MasterBlDetailResult result = masterBlUseCase.updateMasterBl(id, masterBlAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.of(masterBlAssembler.toDetail(result), MessageCode.MASTER_BL_UPDATED.message()));
    }

    @Operation(summary = "Master B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMasterBlById(@PathVariable Long id) {
        masterBlUseCase.deleteMasterBlById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MASTER_BL_DELETED.message()));
    }
}
