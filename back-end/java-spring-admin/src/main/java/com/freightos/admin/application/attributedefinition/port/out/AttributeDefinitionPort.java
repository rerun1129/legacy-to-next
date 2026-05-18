package com.freightos.admin.application.attributedefinition.port.out;

import com.freightos.admin.application.attributedefinition.command.SearchAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.projection.AttributeDefinitionSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;

import java.util.Optional;

public interface AttributeDefinitionPort {
    PagedResult<AttributeDefinitionSummary> searchSummaries(SearchAttributeDefinitionCommand command);
    Optional<AttributeDefinition> findAttributeDefinitionByKey(String attributeKey);
    String save(AttributeDefinition attributeDefinition);
    void update(String attributeKey, AttributeDefinition patchData);
    void deleteAttributeDefinitionByKey(String attributeKey);
    boolean existsByKey(String attributeKey);
}
