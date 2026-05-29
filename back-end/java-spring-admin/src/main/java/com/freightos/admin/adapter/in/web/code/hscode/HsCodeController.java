package com.freightos.admin.adapter.in.web.code.hscode;

import com.freightos.admin.adapter.in.web.code.hscode.dto.CreateHsCodeRequest;
import com.freightos.admin.adapter.in.web.code.hscode.dto.HsCodeDetailResponse;
import com.freightos.admin.adapter.in.web.code.hscode.dto.HsCodeSummaryResponse;
import com.freightos.admin.adapter.in.web.code.hscode.dto.SaveHsCodeChangesRequest;
import com.freightos.admin.adapter.in.web.code.hscode.dto.SearchHsCodeRequest;
import com.freightos.admin.adapter.in.web.code.hscode.dto.UpdateHsCodeRequest;
import com.freightos.admin.application.code.hscode.port.in.HsCodeUseCase;
import com.freightos.admin.application.code.hscode.projection.HsCodeSummary;
import com.freightos.admin.common.request.BulkDeleteRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.code.hscode.entity.HsCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/code/hs-code")
@RequiredArgsConstructor
@Validated
public class HsCodeController {

    private final HsCodeUseCase hsCodeUseCase;
    private final HsCodeAssembler hsCodeAssembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_HSCODE')")
    public ResponseEntity<ApiResponse<PagedResult<HsCodeSummaryResponse>>> search(
            @Valid @RequestBody SearchHsCodeRequest req) {
        PagedResult<HsCodeSummary> result = hsCodeUseCase.searchHsCodes(hsCodeAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(hsCodeAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_HSCODE')")
    public ResponseEntity<ApiResponse<HsCodeDetailResponse>> getById(@PathVariable Long id) {
        HsCode domain = hsCodeUseCase.getHsCodeById(id);
        return ResponseEntity.ok(ApiResponse.of(hsCodeAssembler.toDetail(domain)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_HSCODE_SAVE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateHsCodeRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = hsCodeUseCase.createHsCode(hsCodeAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/code/hs-code/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.HS_CODE_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_HSCODE_SAVE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHsCodeRequest req) {
        hsCodeUseCase.updateHsCode(id, hsCodeAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.HS_CODE_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_HSCODE_SAVE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        hsCodeUseCase.deleteHsCode(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.HS_CODE_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_HSCODE_SAVE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        hsCodeUseCase.deleteHsCodes(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.HS_CODE_DELETED.getMessage()));
    }

    @PostMapping("/save-changes")
    @PreAuthorize("hasAuthority('BTN_ADMIN_CODE_HSCODE_SAVE')")
    public ResponseEntity<ApiResponse<SaveChangesResult>> saveChanges(
            @Valid @RequestBody SaveHsCodeChangesRequest req) {
        SaveChangesResult result = hsCodeUseCase.saveHsCodeChanges(hsCodeAssembler.toSaveChangesCommand(req));
        return ResponseEntity.ok(ApiResponse.of(result, MessageCode.HS_CODE_SAVE_CHANGES.getMessage()));
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("hasAuthority('MENU_ADMIN_CODE_HSCODE')")
    public ResponseEntity<ApiResponse<List<AutocompleteItem>>> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
        return ResponseEntity.ok(ApiResponse.of(hsCodeUseCase.autocompleteHsCodes(q, limit)));
    }
}
