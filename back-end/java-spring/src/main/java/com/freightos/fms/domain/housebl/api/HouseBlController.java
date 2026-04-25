package com.freightos.fms.domain.housebl.api;

import com.freightos.fms.common.response.ApiResponse;
import com.freightos.fms.domain.housebl.api.dto.HouseBlSummaryResponse;
import com.freightos.fms.domain.housebl.entity.HouseBl;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.service.HouseBlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "House B/L", description = "House B/L CRUD — S-02/S-03")
@RestController
@RequestMapping("/api/v1/house-bl")
@RequiredArgsConstructor
public class HouseBlController {

    private final HouseBlService houseBlService;

    @Operation(summary = "House B/L 리스트 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<HouseBlSummaryResponse>>> list(
            @Parameter(description = "운송 모드") @RequestParam JobDiv jobDiv,
            @Parameter(description = "방향")     @RequestParam Bound  bound,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<HouseBlSummaryResponse> page = houseBlService.list(jobDiv, bound, pageable);
        return ResponseEntity.ok(ApiResponse.of(page));
    }

    @Operation(summary = "House B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HouseBl>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(houseBlService.getById(id)));
    }

    @Operation(summary = "House B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        houseBlService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("삭제되었습니다."));
    }
}
