package com.freightos.admin.adapter.in.web.userpermissionpreset;

import com.freightos.admin.adapter.in.web.userpermissionpreset.dto.UserPermissionPresetResponse;
import com.freightos.admin.application.permissionpreset.projection.UserPermissionPresetRow;
import org.springframework.stereotype.Component;

@Component
public class UserPermissionPresetWebAssembler {

    public UserPermissionPresetResponse toResponse(UserPermissionPresetRow row) {
        return new UserPermissionPresetResponse(
                row.id(), row.userId(), row.presetId(),
                row.presetCode(), row.presetName(), row.presetActive()
        );
    }
}
