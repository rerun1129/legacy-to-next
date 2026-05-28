package com.freightos.admin.application.permissionpreset.port.in;

import com.freightos.admin.application.permissionpreset.command.CreatePermissionPresetCommand;

public interface CreatePermissionPresetUseCase {
    /** 프리셋을 생성하고 생성된 preset id 를 반환한다. */
    Long createPermissionPreset(CreatePermissionPresetCommand command);
}
