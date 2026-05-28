package com.freightos.admin.application.attributevalue.port.in;

import com.freightos.admin.application.attributevalue.command.SaveAttributeValueChangesCommand;
import com.freightos.admin.common.response.SaveChangesResult;

public interface SaveAttributeValueChangesUseCase {
    SaveChangesResult saveAttributeValueChanges(SaveAttributeValueChangesCommand command);
}
