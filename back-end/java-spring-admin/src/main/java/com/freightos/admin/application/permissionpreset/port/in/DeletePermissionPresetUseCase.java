package com.freightos.admin.application.permissionpreset.port.in;

public interface DeletePermissionPresetUseCase {
    /**
     * 프리셋을 삭제한다.
     * user_permission_preset 행이 1건이라도 존재하면 CONFLICT 예외(RESTRICT 정책).
     */
    void deletePermissionPreset(Long presetId);
}
