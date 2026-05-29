package com.freightos.admin.application.button.port.in;

import com.freightos.admin.application.button.command.SaveButtonChangesCommand;
import com.freightos.admin.common.response.SaveChangesResult;

public interface SaveButtonChangesUseCase {
    SaveChangesResult saveButtonChanges(SaveButtonChangesCommand command);
}
