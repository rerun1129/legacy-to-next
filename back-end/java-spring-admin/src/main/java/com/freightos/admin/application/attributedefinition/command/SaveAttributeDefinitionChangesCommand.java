package com.freightos.admin.application.attributedefinition.command;

import java.util.List;

public record SaveAttributeDefinitionChangesCommand(
        List<CreateAttributeDefinitionCommand> creates,
        List<UpdateAttributeDefinitionItem> updates,
        List<String> deleteKeys
) {
    /** attributeKey는 식별자이므로 update 항목에서 변경 대상 제외. name/valueType/allowMulti/active만 변경 가능. */
    public record UpdateAttributeDefinitionItem(
            String attributeKey,
            String name,
            String description,
            String valueType,
            Boolean active,
            Boolean allowMulti
    ) {}
}
