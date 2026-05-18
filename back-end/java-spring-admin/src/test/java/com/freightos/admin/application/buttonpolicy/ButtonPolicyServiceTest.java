package com.freightos.admin.application.buttonpolicy;

import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.button.port.out.ButtonPort;
import com.freightos.admin.application.buttonpolicy.command.CreateButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.port.out.ButtonPolicyPort;
import com.freightos.admin.common.exception.ApplicationException;
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
class ButtonPolicyServiceTest {

    @Mock
    private ButtonPolicyPort buttonPolicyPort;

    @Mock
    private ButtonPort buttonPort;

    @Mock
    private AttributeDefinitionPort attributeDefinitionPort;

    @InjectMocks
    private ButtonPolicyService buttonPolicyService;

    @Test
    void createButtonPolicy_buttonNotFound_throwsNotFound() {
        CreateButtonPolicyCommand command = new CreateButtonPolicyCommand(99L, "dept", "DEV");
        given(buttonPort.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> buttonPolicyService.createButtonPolicy(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createButtonPolicy_duplicate_throwsConflict() {
        CreateButtonPolicyCommand command = new CreateButtonPolicyCommand(1L, "dept", "DEV");
        given(buttonPort.existsById(1L)).willReturn(true);
        given(attributeDefinitionPort.existsByKey("dept")).willReturn(true);
        given(buttonPolicyPort.existsByCompositeKey(1L, "dept", "DEV")).willReturn(true);

        assertThatThrownBy(() -> buttonPolicyService.createButtonPolicy(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createButtonPolicy_valid_savesAndReturnsId() {
        CreateButtonPolicyCommand command = new CreateButtonPolicyCommand(1L, "dept", "DEV");
        given(buttonPort.existsById(1L)).willReturn(true);
        given(attributeDefinitionPort.existsByKey("dept")).willReturn(true);
        given(buttonPolicyPort.existsByCompositeKey(1L, "dept", "DEV")).willReturn(false);
        given(buttonPolicyPort.save(org.mockito.ArgumentMatchers.any())).willReturn(200L);

        Long id = buttonPolicyService.createButtonPolicy(command);

        assertThat(id).isEqualTo(200L);
    }

    @Test
    void deleteButtonPolicyById_notFound_throwsNotFound() {
        given(buttonPolicyPort.existsById(50L)).willReturn(false);

        assertThatThrownBy(() -> buttonPolicyService.deleteButtonPolicyById(50L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void findButtonPolicyById_notFound_throwsNotFound() {
        given(buttonPolicyPort.findButtonPolicyById(50L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> buttonPolicyService.findButtonPolicyById(50L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
