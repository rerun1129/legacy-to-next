package com.freightos.admin.adapter.out.persistence.attributevalue;

import com.freightos.admin.application.attributevalue.command.SearchAttributeValueCommand;
import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.attributevalue.projection.AttributeValueSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttributeValuePersistenceAdapter implements AttributeValuePort {

    private final AttributeValueRepository attributeValueRepository;
    private final AttributeValueDomainToJpaMapper attributeValueDomainToJpaMapper;
    private final AttributeValueJpaToDomainMapper attributeValueJpaToDomainMapper;

    @Override
    public PagedResult<AttributeValueSummary> searchSummaries(SearchAttributeValueCommand command) {
        return attributeValueRepository.searchSummaries(command);
    }

    @Override
    public Optional<AttributeValue> findAttributeValueByKey(String attributeKey, String value) {
        return attributeValueRepository.findById(new AttributeValueId(attributeKey, value))
                .map(attributeValueJpaToDomainMapper::toDomain);
    }

    @Override
    public void save(AttributeValue attributeValue) {
        attributeValueRepository.save(attributeValueDomainToJpaMapper.toNewJpa(attributeValue));
    }

    @Override
    public void update(String attributeKey, String value, AttributeValue patchData) {
        AttributeValueJpaEntity entity = attributeValueRepository.findById(new AttributeValueId(attributeKey, value))
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_VALUE_NOT_FOUND", MessageCode.ATTRIBUTE_VALUE_NOT_FOUND.getMessage()));
        attributeValueDomainToJpaMapper.applyUpdateFields(entity, patchData);
    }

    @Override
    public void deleteAttributeValueByKey(String attributeKey, String value) {
        AttributeValueId pk = new AttributeValueId(attributeKey, value);
        if (!attributeValueRepository.existsById(pk)) {
            throw ApplicationException.notFound("ATTRIBUTE_VALUE_NOT_FOUND", MessageCode.ATTRIBUTE_VALUE_NOT_FOUND.getMessage());
        }
        attributeValueRepository.deleteById(pk);
    }

    @Override
    public boolean existsByKey(String attributeKey, String value) {
        return attributeValueRepository.existsById(new AttributeValueId(attributeKey, value));
    }

    @Override
    public boolean existsByAttributeKey(String attributeKey) {
        return attributeValueRepository.existsByIdAttributeKey(attributeKey);
    }

    @Override
    public List<AttributeValue> findActiveAttributeValuesByKey(String attributeKey) {
        return attributeValueRepository.findByIdAttributeKeyAndActiveTrue(attributeKey)
                .stream()
                .map(attributeValueJpaToDomainMapper::toDomain)
                .toList();
    }
}
