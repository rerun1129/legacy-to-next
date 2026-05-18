package com.freightos.admin.application.attributedefinition;

import com.freightos.admin.application.attributedefinition.command.CreateAttributeDefinitionCommand;
import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.attributedefinition.entity.AttributeDefinition;
import com.freightos.admin.domain.attributedefinition.entity.ValueType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class AttributeDefinitionServiceTest {

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
    private AttributeDefinitionService attributeDefinitionService;

    @Test
    void createAttributeDefinition_duplicate_throwsConflict() {
        CreateAttributeDefinitionCommand command = new CreateAttributeDefinitionCommand("dept", "부서", null, "ENUM", true, false);
        given(attributeDefinitionPort.existsByKey("dept")).willReturn(true);

        assertThatThrownBy(() -> attributeDefinitionService.createAttributeDefinition(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createAttributeDefinition_noDuplicate_savesAndReturnsKey() {
        CreateAttributeDefinitionCommand command = new CreateAttributeDefinitionCommand("dept", "부서", null, "ENUM", true, false);
        AttributeDefinition domain = AttributeDefinition.create("dept", "부서", null, ValueType.ENUM, true, false);
        given(attributeDefinitionPort.existsByKey("dept")).willReturn(false);
        given(attributeDefinitionFactory.from(command)).willReturn(domain);
        given(attributeDefinitionPort.save(domain)).willReturn("dept");

        String key = attributeDefinitionService.createAttributeDefinition(command);

        assertThat(key).isEqualTo("dept");
    }

    @Test
    void deleteAttributeDefinitionByKey_inUse_throwsConflict() {
        given(attributeDefinitionPort.existsByKey("dept")).willReturn(true);
        given(attributeValuePort.existsByAttributeKey("dept")).willReturn(true);

        assertThatThrownBy(() -> attributeDefinitionService.deleteAttributeDefinitionByKey("dept"))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void deleteAttributeDefinitionByKey_notInUse_callsDelete() {
        given(attributeDefinitionPort.existsByKey("dept")).willReturn(true);
        given(attributeValuePort.existsByAttributeKey("dept")).willReturn(false);
        given(menuPolicyPort.existsByAttributeKey("dept")).willReturn(false);
        given(buttonPolicyPort.existsByAttributeKey("dept")).willReturn(false);

        attributeDefinitionService.deleteAttributeDefinitionByKey("dept");

        then(attributeDefinitionPort).should().deleteAttributeDefinitionByKey("dept");
    }

    @Test
    void findAttributeDefinitionByKey_notFound_throwsNotFound() {
        given(attributeDefinitionPort.findAttributeDefinitionByKey("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> attributeDefinitionService.findAttributeDefinitionByKey("unknown"))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
