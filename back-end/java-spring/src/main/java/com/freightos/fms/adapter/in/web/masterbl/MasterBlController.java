package com.freightos.fms.adapter.in.web.masterbl;

import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlDetailResponse;
import com.freightos.fms.adapter.in.web.masterbl.dto.MasterBlSummaryResponse;
import com.freightos.fms.common.response.ApiResponse;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.port.in.MasterBlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Master B/L", description = "Master B/L CRUD — S-04/S-05")
@RestController
@RequestMapping("/api/v1/master-bl")
@RequiredArgsConstructor
@Validated
public class MasterBlController {

    private final MasterBlUseCase masterBlUseCase;

    @Operation(summary = "Master B/L 리스트 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<MasterBlSummaryResponse>>> list(
            @RequestParam Bound bound,
            @RequestParam(defaultValue = "0")  @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) int size) {
        PagedResult<MasterBl> result = masterBlUseCase.list(bound, PageRequest.of(page, size));
        List<MasterBlSummaryResponse> content = result.getContent().stream()
                .map(MasterBlSummaryResponse::from)
                .toList();
        PagedResult<MasterBlSummaryResponse> response = PagedResult.of(
                content, result.getTotalElements(), result.getTotalPages(),
                result.getPage(), result.getSize());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(summary = "Master B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterBlDetailResponse>> getById(@PathVariable UUID id) {
        MasterBl masterBl = masterBlUseCase.getById(id);
        return ResponseEntity.ok(ApiResponse.of(MasterBlDetailResponse.from(masterBl)));
    }

    @Operation(summary = "Master B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        masterBlUseCase.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("삭제되었습니다."));
    }
}
