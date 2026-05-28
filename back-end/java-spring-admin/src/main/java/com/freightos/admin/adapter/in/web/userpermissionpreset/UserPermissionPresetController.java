package com.freightos.admin.adapter.in.web.userpermissionpreset;

import com.freightos.admin.adapter.in.web.userpermissionpreset.dto.AssignUserPermissionPresetRequest;
import com.freightos.admin.adapter.in.web.userpermissionpreset.dto.SearchUserPermissionPresetRequest;
import com.freightos.admin.adapter.in.web.userpermissionpreset.dto.UserPermissionPresetResponse;
import com.freightos.admin.application.permissionpreset.projection.UserPermissionPresetRow;
import com.freightos.admin.application.userpermissionpreset.port.in.AssignUserPermissionPresetUseCase;
import com.freightos.admin.application.userpermissionpreset.port.in.ListUserPermissionPresetUseCase;
import com.freightos.admin.application.userpermissionpreset.port.in.RevokeUserPermissionPresetUseCase;
import com.freightos.admin.common.response.ApiResponse;
import com.freightos.admin.common.response.MessageCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/access/user-permission-preset")
@RequiredArgsConstructor
@Validated
public class UserPermissionPresetController {

    private final AssignUserPermissionPresetUseCase assignUseCase;
    private final RevokeUserPermissionPresetUseCase revokeUseCase;
    private final ListUserPermissionPresetUseCase listUseCase;
    private final UserPermissionPresetWebAssembler assembler;

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('MENU_ADMIN_USER_LIST') or hasAuthority('MENU_ADMIN_ACCESS_PERMISSION_PRESET')")
    public ResponseEntity<ApiResponse<List<UserPermissionPresetResponse>>> search(
            @Valid @RequestBody SearchUserPermissionPresetRequest req) {
        List<UserPermissionPresetRow> rows = listUseCase.listUserPermissionPresets(req.userId());
        List<UserPermissionPresetResponse> responses = rows.stream().map(assembler::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.of(responses));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BTN_ADMIN_USER_LIST_UPDATE') or hasAuthority('BTN_ADMIN_ACCESS_PERMISSION_PRESET_UPDATE')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> assign(
            @Valid @RequestBody AssignUserPermissionPresetRequest req,
            UriComponentsBuilder uriBuilder) {
        Long id = assignUseCase.assignUserPermissionPreset(req.userId(), req.presetId());
        URI location = uriBuilder.path("/api/admin/access/user-permission-preset/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.of(Map.of("id", id), MessageCode.USER_PERMISSION_PRESET_ASSIGNED.getMessage()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BTN_ADMIN_USER_LIST_UPDATE') or hasAuthority('BTN_ADMIN_ACCESS_PERMISSION_PRESET_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> revoke(@PathVariable Long id) {
        revokeUseCase.revokeUserPermissionPreset(id);
        return ResponseEntity.ok(ApiResponse.ok(MessageCode.USER_PERMISSION_PRESET_REVOKED.getMessage()));
    }
}
