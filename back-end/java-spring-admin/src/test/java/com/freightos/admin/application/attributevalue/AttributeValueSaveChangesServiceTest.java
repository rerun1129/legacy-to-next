package com.freightos.admin.application.attributevalue;

import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.attributevalue.command.CreateAttributeValueCommand;
import com.freightos.admin.application.attributevalue.command.SaveAttributeValueChangesCommand;
import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;
import com.freightos.admin.domain.attributedefinition.entity.ValueType;
import com.freightos.admin.domain.attributevalue.entity.AttributeValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AttributeValueSaveChangesServiceTest {

    @Mock
    private AttributeValuePort attributeValuePort;

    @Mock
    private AttributeDefinitionPort attributeDefinitionPort;

    @InjectMocks
    private AttributeValueService service;

    // --- fixture helpers ---

    private static AttributeDefinition enumDefinition() {
        return AttributeDefinition.create("dept", "부서", null, ValueType.ENUM, true, false);
    }

    private static AttributeValue existingValue(Long id) {
        AttributeValue av = AttributeValue.create("dept", "DEV", "개발", 1, true);
        av.assignIdentity(id, null, null, null, null);
        return av;
    }

    // --- saveAttributeValueChanges ---

    @Test
    void saveChanges_emptyAll_returnsZeroCounts() {
        SaveAttributeValueChangesCommand command = new SaveAttributeValueChangesCommand("dept", List.of(), List.of(), List.of());

        SaveChangesResult result = service.saveAttributeValueChanges(command);

        assertThat(result.createdCount()).isZero();
        assertThat(result.updatedCount()).isZero();
        assertThat(result.deletedCount()).isZero();
    }

    @Test
    void saveChanges_creates_validEnum_callsSave() {
        CreateAttributeValueCommand create = new CreateAttributeValueCommand("dept", "QA", "QA팀", 2, true);
        SaveAttributeValueChangesCommand command = new SaveAttributeValueChangesCommand("dept", List.of(create), List.of(), List.of());

        given(attributeDefinitionPort.findAttributeDefinitionByKey("dept")).willReturn(Optional.of(enumDefinition()));
        given(attributeValuePort.existsByKey("dept", "QA")).willReturn(false);

        SaveChangesResult result = service.saveAttributeValueChanges(command);

        assertThat(result.createdCount()).isEqualTo(1);
        then(attributeValuePort).should().save(any());
    }

    @Test
    void saveChanges_creates_typeNotEnum_throwsConflict() {
        CreateAttributeValueCommand create = new CreateAttributeValueCommand("level", "HIGH", "높음", 1, true);
        SaveAttributeValueChangesCommand command = new SaveAttributeValueChangesCommand("level", List.of(create), List.of(), List.of());
        AttributeDefinition stringDef = AttributeDefinition.create("level", "레벨", null, ValueType.STRING, true, false);

        given(attributeDefinitionPort.findAttributeDefinitionByKey("level")).willReturn(Optional.of(stringDef));

        assertThatThrownBy(() -> service.saveAttributeValueChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        then(attributeValuePort).should(never()).save(any());
    }

    @Test
    void saveChanges_creates_definitionNotFound_throwsNotFound() {
        CreateAttributeValueCommand create = new CreateAttributeValueCommand("unknown", "VAL", "값", 1, true);
        SaveAttributeValueChangesCommand command = new SaveAttributeValueChangesCommand("unknown", List.of(create), List.of(), List.of());

        given(attributeDefinitionPort.findAttributeDefinitionByKey("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveAttributeValueChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void saveChanges_creates_duplicateValue_throwsConflict() {
        CreateAttributeValueCommand create = new CreateAttributeValueCommand("dept", "DEV", "개발", 1, true);
        SaveAttributeValueChangesCommand command = new SaveAttributeValueChangesCommand("dept", List.of(create), List.of(), List.of());

        given(attributeDefinitionPort.findAttributeDefinitionByKey("dept")).willReturn(Optional.of(enumDefinition()));
        given(attributeValuePort.existsByKey("dept", "DEV")).willReturn(true);

        assertThatThrownBy(() -> service.saveAttributeValueChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void saveChanges_updates_callsUpdateById() {
        AttributeValue existing = existingValue(10L);
        SaveAttributeValueChangesCommand.UpdateAttributeValueItem item =
                new SaveAttributeValueChangesCommand.UpdateAttributeValueItem(10L, "개발팀", 1, true);
        SaveAttributeValueChangesCommand command = new SaveAttributeValueChangesCommand("dept", List.of(), List.of(item), List.of());

        given(attributeValuePort.findAttributeValueById(10L)).willReturn(Optional.of(existing));

        SaveChangesResult result = service.saveAttributeValueChanges(command);

        assertThat(result.updatedCount()).isEqualTo(1);
        then(attributeValuePort).should().updateById(10L, existing);
    }

    @Test
    void saveChanges_updates_notFound_throwsNotFound() {
        SaveAttributeValueChangesCommand.UpdateAttributeValueItem item =
                new SaveAttributeValueChangesCommand.UpdateAttributeValueItem(999L, "없음", 1, true);
        SaveAttributeValueChangesCommand command = new SaveAttributeValueChangesCommand("dept", List.of(), List.of(item), List.of());

        given(attributeValuePort.findAttributeValueById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveAttributeValueChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void saveChanges_deletes_callsDeleteById() {
        AttributeValue existing = existingValue(5L);
        SaveAttributeValueChangesCommand command = new SaveAttributeValueChangesCommand("dept", List.of(), List.of(), List.of(5L));

        given(attributeValuePort.findAttributeValueById(5L)).willReturn(Optional.of(existing));

        SaveChangesResult result = service.saveAttributeValueChanges(command);

        assertThat(result.deletedCount()).isEqualTo(1);
        then(attributeValuePort).should().deleteAttributeValueById(5L);
    }

    @Test
    void saveChanges_deletes_notFound_throwsNotFound() {
        SaveAttributeValueChangesCommand command = new SaveAttributeValueChangesCommand("dept", List.of(), List.of(), List.of(999L));

        given(attributeValuePort.findAttributeValueById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.saveAttributeValueChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void saveChanges_orderIsDeleteThenUpdateThenCreate() {
        // 삭제 → 수정 → 생성 순서 검증
        AttributeValue toDelete = existingValue(1L);
        AttributeValue toUpdate = existingValue(2L);
        CreateAttributeValueCommand create = new CreateAttributeValueCommand("dept", "NEW", "신규", 3, true);
        SaveAttributeValueChangesCommand.UpdateAttributeValueItem updateItem =
                new SaveAttributeValueChangesCommand.UpdateAttributeValueItem(2L, "수정된 개발", 1, true);
        SaveAttributeValueChangesCommand command = new SaveAttributeValueChangesCommand(
                "dept", List.of(create), List.of(updateItem), List.of(1L));

        given(attributeValuePort.findAttributeValueById(1L)).willReturn(Optional.of(toDelete));
        given(attributeValuePort.findAttributeValueById(2L)).willReturn(Optional.of(toUpdate));
        given(attributeDefinitionPort.findAttributeDefinitionByKey("dept")).willReturn(Optional.of(enumDefinition()));
        given(attributeValuePort.existsByKey("dept", "NEW")).willReturn(false);

        SaveChangesResult result = service.saveAttributeValueChanges(command);

        assertThat(result.deletedCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isEqualTo(1);
        assertThat(result.createdCount()).isEqualTo(1);
    }
}
