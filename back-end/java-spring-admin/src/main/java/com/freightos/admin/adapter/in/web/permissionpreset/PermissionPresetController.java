package com.freightos.admin.adapter.in.web.permissionpreset;

import com.freightos.admin.adapter.in.web.permissionpreset.dto.AssignAttributeValuesRequest;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.CreatePermissionPresetRequest;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.PermissionPresetResponse;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.PermissionPresetSummaryResponse;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.SearchPermissionPresetRequest;
import com.freightos.admin.adapter.in.web.permissionpreset.dto.UpdatePermissionPresetRequest;
import com.freightos.admin.application.permissionpreset.port.in.AssignAttributeValuesToPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.CreatePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.DeletePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.GetPermissionPresetDetailUseCase;
import com.freightos.admin.application.permissionpreset.port.in.ListPermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.port.in.UpdatePermissionPresetUseCase;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetDetail;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetSummary;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/access/permission-preset")
@RequiredArgsConstructor
@Validated
public class PermissionPresetController {

    private final CreatePermissionPresetUseCase createUseCase;
    private final UpdatePermissionPresetUseCase updateUseCase;
    private final DeletePermissionPresetUseCase deleteUseCase;
    private final GetPermissionPresetDetailUseCase getDetailUseCase;
    private final ListPermissionPresetUseCase listUseCase;
    private final AssignAttributeValuesToPresetUseCase assignUseCase;
    private final PermissionPresetWebAssembler assembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_ACCESS_PERMISSION_PRESET')")
    public ResponseEntity<ApiResponse<List<PermissionPresetSummaryResponse>>> search(
            @RequestBody SearchPermissionPresetRequest req) {
        List<PermissionPresetSummary> summaries = listUseCase.listPermissionPresets(assembler.toListCommand(req));
        List<PermissionPresetSummaryResponse> responses = summaries.stream().map(assembler::toSummaryResponse).toList();
        return ResponseEntity.ok(ApiResponse.of(responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MENU_ADMIN_ACCESS_PERMISSION_PRESET')")
    public ResponseEntity<ApiResponse<PermissionPresetResponse>> getById(@PathVariable Long id) {
        PermissionPresetDetail detail = getDetailUseCase.getPermissionPresetDetail(id);
        return ResponseEntity.ok(ApiResponse.of(assembler.toDetailResponse(detail)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_PERMISSION_PRESET_CREATE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> create(
            @Valid @RequestBody CreatePermissionPresetRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = createUseCase.createPermissionPreset(assembler.toCreateCommand(req));
        URI location = uriBuilder.path("/api/admin/access/permission-preset/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.PERMISSION_PRESET_CREATED.getMessage()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_PERMISSION_PRESET_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePermissionPresetRequest req) {
        updateUseCase.updatePermissionPreset(id, assembler.toUpdateCommand(req));
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.PERMISSION_PRESET_UPDATED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_PERMISSION_PRESET_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteById(@PathVariable Long id) {
        deleteUseCase.deletePermissionPreset(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.PERMISSION_PRESET_DELETED.getMessage()));
    }

    @PostMapping("/{id}/attribute-values")
    @PreAuthorize("hasAuthority('BTN_ADMIN_ACCESS_PERMISSION_PRESET_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> assignAttributeValues(
            @PathVariable Long id,
            @Valid @RequestBody AssignAttributeValuesRequest req) {
        assignUseCase.assignAttributeValuesToPreset(id, assembler.toAssignCommand(req));
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
