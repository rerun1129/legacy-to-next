package com.freightos.admin.application.attributedefinition.port.in;

import com.freightos.admin.application.attributedefinition.command.CreateAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.command.SearchAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.command.UpdateAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.projection.AttributeDefinitionSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;

import java.util.List;

public interface AttributeDefinitionUseCase {
    PagedResult<AttributeDefinitionSummary> searchAttributeDefinitions(SearchAttributeDefinitionCommand command);
    AttributeDefinition findAttributeDefinitionByKey(String attributeKey);
    String createAttributeDefinition(CreateAttributeDefinitionCommand command);
    void updateAttributeDefinition(String attributeKey, UpdateAttributeDefinitionCommand command);
    void deleteAttributeDefinitionByKey(String attributeKey);
    void deleteAttributeDefinitionsByKeys(List<String> keys);
}
