package com.freightos.fms.adapter.in.web.blquicksearch;

import com.freightos.common.response.ApiResponse;
import com.freightos.fms.adapter.in.web.blquicksearch.dto.BlQuickSearchAutocompleteRequest;
import com.freightos.fms.adapter.in.web.blquicksearch.dto.BlQuickSearchItemResponse;
import com.freightos.fms.application.blquicksearch.port.in.BlQuickSearchUseCase;
import com.freightos.fms.application.blquicksearch.projection.BlQuickSearchSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "BL Quick Search", description = "House/Master BL 통합 자동완성")
@RestController
@RequestMapping("/api/bl/quick-search")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('MENU_FMS_HOUSE_BL','MENU_FMS_MASTER_BL')")
public class BlQuickSearchController {

    private final BlQuickSearchUseCase blQuickSearchUseCase;
    private final BlQuickSearchAssembler blQuickSearchAssembler;

    @Operation(summary = "BL 번호 자동완성 (House + Master 통합)")
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<BlQuickSearchItemResponse>>> autocomplete(
            @ModelAttribute BlQuickSearchAutocompleteRequest req) {
        List<BlQuickSearchSummary> summaries = blQuickSearchUseCase.quickSearch(blQuickSearchAssembler.toCommand(req));
        return ResponseEntity.ok(ApiResponse.of(blQuickSearchAssembler.toResponseList(summaries)));
    }
}
