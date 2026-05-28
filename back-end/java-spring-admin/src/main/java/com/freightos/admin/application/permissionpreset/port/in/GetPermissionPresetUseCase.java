package com.freightos.admin.application.permissionpreset.port.in;

import com.freightos.admin.domain.permissionpreset.entity.PermissionPreset;

public interface GetPermissionPresetUseCase {
    /** 존재하지 않으면 NOT_FOUND 예외. */
    PermissionPreset getPermissionPresetById(Long presetId);
}
