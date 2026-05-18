package com.freightos.admin.application.attributevalue;

import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.attributevalue.command.CreateAttributeValueCommand;
import com.freightos.admin.application.attributevalue.command.SearchAttributeValueCommand;
import com.freightos.admin.application.attributevalue.command.UpdateAttributeValueCommand;
import com.freightos.admin.application.attributevalue.port.in.AttributeValueUseCase;
import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.attributevalue.projection.AttributeValueSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;
import com.freightos.admin.domain.attributedefinition.entity.ValueType;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttributeValueService implements AttributeValueUseCase {

    private final AttributeValuePort attributeValuePort;
    private final AttributeDefinitionPort attributeDefinitionPort;

    @Override
    public PagedResult<AttributeValueSummary> searchAttributeValues(SearchAttributeValueCommand command) {
        return attributeValuePort.searchSummaries(command);
    }

    @Override
    public AttributeValue findAttributeValueByKey(String attributeKey, String value) {
        return attributeValuePort.findAttributeValueByKey(attributeKey, value)
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_VALUE_NOT_FOUND", MessageCode.ATTRIBUTE_VALUE_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public void createAttributeValue(CreateAttributeValueCommand command) {
        // 부모 AttributeDefinition이 ENUM 타입이어야만 값 등록 가능
        AttributeDefinition definition = attributeDefinitionPort.findAttributeDefinitionByKey(command.attributeKey())
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_DEFINITION_NOT_FOUND", MessageCode.ATTRIBUTE_DEFINITION_NOT_FOUND.getMessage()));
        if (definition.getValueType() != ValueType.ENUM) {
            throw ApplicationException.conflict("ATTRIBUTE_VALUE_TYPE_NOT_ENUM", MessageCode.ATTRIBUTE_VALUE_TYPE_NOT_ENUM.getMessage());
        }
        if (attributeValuePort.existsByKey(command.attributeKey(), command.value())) {
            throw ApplicationException.conflict("ATTRIBUTE_VALUE_DUPLICATE", MessageCode.ATTRIBUTE_VALUE_DUPLICATE.getMessage());
        }
        AttributeValue attributeValue = AttributeValue.create(command.attributeKey(), command.value(), command.label(), command.sortOrder(), command.active());
        attributeValuePort.save(attributeValue);
    }

    @Override
    @Transactional
    public void updateAttributeValue(String attributeKey, String value, UpdateAttributeValueCommand command) {
        AttributeValue existing = attributeValuePort.findAttributeValueByKey(attributeKey, value)
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_VALUE_NOT_FOUND", MessageCode.ATTRIBUTE_VALUE_NOT_FOUND.getMessage()));
        existing.applyUpdate(command.label(), command.sortOrder(), command.active());
        attributeValuePort.update(attributeKey, value, existing);
    }

    @Override
    @Transactional
    public void deleteAttributeValueByKey(String attributeKey, String value) {
        if (!attributeValuePort.existsByKey(attributeKey, value)) {
            throw ApplicationException.notFound("ATTRIBUTE_VALUE_NOT_FOUND", MessageCode.ATTRIBUTE_VALUE_NOT_FOUND.getMessage());
        }
        attributeValuePort.deleteAttributeValueByKey(attributeKey, value);
    }
}
