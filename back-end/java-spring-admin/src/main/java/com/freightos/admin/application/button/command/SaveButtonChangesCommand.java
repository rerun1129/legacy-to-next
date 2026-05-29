package com.freightos.admin.application.button.command;

import java.util.List;

public record SaveButtonChangesCommand(
        List<CreateButtonCommand> creates,
        List<UpdateButtonItem> updates
) {
    /** id는 업데이트 대상 식별자. buttonCode는 변경 불가이므로 제외. */
    public record UpdateButtonItem(
            Long id,
            Long menuId,
            String label,
            String actionType,
            String apiMethod,
            String apiPath,
            Integer sortOrder,
            Boolean active
    ) {}
}
