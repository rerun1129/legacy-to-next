package com.freightos.admin.adapter.in.web.button;

import com.freightos.admin.adapter.in.web.button.dto.ButtonDetailResponse;
import com.freightos.admin.adapter.in.web.button.dto.ButtonSummaryResponse;
import com.freightos.admin.adapter.in.web.button.dto.SaveButtonChangesRequest;
import com.freightos.admin.adapter.in.web.button.dto.SearchButtonRequest;
import com.freightos.admin.application.button.port.in.ButtonUseCase;
import com.freightos.admin.application.button.port.in.SaveButtonChangesUseCase;
import com.freightos.admin.application.button.projection.ButtonSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/access/button")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('MENU_ADMIN_ACCESS_BUTTON')")
public class ButtonController {

    private final ButtonUseCase buttonUseCase;
    private final SaveButtonChangesUseCase saveButtonChangesUseCase;
    private final ButtonAssembler buttonAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<ButtonSummaryResponse>>> search(
            @Valid @RequestBody SearchButtonRequest req) {
        PagedResult<ButtonSummary> result = buttonUseCase.searchButtons(buttonAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(buttonAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ButtonDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(buttonAssembler.toDetail(buttonUseCase.findButtonById(id))));
    }

    @PostMapping("/save-changes")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_BUTTON_SAVE')")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SaveButtonChangesRequest req) {
        SaveChangesResult result = saveButtonChangesUseCase.saveButtonChanges(buttonAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.BUTTON_SAVE_CHANGES.getMessage()));
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<AutocompleteItem>>> autocomplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.of(buttonUseCase.autocompleteButtonCodes(query, limit)));
    }
}
