package com.freightos.admin.application.permissionpreset.port.in;

import com.freightos.admin.application.permissionpreset.command.SavePermissionPresetChangesCommand;
import com.freightos.admin.common.response.SaveChangesResult;

public interface SavePermissionPresetChangesUseCase {
    SaveChangesResult savePermissionPresetChanges(SavePermissionPresetChangesCommand command);
}
