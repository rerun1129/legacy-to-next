package com.freightos.admin.application.userpermissionpreset.port.in;

public interface AssignUserPermissionPresetUseCase {
    /** 사용자에게 프리셋을 부여하고 생성된 PK 를 반환한다. */
    Long assignUserPermissionPreset(Long userId, Long presetId);
}
