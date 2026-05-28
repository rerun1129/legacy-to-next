package com.freightos.admin.application.permissionpreset.port.in;

import com.freightos.admin.application.permissionpreset.command.UpdatePermissionPresetCommand;

public interface UpdatePermissionPresetUseCase {
    void updatePermissionPreset(Long presetId, UpdatePermissionPresetCommand command);
}
