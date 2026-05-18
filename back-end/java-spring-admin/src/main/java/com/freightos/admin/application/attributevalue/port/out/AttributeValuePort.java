package com.freightos.admin.application.attributevalue.port.out;

import com.freightos.admin.application.attributevalue.command.SearchAttributeValueCommand;
import com.freightos.admin.application.attributevalue.projection.AttributeValueSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;

import java.util.Optional;

public interface AttributeValuePort {
    PagedResult<AttributeValueSummary> searchSummaries(SearchAttributeValueCommand command);
    Optional<AttributeValue> findAttributeValueByKey(String attributeKey, String value);
    void save(AttributeValue attributeValue);
    void update(String attributeKey, String value, AttributeValue patchData);
    void deleteAttributeValueByKey(String attributeKey, String value);
    boolean existsByKey(String attributeKey, String value);
    boolean existsByAttributeKey(String attributeKey);
}
