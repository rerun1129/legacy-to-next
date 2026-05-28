package com.freightos.admin.adapter.out.persistence.attributedefinition;

import com.freightos.admin.application.attributedefinition.command.SearchAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.attributedefinition.projection.AttributeDefinitionSummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AttributeDefinitionPersistenceAdapter implements AttributeDefinitionPort {

    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeDefinitionDomainToJpaMapper attributeDefinitionDomainToJpaMapper;
    private final AttributeDefinitionJpaToDomainMapper attributeDefinitionJpaToDomainMapper;

    @Override
    public PagedResult<AttributeDefinitionSummary> searchSummaries(SearchAttributeDefinitionCommand command) {
        return attributeDefinitionRepository.searchSummaries(command);
    }

    @Override
    public Optional<AttributeDefinition> findAttributeDefinitionByKey(String attributeKey) {
        return attributeDefinitionRepository.findByAttributeKey(attributeKey)
                .map(attributeDefinitionJpaToDomainMapper::toDomain);
    }

    @Override
    public String save(AttributeDefinition attributeDefinition) {
        AttributeDefinitionJpaEntity entity = attributeDefinitionDomainToJpaMapper.toNewJpa(attributeDefinition);
        attributeDefinitionRepository.save(entity);
        return entity.getAttributeKey();
    }

    @Override
    public void update(String attributeKey, AttributeDefinition patchData) {
        AttributeDefinitionJpaEntity entity = attributeDefinitionRepository.findByAttributeKey(attributeKey)
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_DEFINITION_NOT_FOUND", MessageCode.ATTRIBUTE_DEFINITION_NOT_FOUND.getMessage()));
        attributeDefinitionDomainToJpaMapper.applyUpdateFields(entity, patchData);
    }

    @Override
    public void deleteAttributeDefinitionByKey(String attributeKey) {
        AttributeDefinitionJpaEntity entity = attributeDefinitionRepository.findByAttributeKey(attributeKey)
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_DEFINITION_NOT_FOUND", MessageCode.ATTRIBUTE_DEFINITION_NOT_FOUND.getMessage()));
        attributeDefinitionRepository.delete(entity);
    }

    @Override
    public boolean existsByKey(String attributeKey) {
        return attributeDefinitionRepository.existsByAttributeKey(attributeKey);
    }

    @Override
    public List<AutocompleteItem> autocompleteAttributeKeys(String query, int limit) {
        return attributeDefinitionRepository.autocompleteAttributeKeys(query, limit);
    }
}
