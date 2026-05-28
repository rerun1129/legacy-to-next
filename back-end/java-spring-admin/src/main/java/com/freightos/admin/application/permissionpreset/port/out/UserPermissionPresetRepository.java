package com.freightos.admin.application.permissionpreset.port.out;

import java.util.List;

public interface UserPermissionPresetRepository {
    /** 사용자에게 부여된 모든 preset id 목록을 반환한다. */
    List<Long> findPresetIdsByUserId(Long userId);
    /**
     * 해당 preset 을 보유한 user 수를 반환한다.
     * 삭제 RESTRICT 체크에 사용한다.
     */
    long countByPresetId(Long presetId);
}
