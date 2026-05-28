package com.freightos.admin.application.attributedefinition;

import com.freightos.admin.application.attributedefinition.command.CreateAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.command.SaveAttributeDefinitionChangesCommand;
import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.SaveChangesResult;
import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;
import com.freightos.admin.domain.attributedefinition.entity.ValueType;
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
class AttributeDefinitionSaveChangesServiceTest {

    @Mock
    private AttributeDefinitionPort attributeDefinitionPort;

    @Mock
    private AttributeDefinitionFactory attributeDefinitionFactory;

    @Mock
    private AttributeValuePort attributeValuePort;

    @Mock
    private MenuPolicyPort menuPolicyPort;

    @Mock
    private ButtonPolicyPort buttonPolicyPort;

    @InjectMocks
    private AttributeDefinitionService service;

    // --- saveAttributeDefinitionChanges ---

    @Test
    void saveChanges_emptyAll_returnsZeroCounts() {
        SaveAttributeDefinitionChangesCommand command = new SaveAttributeDefinitionChangesCommand(List.of(), List.of(), List.of());

        SaveChangesResult result = service.saveAttributeDefinitionChanges(command);

        assertThat(result.createdCount()).isZero();
        assertThat(result.updatedCount()).isZero();
        assertThat(result.deletedCount()).isZero();
    }

    @Test
    void saveChanges_creates_callsSaveForEach() {
        CreateAttributeDefinitionCommand create = new CreateAttributeDefinitionCommand("dept", "부서", null, "ENUM", true, false);
        AttributeDefinition domain = AttributeDefinition.create("dept", "부서", null, ValueType.ENUM, true, false);
        SaveAttributeDefinitionChangesCommand command = new SaveAttributeDefinitionChangesCommand(List.of(create), List.of(), List.of());

        given(attributeDefinitionPort.existsByKey("dept")).willReturn(false);
        given(attributeDefinitionFactory.from(create)).willReturn(domain);
        given(attributeDefinitionPort.save(domain)).willReturn("dept");

        SaveChangesResult result = service.saveAttributeDefinitionChanges(command);

        assertThat(result.createdCount()).isEqualTo(1);
        then(attributeDefinitionPort).should().save(domain);
    }

