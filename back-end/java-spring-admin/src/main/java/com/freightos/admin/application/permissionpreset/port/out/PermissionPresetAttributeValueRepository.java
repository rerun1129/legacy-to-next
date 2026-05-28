package com.freightos.admin.application.permissionpreset.port.out;

import com.freightos.admin.domain.permissionpreset.entity.AttributeValueRef;

import java.util.List;

public interface PermissionPresetAttributeValueRepository {
    /** 프리셋에 속한 attribute_value 참조 목록을 반환한다. */
    List<AttributeValueRef> findAttributeValueRefsByPresetId(Long presetId);
    /** 프리셋에 attribute_value 를 일괄 추가한다. 이미 존재하는 조합은 무시한다. */
    void saveAllByPresetId(Long presetId, List<AttributeValueRef> refs);
    /** 프리셋에서 지정한 attribute_value 를 제거한다. */
    void deleteByPresetIdAndRefsIn(Long presetId, List<AttributeValueRef> refs);
}
