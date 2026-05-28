package com.freightos.admin.application.attributedefinition.port.in;

import com.freightos.admin.application.attributedefinition.command.SaveAttributeDefinitionChangesCommand;
import com.freightos.admin.common.response.SaveChangesResult;

public interface SaveAttributeDefinitionChangesUseCase {
    SaveChangesResult saveAttributeDefinitionChanges(SaveAttributeDefinitionChangesCommand command);
}
