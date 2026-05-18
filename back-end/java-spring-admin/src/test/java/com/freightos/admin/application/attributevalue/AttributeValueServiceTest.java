package com.freightos.admin.application.attributevalue;

import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.attributevalue.command.CreateAttributeValueCommand;
import com.freightos.admin.application.attributevalue.port.out.AttributeValuePort;
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
class AttributeValueServiceTest {

    @Mock
    private AttributeValuePort attributeValuePort;

    @Mock
    private AttributeDefinitionPort attributeDefinitionPort;

    @InjectMocks
    private AttributeValueService attributeValueService;

    @Test
    void createAttributeValue_typeNotEnum_throwsConflict() {
        CreateAttributeValueCommand command = new CreateAttributeValueCommand("level", "HIGH", "높음", 1, true);
        AttributeDefinition stringDef = AttributeDefinition.create("level", "레벨", null, ValueType.STRING, true, false);
        given(attributeDefinitionPort.findAttributeDefinitionByKey("level")).willReturn(Optional.of(stringDef));

        assertThatThrownBy(() -> attributeValueService.createAttributeValue(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createAttributeValue_duplicateKey_throwsConflict() {
        CreateAttributeValueCommand command = new CreateAttributeValueCommand("dept", "DEV", "개발", 1, true);
        AttributeDefinition enumDef = AttributeDefinition.create("dept", "부서", null, ValueType.ENUM, true, false);
        given(attributeDefinitionPort.findAttributeDefinitionByKey("dept")).willReturn(Optional.of(enumDef));
        given(attributeValuePort.existsByKey("dept", "DEV")).willReturn(true);

        assertThatThrownBy(() -> attributeValueService.createAttributeValue(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createAttributeValue_valid_callsSave() {
        CreateAttributeValueCommand command = new CreateAttributeValueCommand("dept", "DEV", "개발", 1, true);
        AttributeDefinition enumDef = AttributeDefinition.create("dept", "부서", null, ValueType.ENUM, true, false);
        given(attributeDefinitionPort.findAttributeDefinitionByKey("dept")).willReturn(Optional.of(enumDef));
        given(attributeValuePort.existsByKey("dept", "DEV")).willReturn(false);

        attributeValueService.createAttributeValue(command);

        then(attributeValuePort).should().save(org.mockito.ArgumentMatchers.any());
    }
}
