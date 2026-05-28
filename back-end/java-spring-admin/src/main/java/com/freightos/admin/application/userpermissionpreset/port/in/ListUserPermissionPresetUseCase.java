package com.freightos.admin.application.userpermissionpreset.port.in;

import com.freightos.admin.application.permissionpreset.projection.UserPermissionPresetRow;

import java.util.List;

public interface ListUserPermissionPresetUseCase {
    /** userId 에 부여된 모든 프리셋 행 목록을 반환한다. */
    List<UserPermissionPresetRow> listUserPermissionPresets(Long userId);
}
