package com.freightos.admin.adapter.in.web.button;

import com.freightos.admin.adapter.in.web.button.dto.ButtonDetailResponse;
import com.freightos.admin.adapter.in.web.button.dto.ButtonSummaryResponse;
import com.freightos.admin.adapter.in.web.button.dto.CreateButtonRequest;
import com.freightos.admin.adapter.in.web.button.dto.SearchButtonRequest;
import com.freightos.admin.adapter.in.web.button.dto.UpdateButtonRequest;
import com.freightos.admin.application.button.port.in.ButtonUseCase;
import com.freightos.admin.application.button.projection.ButtonSummary;
import com.freightos.admin.common.request.BulkDeleteRequest;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/access/button")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class ButtonController {

    private final ButtonUseCase buttonUseCase;
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

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateButtonRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = buttonUseCase.createButton(buttonAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/access/button/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.BUTTON_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateButtonRequest req) {
        buttonUseCase.updateButton(id, buttonAssembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.BUTTON_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        buttonUseCase.deleteButtonById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.BUTTON_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_BUTTON_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        buttonUseCase.deleteButtonsByIds(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.BUTTON_DELETED.getMessage()));
    }
}
