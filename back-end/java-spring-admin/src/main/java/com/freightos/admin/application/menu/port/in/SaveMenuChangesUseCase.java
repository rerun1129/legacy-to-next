package com.freightos.admin.application.menu.port.in;

import com.freightos.admin.application.menu.command.SaveMenuChangesCommand;
import com.freightos.admin.common.response.SaveChangesResult;

public interface SaveMenuChangesUseCase {
    SaveChangesResult saveMenuChanges(SaveMenuChangesCommand command);
}
