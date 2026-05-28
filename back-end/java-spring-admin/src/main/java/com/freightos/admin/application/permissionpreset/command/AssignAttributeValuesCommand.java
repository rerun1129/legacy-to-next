package com.freightos.admin.application.permissionpreset.command;

import com.freightos.admin.domain.permissionpreset.entity.AttributeValueRef;

import java.util.List;

/**
 * 프리셋에 attribute_value 를 일괄 추가(add)하거나 제거(remove)할 때 사용한다.
 * removeRefs 를 먼저 처리한 후 addRefs 를 처리한다.
 */
public record AssignAttributeValuesCommand(
        List<AttributeValueRef> addRefs,
        List<AttributeValueRef> removeRefs
) {}
