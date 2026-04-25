package com.freightos.fms.domain.masterbl.api;

import com.freightos.fms.common.response.ApiResponse;
import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.masterbl.entity.MasterBl;
import com.freightos.fms.domain.masterbl.service.MasterBlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Master B/L", description = "Master B/L CRUD — S-04/S-05")
@RestController
@RequestMapping("/api/v1/master-bl")
@RequiredArgsConstructor
public class MasterBlController {

    private final MasterBlService masterBlService;

    @Operation(summary = "Master B/L 리스트 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MasterBl>>> list(
            @RequestParam Bound bound,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.of(masterBlService.list(bound, pageable)));
    }

    @Operation(summary = "Master B/L 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MasterBl>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(masterBlService.getById(id)));
    }

    @Operation(summary = "Master B/L 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        masterBlService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("삭제되었습니다."));
    }
}