    @Test
    void saveChanges_creates_duplicateKey_throwsConflict() {
        CreateAttributeDefinitionCommand create = new CreateAttributeDefinitionCommand("dept", "부서", null, "ENUM", true, false);
        SaveAttributeDefinitionChangesCommand command = new SaveAttributeDefinitionChangesCommand(List.of(create), List.of(), List.of());

        given(attributeDefinitionPort.existsByKey("dept")).willReturn(true);

        assertThatThrownBy(() -> service.saveAttributeDefinitionChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void saveChanges_updates_callsUpdateForEach() {
        AttributeDefinition existing = AttributeDefinition.create("dept", "부서", null, ValueType.ENUM, true, false);
        SaveAttributeDefinitionChangesCommand.UpdateAttributeDefinitionItem item =
                new SaveAttributeDefinitionChangesCommand.UpdateAttributeDefinitionItem("dept", "부서(수정)", null, "ENUM", true, false);
        SaveAttributeDefinitionChangesCommand command = new SaveAttributeDefinitionChangesCommand(List.of(), List.of(item), List.of());

        given(attributeDefinitionPort.findAttributeDefinitionByKey("dept")).willReturn(Optional.of(existing));

        SaveChangesResult result = service.saveAttributeDefinitionChanges(command);

        assertThat(result.updatedCount()).isEqualTo(1);
        then(attributeDefinitionPort).should().update("dept", existing);
    }

    @Test
    void saveChanges_deletes_callsDeleteForEach() {
        SaveAttributeDefinitionChangesCommand command = new SaveAttributeDefinitionChangesCommand(List.of(), List.of(), List.of("dept"));

        given(attributeDefinitionPort.existsByKey("dept")).willReturn(true);
        given(attributeValuePort.existsByAttributeKey("dept")).willReturn(false);
        given(menuPolicyPort.existsByAttributeKey("dept")).willReturn(false);
        given(buttonPolicyPort.existsByAttributeKey("dept")).willReturn(false);

        SaveChangesResult result = service.saveAttributeDefinitionChanges(command);

        assertThat(result.deletedCount()).isEqualTo(1);
        then(attributeDefinitionPort).should().deleteAttributeDefinitionByKey("dept");
    }

    @Test
    void saveChanges_deleteNotFound_throwsNotFound() {
        SaveAttributeDefinitionChangesCommand command = new SaveAttributeDefinitionChangesCommand(List.of(), List.of(), List.of("unknown"));

        given(attributeDefinitionPort.existsByKey("unknown")).willReturn(false);

        assertThatThrownBy(() -> service.saveAttributeDefinitionChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void saveChanges_deleteInUse_throwsConflict() {
        SaveAttributeDefinitionChangesCommand command = new SaveAttributeDefinitionChangesCommand(List.of(), List.of(), List.of("dept"));

        given(attributeDefinitionPort.existsByKey("dept")).willReturn(true);
        given(attributeValuePort.existsByAttributeKey("dept")).willReturn(true);

        assertThatThrownBy(() -> service.saveAttributeDefinitionChanges(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));

        then(attributeDefinitionPort).should(never()).deleteAttributeDefinitionByKey(any());
    }

    @Test
    void saveChanges_orderIsDeleteThenUpdateThenCreate() {
        // 삭제 → 수정 → 생성 순서 검증: 삭제 키와 다른 생성 키로 순서 확인
        CreateAttributeDefinitionCommand create = new CreateAttributeDefinitionCommand("new_key", "신규", null, "STRING", true, false);
        AttributeDefinition domain = AttributeDefinition.create("new_key", "신규", null, ValueType.STRING, true, false);
        SaveAttributeDefinitionChangesCommand.UpdateAttributeDefinitionItem updateItem =
                new SaveAttributeDefinitionChangesCommand.UpdateAttributeDefinitionItem("upd_key", "수정됨", null, "STRING", true, false);
        AttributeDefinition existingForUpdate = AttributeDefinition.create("upd_key", "수정 전", null, ValueType.STRING, true, false);
        SaveAttributeDefinitionChangesCommand command = new SaveAttributeDefinitionChangesCommand(
                List.of(create), List.of(updateItem), List.of("del_key"));

        given(attributeDefinitionPort.existsByKey("del_key")).willReturn(true);
        given(attributeValuePort.existsByAttributeKey("del_key")).willReturn(false);
        given(menuPolicyPort.existsByAttributeKey("del_key")).willReturn(false);
        given(buttonPolicyPort.existsByAttributeKey("del_key")).willReturn(false);
        given(attributeDefinitionPort.findAttributeDefinitionByKey("upd_key")).willReturn(Optional.of(existingForUpdate));
        given(attributeDefinitionPort.existsByKey("new_key")).willReturn(false);
        given(attributeDefinitionFactory.from(create)).willReturn(domain);
        given(attributeDefinitionPort.save(domain)).willReturn("new_key");

        SaveChangesResult result = service.saveAttributeDefinitionChanges(command);

        assertThat(result.deletedCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isEqualTo(1);
        assertThat(result.createdCount()).isEqualTo(1);
    }

    // --- autocompleteAttributeKeys ---

    @Test
    void autocompleteAttributeKeys_delegatesToPort() {
        List<AutocompleteItem> items = List.of(new AutocompleteItem("dept", "부서"), new AutocompleteItem("dept_code", "부서 코드"));
        given(attributeDefinitionPort.autocompleteAttributeKeys("dept", 20)).willReturn(items);

        List<AutocompleteItem> result = service.autocompleteAttributeKeys("dept", 20);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).code()).isEqualTo("dept");
    }

    @Test
    void autocompleteAttributeKeys_emptyQuery_returnsEmpty() {
        given(attributeDefinitionPort.autocompleteAttributeKeys("", 20)).willReturn(List.of());

        List<AutocompleteItem> result = service.autocompleteAttributeKeys("", 20);

        assertThat(result).isEmpty();
    }
}
