package com.freightos.admin.application.attributevalue.command;

import java.util.List;

public record SaveAttributeValueChangesCommand(
        String attributeKey,
        List<CreateAttributeValueCommand> creates,
        List<UpdateAttributeValueItem> updates,
        List<Long> deleteIds
) {
    /** attributeKey·value는 복합 UK 식별자이므로 update 항목에서 변경 불가. label/sortOrder/active만 변경 가능. */
    public record UpdateAttributeValueItem(Long id, String label, Integer sortOrder, Boolean active) {}
}
