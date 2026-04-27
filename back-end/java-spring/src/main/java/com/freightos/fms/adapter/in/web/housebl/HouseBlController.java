package com.freightos.fms.adapter.in.web.housebl;

import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlDetailResponse;
import com.freightos.fms.adapter.in.web.housebl.dto.HouseBlSummaryResponse;
import com.freightos.fms.common.response.ApiResponse;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.model.PageRequest;
import com.freightos.fms.domain.common.model.PagedResult;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.port.in.HouseBlUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "House B/L", description = "House B/L CRUD — S-02/S-03")
@RestController
@RequestMapping("/api/v1/house-bl")
@RequiredArgsConstructor
public class HouseBlController {

    private final HouseBlUseCase houseBlUseCase;

    @Operation(summary = "House B/L 리스트 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResult<HouseBlSummaryResponse>>> list(
            @Parameter(description = "운송 모드") @RequestParam JobDiv jobDiv,
            @Parameter(description = "방향")     @RequestParam Bound  bound,
            @RequestParam(defaultValue = "0")    int page,
            @RequestParam(defaultValue = "50")   int size) {

        PagedResult<HouseBl> result = houseBlUseCase.list(jobDiv, bound, PageRequest.of(page, size));
        List<HouseBlSummaryResponse> content = result.getContent().stream()
                .map(HouseBlSummaryResponse::from)
                .toList();
        PagedResult<HouseBlSummaryResponse> response = PagedResult.of(
                content, result.getTotalElements(), result.getTotalPages(),
                result.getPage(), result.getSize());
        return ResponseEntity.ok(ApiResponse.of(response));
    }

    @Operation(summary = "House B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HouseBlDetailResponse>> getById(@PathVariable UUID id) {
        HouseBl houseBl = houseBlUseCase.getById(id);
        return ResponseEntity.ok(ApiResponse.of(HouseBlDetailResponse.from(houseBl)));
    }

    @Operation(summary = "House B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        houseBlUseCase.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("삭제되었습니다."));
    }
}
