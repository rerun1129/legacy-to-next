package com.freightos.fms.adapter.in.web.seamaster;

import com.freightos.common.model.PageRequest;
import com.freightos.common.model.PagedResult;
import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.seamaster.dto.SearchSeaMasterRequest;
import com.freightos.fms.adapter.in.web.seamaster.dto.SeaMasterSummaryResponse;
import com.freightos.fms.application.seamaster.port.in.SeaMasterSearchUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Sea Master", description = "Sea Master B/L 검색")
@RestController
@RequestMapping("/api/sea-master")
@RequiredArgsConstructor
public class SeaMasterController {

    private final SeaMasterSearchUseCase seaMasterSearchUseCase;
    private final SeaMasterAssembler seaMasterAssembler;

    @Operation(summary = "Sea Master B/L 검색 (POST body)")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<SeaMasterSummaryResponse>>> searchSeaMasters(
            @Valid @RequestBody SearchSeaMasterRequest req) {
        return ResponseEntity.ok(ApiResponse.of(seaMasterAssembler.toSummaryPage(
                seaMasterSearchUseCase.searchSeaMasters(
                        seaMasterAssembler.toSearchCommand(req),
                        PageRequest.of(req.page(), req.size())))));
    }
}
