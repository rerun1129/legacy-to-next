package com.freightos.admin.application.attributevalue.port.in;

import com.freightos.admin.application.attributevalue.command.CreateAttributeValueCommand;
import com.freightos.admin.application.attributevalue.command.SearchAttributeValueCommand;
import com.freightos.admin.application.attributevalue.command.UpdateAttributeValueCommand;
import com.freightos.admin.application.attributevalue.projection.AttributeValueSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;

public interface AttributeValueUseCase {
    PagedResult<AttributeValueSummary> searchAttributeValues(SearchAttributeValueCommand command);
    AttributeValue findAttributeValueByKey(String attributeKey, String value);
    void createAttributeValue(CreateAttributeValueCommand command);
    void updateAttributeValue(String attributeKey, String value, UpdateAttributeValueCommand command);
    void deleteAttributeValueByKey(String attributeKey, String value);
}
