package com.freightos.admin.application.code;

import com.freightos.admin.application.code.command.CreateCodeCommand;
import com.freightos.admin.application.code.command.UpdateCodeCommand;
import com.freightos.admin.application.code.port.out.CodePort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.code.entity.Code;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CodeServiceTest {

    @Mock
    private CodePort codePort;

    @Mock
    private CodeFactory codeFactory;

    @InjectMocks
    private CodeService codeService;

    // ── createCode ────────────────────────────────────────────────────────────

    @Test
    void createCode_callsFactoryAndPortSaveReturnsId() {
        CreateCodeCommand command = new CreateCodeCommand("CARRIER", "KR001", "Korean Carrier", 1, true, null);
        Code domain = Code.create("CARRIER", "KR001", "Korean Carrier", 1, true, null);
        given(codeFactory.from(command)).willReturn(domain);
        given(codePort.save(domain)).willReturn(10L);

        Long id = codeService.createCode(command);

        assertThat(id).isEqualTo(10L);
        then(codeFactory).should().from(command);
        then(codePort).should().save(domain);
    }

    // ── findCodeById: not found → ApplicationException ────────────────────────

    @Test
    void findCodeById_notFound_throwsApplicationException() {
        given(codePort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> codeService.findCodeById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("CODE_NOT_FOUND");
                });
    }

    // ── findCodeById: present → 도메인 반환 ───────────────────────────────────

    @Test
    void findCodeById_found_returnsDomain() {
        Code domain = Code.create("CARRIER", "KR001", "Korean Carrier", 1, true, null);
        given(codePort.findById(1L)).willReturn(Optional.of(domain));

        Code result = codeService.findCodeById(1L);

        assertThat(result).isEqualTo(domain);
    }

    // ── updateCode: 존재 검증 후 port.update 호출 ──────────────────────────────

    @Test
    void updateCode_existsAndCallsPortUpdate() {
        UpdateCodeCommand command = new UpdateCodeCommand("Updated Label", 2, true, "updated remark");
        Code existing = Code.create("CARRIER", "KR001", "Korean Carrier", 1, true, null);
        given(codePort.findById(1L)).willReturn(Optional.of(existing));

        codeService.updateCode(1L, command);

        then(codePort).should().update(eq(1L), any(Code.class));
    }

    // ── deleteCodeById: port.deleteById 호출 ──────────────────────────────────

    @Test
    void deleteCodeById_callsPortDeleteById() {
        codeService.deleteCodeById(5L);

        then(codePort).should().deleteById(5L);
    }
}
