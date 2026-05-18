package com.freightos.admin.application.button;

import com.freightos.admin.application.button.command.CreateButtonCommand;
import com.freightos.admin.domain.button.entity.ActionType;
import com.freightos.admin.domain.button.entity.Button;
import org.springframework.stereotype.Component;

@Component
public class ButtonFactory {

    public Button from(CreateButtonCommand command) {
        return Button.create(
                command.buttonCode(),
                command.menuId(),
                command.label(),
                ActionType.valueOf(command.actionType()),
                command.apiMethod(),
                command.apiPath(),
                command.sortOrder(),
                command.active()
        );
    }
}
