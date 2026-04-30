package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.common.response.ApiResponse;
import com.freightos.fms.common.response.MessageCode;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.port.in.MasterBlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Master B/L", description = "Master B/L CRUD — S-04/S-05")
@RestController
@RequestMapping("/api/master-bl")
@RequiredArgsConstructor
@Validated
public class MasterBlController {

    private final MasterBlUseCase masterBlUseCase;
    private final MasterBlAssembler masterBlAssembler;

    @Operation(summary = "Master B/L 리스트 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<MasterBlSummaryResponse>>> getMasterBlsByBound(
            @RequestParam Bound bound,
            @RequestParam(defaultValue = "0")  @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) int size) {
        return ResponseEntity.ok(ApiResponse.of(
                masterBlAssembler.toSummaryPage(masterBlUseCase.getMasterBlsByBound(bound, PageRequest.of(page, size)))));
    }

    @Operation(summary = "Master B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterBlDetailResponse>> findMasterBlById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(masterBlAssembler.toDetail(masterBlUseCase.findMasterBlDetailById(id))));
    }

    @Operation(summary = "Master B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMasterBlById(@PathVariable Long id) {
        masterBlUseCase.deleteMasterBlById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MASTER_BL_DELETED));
    }
}
