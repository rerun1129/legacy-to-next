package com.freightos.admin.adapter.out.persistence.attributevalue;

import com.freightos.admin.application.attributevalue.command.SearchAttributeValueCommand;
import com.freightos.admin.application.attributevalue.projection.AttributeValueSummary;
import com.freightos.admin.common.response.PagedResult;

public interface AttributeValueRepositoryCustom {
    PagedResult<AttributeValueSummary> searchSummaries(SearchAttributeValueCommand command);
}
