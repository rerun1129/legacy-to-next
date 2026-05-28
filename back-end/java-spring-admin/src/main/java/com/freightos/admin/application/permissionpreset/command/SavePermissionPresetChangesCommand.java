package com.freightos.admin.application.permissionpreset.command;

import java.util.List;

public record SavePermissionPresetChangesCommand(
        List<CreatePermissionPresetCommand> creates,
        List<UpdatePermissionPresetItem> updates,
        List<Long> deleteIds
) {
    /** code 는 식별자이므로 update 항목에서 의도적으로 제외한다. */
    public record UpdatePermissionPresetItem(Long id, String name, String description, boolean active) {}
}
