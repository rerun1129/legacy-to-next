package com.freightos.admin.adapter.in.web.buttonpolicy;

import com.freightos.admin.adapter.in.web.buttonpolicy.dto.ButtonPolicyDetailResponse;
import com.freightos.admin.adapter.in.web.buttonpolicy.dto.ButtonPolicySummaryResponse;
import com.freightos.admin.adapter.in.web.buttonpolicy.dto.CreateButtonPolicyRequest;
import com.freightos.admin.adapter.in.web.buttonpolicy.dto.SearchButtonPolicyRequest;
import com.freightos.admin.application.buttonpolicy.port.in.ButtonPolicyUseCase;
import com.freightos.admin.application.buttonpolicy.projection.ButtonPolicySummary;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/access/button-policy")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasAuthority('MENU_ADMIN_ACCESS_POLICY')")
public class ButtonPolicyController {

    private final ButtonPolicyUseCase buttonPolicyUseCase;
    private final ButtonPolicyAssembler buttonPolicyAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<ButtonPolicySummaryResponse>>> search(
            @Valid @RequestBody SearchButtonPolicyRequest req) {
        PagedResult<ButtonPolicySummary> result = buttonPolicyUseCase.searchButtonPolicies(buttonPolicyAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(buttonPolicyAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ButtonPolicyDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(buttonPolicyAssembler.toDetail(buttonPolicyUseCase.findButtonPolicyById(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateButtonPolicyRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = buttonPolicyUseCase.createButtonPolicy(buttonPolicyAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/access/button-policy/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.BUTTON_POLICY_CREATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        buttonPolicyUseCase.deleteButtonPolicyById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.BUTTON_POLICY_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_POLICY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        buttonPolicyUseCase.deleteButtonPoliciesByIds(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.BUTTON_POLICY_DELETED.getMessage()));
    }
}
