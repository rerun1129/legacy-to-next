package com.freightos.admin.application.attributedefinition;

import com.freightos.admin.application.attributedefinition.command.CreateAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.command.SaveAttributeDefinitionChangesCommand;
import com.freightos.admin.application.attributedefinition.command.SearchAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.command.UpdateAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.port.in.AttributeDefinitionUseCase;
import com.freightos.admin.application.attributedefinition.port.in.AutocompleteAttributeKeyUseCase;
import com.freightos.admin.application.attributedefinition.port.in.SaveAttributeDefinitionChangesUseCase;
import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.attributedefinition.projection.AttributeDefinitionSummary;
import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;
import com.freightos.admin.domain.attributedefinition.entity.ValueType;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttributeDefinitionService implements AttributeDefinitionUseCase,
        SaveAttributeDefinitionChangesUseCase,
        AutocompleteAttributeKeyUseCase {

    private final AttributeDefinitionPort attributeDefinitionPort;
    private final AttributeDefinitionFactory attributeDefinitionFactory;
    private final AttributeValuePort attributeValuePort;
    private final MenuPolicyPort menuPolicyPort;
    private final ButtonPolicyPort buttonPolicyPort;

    @Override
    public PagedResult<AttributeDefinitionSummary> searchAttributeDefinitions(SearchAttributeDefinitionCommand command) {
        return attributeDefinitionPort.searchSummaries(command);
    }

    @Override
    public AttributeDefinition findAttributeDefinitionByKey(String attributeKey) {
        return attributeDefinitionPort.findAttributeDefinitionByKey(attributeKey)
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_DEFINITION_NOT_FOUND", MessageCode.ATTRIBUTE_DEFINITION_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public String createAttributeDefinition(CreateAttributeDefinitionCommand command) {
        if (attributeDefinitionPort.existsByKey(command.attributeKey())) {
            throw ApplicationException.conflict("ATTRIBUTE_DEFINITION_DUPLICATE_KEY", MessageCode.ATTRIBUTE_DEFINITION_DUPLICATE_KEY.getMessage());
        }
        AttributeDefinition attributeDefinition = attributeDefinitionFactory.from(command);
        return attributeDefinitionPort.save(attributeDefinition);
    }

    @Override
    @Transactional
    public void updateAttributeDefinition(String attributeKey, UpdateAttributeDefinitionCommand command) {
        AttributeDefinition existing = attributeDefinitionPort.findAttributeDefinitionByKey(attributeKey)
                .orElseThrow(() -> ApplicationException.notFound("ATTRIBUTE_DEFINITION_NOT_FOUND", MessageCode.ATTRIBUTE_DEFINITION_NOT_FOUND.getMessage()));
        existing.applyUpdate(command.name(), command.description(), ValueType.valueOf(command.valueType()), command.active(), command.allowMulti());
        attributeDefinitionPort.update(attributeKey, existing);
    }

    @Override
    @Transactional
    public void deleteAttributeDefinitionsByKeys(List<String> keys) {
        for (String key : keys) {
            deleteAttributeDefinitionByKey(key);
        }
    }

    @Override
    @Transactional
    public void deleteAttributeDefinitionByKey(String attributeKey) {
        if (!attributeDefinitionPort.existsByKey(attributeKey)) {
            throw ApplicationException.notFound("ATTRIBUTE_DEFINITION_NOT_FOUND", MessageCode.ATTRIBUTE_DEFINITION_NOT_FOUND.getMessage());
        }
        // 참조 데이터 존재 시 삭제 불가
        if (attributeValuePort.existsByAttributeKey(attributeKey)
                || menuPolicyPort.existsByAttributeKey(attributeKey)
                || buttonPolicyPort.existsByAttributeKey(attributeKey)) {
            throw ApplicationException.conflict("ATTRIBUTE_DEFINITION_IN_USE_CANNOT_DELETE", MessageCode.ATTRIBUTE_DEFINITION_IN_USE_CANNOT_DELETE.getMessage());
        }
        attributeDefinitionPort.deleteAttributeDefinitionByKey(attributeKey);
    }

    @Override
    @Transactional
    public SaveChangesResult saveAttributeDefinitionChanges(SaveAttributeDefinitionChangesCommand command) {
        for (String key : command.deleteKeys()) {
            deleteAttributeDefinitionByKey(key);
        }
        for (SaveAttributeDefinitionChangesCommand.UpdateAttributeDefinitionItem item : command.updates()) {
            updateAttributeDefinition(item.attributeKey(), new UpdateAttributeDefinitionCommand(item.name(), item.description(), item.valueType(), item.active(), item.allowMulti()));
        }
        for (CreateAttributeDefinitionCommand create : command.creates()) {
            createAttributeDefinition(create);
        }
        return new SaveChangesResult(command.creates().size(), command.updates().size(), command.deleteKeys().size());
    }

    @Override
    public List<AutocompleteItem> autocompleteAttributeKeys(String query, int limit) {
        return attributeDefinitionPort.autocompleteAttributeKeys(query, limit);
    }

    @Override
    public List<ModuleAttributeResult> findAttributesByModuleCode(String moduleCode) {
        List<String> keys = menuPolicyPort.findDistinctAttributeKeysByModuleCode(moduleCode);
        return keys.stream()
                .map(key -> {
                    AttributeDefinition def = attributeDefinitionPort.findAttributeDefinitionByKey(key).orElse(null);
                    if (def == null) return null;
                    List<ModuleAttributeResult.ValueEntry> valueEntries = List.of();
                    if ("ENUM".equals(def.getValueType().name())) {
                        List<AttributeValue> activeValues = attributeValuePort.findActiveAttributeValuesByKey(key);
                        valueEntries = activeValues.stream()
                                .map(v -> new ModuleAttributeResult.ValueEntry(v.getValue(), v.getLabel()))
                                .toList();
                    }
                    return new ModuleAttributeResult(def.getAttributeKey(), def.getName(), def.getValueType().name(), Boolean.TRUE.equals(def.getAllowMulti()), valueEntries);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
