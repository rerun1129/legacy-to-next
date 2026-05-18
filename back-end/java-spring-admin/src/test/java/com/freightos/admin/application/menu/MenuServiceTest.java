package com.freightos.admin.application.menu;

import com.freightos.admin.application.button.port.out.ButtonPort;
import com.freightos.admin.application.menu.command.CreateMenuCommand;
import com.freightos.admin.application.menu.port.out.MenuPort;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.application.module.port.out.ModulePort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.menu.entity.Menu;
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
class MenuServiceTest {

    @Mock
    private MenuPort menuPort;

    @Mock
    private MenuFactory menuFactory;

    @Mock
    private ModulePort modulePort;

    @Mock
    private ButtonPort buttonPort;

    @Mock
    private MenuPolicyPort menuPolicyPort;

    @InjectMocks
    private MenuService menuService;

    @Test
    void createMenu_duplicateCode_throwsConflict() {
        CreateMenuCommand command = new CreateMenuCommand("MAIN", null, "/main", "메인", null, null, 1, true, "ACCESS");
        given(menuPort.existsByMenuCode("MAIN")).willReturn(true);

        assertThatThrownBy(() -> menuService.createMenu(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createMenu_moduleNotFound_throwsNotFound() {
        CreateMenuCommand command = new CreateMenuCommand("MAIN", null, "/main", "메인", null, null, 1, true, "MISSING");
        given(menuPort.existsByMenuCode("MAIN")).willReturn(false);
        given(modulePort.existsByCode("MISSING")).willReturn(false);

        assertThatThrownBy(() -> menuService.createMenu(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createMenu_valid_savesAndReturnsId() {
        CreateMenuCommand command = new CreateMenuCommand("MAIN", null, "/main", "메인", null, null, 1, true, "ACCESS");
        Menu domain = Menu.create("MAIN", null, "/main", "메인", null, null, 1, true, "ACCESS");
        given(menuPort.existsByMenuCode("MAIN")).willReturn(false);
        given(modulePort.existsByCode("ACCESS")).willReturn(true);
        given(menuFactory.from(command)).willReturn(domain);
        given(menuPort.save(domain)).willReturn(10L);

        Long id = menuService.createMenu(command);

        assertThat(id).isEqualTo(10L);
    }

    @Test
    void deleteMenuById_hasChildren_throwsConflict() {
        given(menuPort.existsById(5L)).willReturn(true);
        given(menuPort.existsByParentId(5L)).willReturn(true);

        assertThatThrownBy(() -> menuService.deleteMenuById(5L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void deleteMenuById_noChildren_callsDelete() {
        given(menuPort.existsById(5L)).willReturn(true);
        given(menuPort.existsByParentId(5L)).willReturn(false);
        given(buttonPort.existsByMenuId(5L)).willReturn(false);
        given(menuPolicyPort.existsByMenuId(5L)).willReturn(false);

        menuService.deleteMenuById(5L);

        then(menuPort).should().deleteMenuById(5L);
    }

    @Test
    void findMenuById_notFound_throwsNotFound() {
        given(menuPort.findMenuById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.findMenuById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
