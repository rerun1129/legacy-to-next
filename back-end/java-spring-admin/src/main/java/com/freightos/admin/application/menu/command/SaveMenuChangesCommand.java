package com.freightos.admin.application.menu.command;

import java.util.List;

public record SaveMenuChangesCommand(
        List<CreateMenuCommand> creates,
        List<UpdateMenuItem> updates
) {
    /** id는 업데이트 대상 식별자. menuCode는 변경 불가이므로 제외. */
    public record UpdateMenuItem(
            Long id,
            Long parentId,
            String path,
            String label,
            String labelEn,
            String icon,
            Integer sortOrder,
            boolean active,
            String moduleCode
    ) {}
}
