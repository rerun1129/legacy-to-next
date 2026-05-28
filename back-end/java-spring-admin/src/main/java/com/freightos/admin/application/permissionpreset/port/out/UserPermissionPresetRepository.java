package com.freightos.admin.application.permissionpreset.port.out;

import com.freightos.admin.application.permissionpreset.projection.UserPermissionPresetRow;

import java.util.List;

public interface UserPermissionPresetRepository {
    /** 사용자에게 부여된 모든 preset id 목록을 반환한다. */
    List<Long> findPresetIdsByUserId(Long userId);

    /** userId 기준 부여 행 전체를 상세 row 로 반환한다. */
    List<UserPermissionPresetRow> findRowsByUserId(Long userId);

    /**
     * 해당 preset 을 보유한 user 수를 반환한다.
     * 삭제 RESTRICT 체크에 사용한다.
     */
    long countByPresetId(Long presetId);

    /** 이미 부여 여부를 확인한다. */
    boolean existsByUserIdAndPresetId(Long userId, Long presetId);

    /** 부여 행을 신규 저장하고 생성된 PK 를 반환한다. */
    Long saveUserPermissionPreset(Long userId, Long presetId);

    /** id 로 부여 행을 삭제한다. */
    void deleteUserPermissionPresetById(Long id);

    /** id 로 존재 여부를 확인한다. */
    boolean existsUserPermissionPresetById(Long id);
}
