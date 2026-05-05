package com.freightos.fms.adapter.in.web.enums;

import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.enums.dto.EnumMapResponse;
import com.freightos.fms.adapter.in.web.enums.dto.EnumOptionResponse;
import com.freightos.fms.domain.enums.EnumRegistry;
import com.freightos.fms.domain.enums.port.in.EnumQueryResult;
import com.freightos.fms.domain.enums.port.in.EnumQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Enums", description = "드롭박스 ENUM 메타 조회 SSOT")
@RestController
@RequestMapping("/api/enums")
public class EnumController {

    private final EnumQueryUseCase enumQueryUseCase;
    private final EnumAssembler enumAssembler;
    private final EnumRegistry enumRegistry;

    public EnumController(EnumQueryUseCase enumQueryUseCase,
                          EnumAssembler enumAssembler,
                          EnumRegistry enumRegistry) {
        this.enumQueryUseCase = enumQueryUseCase;
        this.enumAssembler    = enumAssembler;
        this.enumRegistry     = enumRegistry;
    }

    @Operation(summary = "ENUM 메타 단일 조회 (DropBox 옵션 SSOT)")
    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<List<EnumOptionResponse>>> getByName(
            @PathVariable String name) {
        List<EnumOptionResponse> data = enumAssembler.toResponse(enumQueryUseCase.getByName(name));
        String etag = enumRegistry.getEtag();
        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=3600")
                .header("ETag", "\"" + etag + "\"")
                .body(ApiResponse.of(data));
    }

    @Operation(summary = "ENUM 메타 일괄 조회 (DropBox 옵션 SSOT)")
    @GetMapping
    public ResponseEntity<ApiResponse<EnumMapResponse>> getByNames(
            @RequestParam List<String> names) {
        EnumQueryResult result = enumQueryUseCase.getByNames(names);
        EnumMapResponse data = enumAssembler.toMapResponse(result);
        String etag = enumRegistry.getEtag();
        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=3600")
                .header("ETag", "\"" + etag + "\"")
                .body(ApiResponse.of(data));
    }
}
