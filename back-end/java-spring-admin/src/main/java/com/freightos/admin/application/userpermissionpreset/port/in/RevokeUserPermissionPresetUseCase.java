package com.freightos.admin.application.userpermissionpreset.port.in;

public interface RevokeUserPermissionPresetUseCase {
    /** id(user_permission_preset PK)로 부여 행을 해제(삭제)한다. */
    void revokeUserPermissionPreset(Long id);
}
