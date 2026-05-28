package com.freightos.admin.application.permissionpreset.port.in;

import com.freightos.admin.application.permissionpreset.command.AssignAttributeValuesCommand;

public interface AssignAttributeValuesToPresetUseCase {
    /**
     * 프리셋에 attribute_value 를 일괄 추가/제거한다.
     * removeRefs 를 먼저 처리한 후 addRefs 를 처리한다.
     */
    void assignAttributeValuesToPreset(Long presetId, AssignAttributeValuesCommand command);
}
