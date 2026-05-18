package com.freightos.admin.application.menu;

import com.freightos.admin.application.menu.command.CreateMenuCommand;
import com.freightos.admin.domain.menu.entity.Menu;
import org.springframework.stereotype.Component;

@Component
public class MenuFactory {

    public Menu from(CreateMenuCommand command) {
        return Menu.create(
                command.menuCode(),
                command.parentId(),
                command.path(),
                command.label(),
                command.labelEn(),
                command.icon(),
                command.sortOrder(),
                command.active(),
                command.moduleCode()
        );
    }
}
