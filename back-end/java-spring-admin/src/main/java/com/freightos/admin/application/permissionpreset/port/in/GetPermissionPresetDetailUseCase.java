package com.freightos.admin.application.permissionpreset.port.in;

import com.freightos.admin.application.permissionpreset.projection.PermissionPresetDetail;

public interface GetPermissionPresetDetailUseCase {
    /**
     * 프리셋 상세(attribute_value key/value/label 포함)를 반환한다.
     * 존재하지 않으면 NOT_FOUND 예외.
     */
    PermissionPresetDetail getPermissionPresetDetail(Long presetId);
}
