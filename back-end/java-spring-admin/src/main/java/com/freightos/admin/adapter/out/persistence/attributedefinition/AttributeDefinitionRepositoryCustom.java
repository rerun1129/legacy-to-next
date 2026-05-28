package com.freightos.admin.adapter.out.persistence.attributedefinition;

import com.freightos.admin.application.attributedefinition.command.SearchAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.projection.AttributeDefinitionSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface AttributeDefinitionRepositoryCustom {
    PagedResult<AttributeDefinitionSummary> searchSummaries(SearchAttributeDefinitionCommand command);
    List<AutocompleteItem> autocompleteAttributeKeys(String query, int limit);
}
