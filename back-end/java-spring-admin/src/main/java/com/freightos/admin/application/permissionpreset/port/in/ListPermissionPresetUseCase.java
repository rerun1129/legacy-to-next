package com.freightos.admin.application.permissionpreset.port.in;

import com.freightos.admin.application.permissionpreset.command.ListPermissionPresetCommand;
import com.freightos.admin.application.permissionpreset.projection.PermissionPresetSummary;

import java.util.List;

public interface ListPermissionPresetUseCase {
    List<PermissionPresetSummary> listPermissionPresets(ListPermissionPresetCommand command);
}
