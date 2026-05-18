package com.freightos.admin.adapter.out.persistence.attributedefinition;

import com.freightos.admin.application.attributedefinition.command.SearchAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.projection.AttributeDefinitionSummary;
import com.freightos.admin.common.response.PagedResult;

public interface AttributeDefinitionRepositoryCustom {
    PagedResult<AttributeDefinitionSummary> searchSummaries(SearchAttributeDefinitionCommand command);
}
