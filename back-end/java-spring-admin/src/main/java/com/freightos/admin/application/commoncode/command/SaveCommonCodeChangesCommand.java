package com.freightos.admin.application.commoncode.command;

import java.util.List;

public record SaveCommonCodeChangesCommand(
        String groupCode,
        List<CreateCommonCodeItem> creates,
        List<UpdateCommonCodeItem> updates
) {
    public record CreateCommonCodeItem(
            String code,
            String label,
            String labelKo,
            Integer sortOrder,
            Boolean active
    ) {}

    public record UpdateCommonCodeItem(
            Long id,
            String label,
            String labelKo,
            Integer sortOrder,
            Boolean active
    ) {}
}
