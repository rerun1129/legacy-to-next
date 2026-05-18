package com.freightos.admin.application.menupolicy;

import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.menu.port.out.MenuPort;
import com.freightos.admin.application.menupolicy.command.CreateMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
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
class MenuPolicyServiceTest {

    @Mock
    private MenuPolicyPort menuPolicyPort;

    @Mock
    private MenuPort menuPort;

    @Mock
    private AttributeDefinitionPort attributeDefinitionPort;

    @InjectMocks
    private MenuPolicyService menuPolicyService;

    @Test
    void createMenuPolicy_menuNotFound_throwsNotFound() {
        CreateMenuPolicyCommand command = new CreateMenuPolicyCommand(99L, "dept", "DEV");
        given(menuPort.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> menuPolicyService.createMenuPolicy(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createMenuPolicy_duplicate_throwsConflict() {
        CreateMenuPolicyCommand command = new CreateMenuPolicyCommand(1L, "dept", "DEV");
        given(menuPort.existsById(1L)).willReturn(true);
        given(attributeDefinitionPort.existsByKey("dept")).willReturn(true);
        given(menuPolicyPort.existsByCompositeKey(1L, "dept", "DEV")).willReturn(true);

        assertThatThrownBy(() -> menuPolicyService.createMenuPolicy(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createMenuPolicy_valid_savesAndReturnsId() {
        CreateMenuPolicyCommand command = new CreateMenuPolicyCommand(1L, "dept", "DEV");
        given(menuPort.existsById(1L)).willReturn(true);
        given(attributeDefinitionPort.existsByKey("dept")).willReturn(true);
        given(menuPolicyPort.existsByCompositeKey(1L, "dept", "DEV")).willReturn(false);
        given(menuPolicyPort.save(org.mockito.ArgumentMatchers.any())).willReturn(100L);

        Long id = menuPolicyService.createMenuPolicy(command);

        assertThat(id).isEqualTo(100L);
    }

    @Test
    void deleteMenuPolicyById_notFound_throwsNotFound() {
        given(menuPolicyPort.existsById(50L)).willReturn(false);

        assertThatThrownBy(() -> menuPolicyService.deleteMenuPolicyById(50L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void findMenuPolicyById_notFound_throwsNotFound() {
        given(menuPolicyPort.findMenuPolicyById(50L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> menuPolicyService.findMenuPolicyById(50L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
