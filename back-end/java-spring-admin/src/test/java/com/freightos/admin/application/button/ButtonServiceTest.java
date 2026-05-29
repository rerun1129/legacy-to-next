package com.freightos.admin.application.button;

import com.freightos.admin.application.button.command.CreateButtonCommand;
import com.freightos.admin.application.button.port.out.ButtonPort;
import com.freightos.admin.application.menu.port.out.MenuPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.button.entity.Button;
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

@ExtendWith(MockitoExtension.class)
class ButtonServiceTest {

    @Mock
    private ButtonPort buttonPort;

    @Mock
    private ButtonFactory buttonFactory;

    @Mock
    private MenuPort menuPort;

    @InjectMocks
    private ButtonService buttonService;

    @Test
    void createButton_duplicateCode_throwsConflict() {
        CreateButtonCommand command = new CreateButtonCommand("SAVE_BTN", 1L, "저장", "CREATE", "POST", "/api/save", 1, true);
        given(buttonPort.existsByButtonCode("SAVE_BTN")).willReturn(true);

        assertThatThrownBy(() -> buttonService.createButton(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createButton_menuNotFound_throwsNotFound() {
        CreateButtonCommand command = new CreateButtonCommand("SAVE_BTN", 99L, "저장", "CREATE", "POST", "/api/save", 1, true);
        given(buttonPort.existsByButtonCode("SAVE_BTN")).willReturn(false);
        given(menuPort.existsById(99L)).willReturn(false);

        assertThatThrownBy(() -> buttonService.createButton(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createButton_valid_savesAndReturnsId() {
        CreateButtonCommand command = new CreateButtonCommand("SAVE_BTN", 1L, "저장", "CREATE", "POST", "/api/save", 1, true);
        Button domain = Button.create("SAVE_BTN", 1L, "저장", com.freightos.admin.domain.button.entity.ActionType.CREATE, "POST", "/api/save", 1, true);
        given(buttonPort.existsByButtonCode("SAVE_BTN")).willReturn(false);
        given(menuPort.existsById(1L)).willReturn(true);
        given(buttonFactory.from(command)).willReturn(domain);
        given(buttonPort.save(domain)).willReturn(7L);

        Long id = buttonService.createButton(command);

        assertThat(id).isEqualTo(7L);
    }

    @Test
    void findButtonById_notFound_throwsNotFound() {
        given(buttonPort.findButtonById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> buttonService.findButtonById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
