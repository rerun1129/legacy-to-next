package com.freightos.admin.adapter.in.web.menupolicy;

import com.freightos.admin.adapter.in.web.menupolicy.dto.CreateMenuPolicyRequest;
import com.freightos.admin.adapter.in.web.menupolicy.dto.MenuPolicyDetailResponse;
import com.freightos.admin.adapter.in.web.menupolicy.dto.MenuPolicySummaryResponse;
import com.freightos.admin.adapter.in.web.menupolicy.dto.SearchMenuPolicyRequest;
import com.freightos.admin.application.menupolicy.port.in.MenuPolicyUseCase;
import com.freightos.admin.application.menupolicy.projection.MenuPolicySummary;
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
@RequestMapping("/api/admin/access/menu-policy")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class MenuPolicyController {

    private final MenuPolicyUseCase menuPolicyUseCase;
    private final MenuPolicyAssembler menuPolicyAssembler;

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagedResult<MenuPolicySummaryResponse>>> search(
            @Valid @RequestBody SearchMenuPolicyRequest req) {
        PagedResult<MenuPolicySummary> result = menuPolicyUseCase.searchMenuPolicies(menuPolicyAssembler.toSearchCommand(req));
        return ResponseEntity.ok(ApiResponse.of(menuPolicyAssembler.toSummaryPage(result)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuPolicyDetailResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.of(menuPolicyAssembler.toDetail(menuPolicyUseCase.findMenuPolicyById(id))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreateMenuPolicyRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = menuPolicyUseCase.createMenuPolicy(menuPolicyAssembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/access/menu-policy/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.MENU_POLICY_CREATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        menuPolicyUseCase.deleteMenuPolicyById(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MENU_POLICY_DELETED.getMessage()));
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_POLICY_DELETE')")
    public ResponseEntity<ApiResponse<Void>> bulkDelete(@Valid @RequestBody BulkDeleteRequest req) {
        menuPolicyUseCase.deleteMenuPoliciesByIds(req.ids());
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.MENU_POLICY_DELETED.getMessage()));
    }
}
