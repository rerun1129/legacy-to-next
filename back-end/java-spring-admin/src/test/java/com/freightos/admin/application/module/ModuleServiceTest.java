package com.freightos.admin.application.module;

import com.freightos.admin.application.menu.port.out.MenuPort;
import com.freightos.admin.application.module.command.CreateModuleCommand;
import com.freightos.admin.application.module.command.UpdateModuleCommand;
import com.freightos.admin.application.module.port.out.ModulePort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.module.entity.Module;
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
class ModuleServiceTest {

    @Mock
    private ModulePort modulePort;

    @Mock
    private ModuleFactory moduleFactory;

    @Mock
    private MenuPort menuPort;

    @InjectMocks
    private ModuleService moduleService;

    @Test
    void createModule_duplicateCode_throwsConflict() {
        CreateModuleCommand command = new CreateModuleCommand("ACCESS", "접근 제어", null, 1, true);
        given(modulePort.existsByCode("ACCESS")).willReturn(true);

        assertThatThrownBy(() -> moduleService.createModule(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createModule_noDuplicate_savesAndReturnsCode() {
        CreateModuleCommand command = new CreateModuleCommand("ACCESS", "접근 제어", null, 1, true);
        Module domain = Module.create("ACCESS", "접근 제어", null, 1, true);
        given(modulePort.existsByCode("ACCESS")).willReturn(false);
        given(moduleFactory.from(command)).willReturn(domain);
        given(modulePort.save(domain)).willReturn("ACCESS");

        String code = moduleService.createModule(command);

        assertThat(code).isEqualTo("ACCESS");
        then(modulePort).should().save(domain);
    }

    @Test
    void deleteModuleByCode_hasMenu_throwsConflict() {
        given(modulePort.existsByCode("ACCESS")).willReturn(true);
        given(menuPort.existsByModuleCode("ACCESS")).willReturn(true);

        assertThatThrownBy(() -> moduleService.deleteModuleByCode("ACCESS"))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void findModuleByCode_notFound_throwsNotFound() {
        given(modulePort.findModuleByCode("UNKNOWN")).willReturn(Optional.empty());

        assertThatThrownBy(() -> moduleService.findModuleByCode("UNKNOWN"))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateModule_exists_callsPortUpdate() {
        UpdateModuleCommand command = new UpdateModuleCommand("수정명", null, 2, true);
        Module existing = Module.create("ACCESS", "접근 제어", null, 1, true);
        given(modulePort.findModuleByCode("ACCESS")).willReturn(Optional.of(existing));

        moduleService.updateModule("ACCESS", command);

        then(modulePort).should().update("ACCESS", existing);
    }
}
