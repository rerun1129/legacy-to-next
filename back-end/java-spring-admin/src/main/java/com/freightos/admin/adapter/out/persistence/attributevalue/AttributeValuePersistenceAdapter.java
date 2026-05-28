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

import java.util.Collection;
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
        return attributeValueRepository.findByAttributeKeyAndValue(attributeKey, value)
                .map(attributeValueJpaToDomainMapper::toDomain);
    }

    @Override
    public Optional<AttributeValue> findAttributeValueById(Long id) {
        return attributeValueRepository.findById(id)
                .map(attributeValueJpaToDomainMapper::toDomain);
    }

    @Override
    public List<AttributeValue> findAttributeValuesByIds(Collection<Long> ids) {
        return attributeValueRepository.findAllById(ids)
                .stream()
                .map(attributeValueJpaToDomainMapper::toDomain)
                .toList();
    }

    @Override
    public void save(AttributeValue attributeValue) {
        attributeValueRepository.save(attributeValueDomainToJpaMapper.toNewJpa(attributeValue));
    }

    @Override
    public void update(String attributeKey, String value, AttributeValue patchData) {
        AttributeValueJpaEntity entity = attributeValueRepository.findByAttributeKeyAndValue(attributeKey, value)
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_VALUE_NOT_FOUND", MessageCode.ATTRIBUTE_VALUE_NOT_FOUND.getMessage()));
        attributeValueDomainToJpaMapper.applyUpdateFields(entity, patchData);
    }

    @Override
    public void updateById(Long id, AttributeValue patchData) {
        AttributeValueJpaEntity entity = attributeValueRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_VALUE_NOT_FOUND", MessageCode.ATTRIBUTE_VALUE_NOT_FOUND.getMessage()));
        attributeValueDomainToJpaMapper.applyUpdateFields(entity, patchData);
    }

    @Override
    public void deleteAttributeValueByKey(String attributeKey, String value) {
        AttributeValueJpaEntity entity = attributeValueRepository.findByAttributeKeyAndValue(attributeKey, value)
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_VALUE_NOT_FOUND", MessageCode.ATTRIBUTE_VALUE_NOT_FOUND.getMessage()));
        attributeValueRepository.deleteById(entity.getId());
    }

    @Override
    public void deleteAttributeValueById(Long id) {
        attributeValueRepository.deleteById(id);
    }

    @Override
    public boolean existsByKey(String attributeKey, String value) {
        return attributeValueRepository.existsByAttributeKeyAndValue(attributeKey, value);
    }

    @Override
    public boolean existsByAttributeKey(String attributeKey) {
        return attributeValueRepository.existsByAttributeKey(attributeKey);
    }

    @Override
    public boolean existsByAttributeKeyAndValueExcludingId(String attributeKey, String value, Long excludeId) {
        return attributeValueRepository.existsByAttributeKeyAndValueAndIdNot(attributeKey, value, excludeId);
    }

    @Override
    public List<AttributeValue> findActiveAttributeValuesByKey(String attributeKey) {
        return attributeValueRepository.findByAttributeKeyAndActiveTrue(attributeKey)
                .stream()
                .map(attributeValueJpaToDomainMapper::toDomain)
                .toList();
    }
}
