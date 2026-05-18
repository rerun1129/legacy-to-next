package com.freightos.admin.application.codemaster;

import com.freightos.admin.application.codedetail.port.out.CodeDetailPort;
import com.freightos.admin.application.codemaster.command.CreateCodeMasterCommand;
import com.freightos.admin.application.codemaster.command.UpdateCodeMasterCommand;
import com.freightos.admin.application.codemaster.port.out.CodeMasterPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.codemaster.entity.CodeMaster;
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
class CodeMasterServiceTest {

    @Mock
    private CodeMasterPort codeMasterPort;

    @Mock
    private CodeMasterFactory codeMasterFactory;

    @Mock
    private CodeDetailPort codeDetailPort;

    @InjectMocks
    private CodeMasterService codeMasterService;

    // ── createCodeMaster: Factory·Port 호출 후 id 반환 ────────────────────────

    @Test
    void createCodeMaster_callsFactoryAndPortSaveReturnsId() {
        CreateCodeMasterCommand command = new CreateCodeMasterCommand("USER_STATUS", "사용자 상태", null, 1, true);
        CodeMaster domain = CodeMaster.create("USER_STATUS", "사용자 상태", null, 1, true);
        given(codeMasterFactory.from(command)).willReturn(domain);
        given(codeMasterPort.save(domain)).willReturn(10L);

        Long id = codeMasterService.createCodeMaster(command);

        assertThat(id).isEqualTo(10L);
        then(codeMasterFactory).should().from(command);
        then(codeMasterPort).should().save(domain);
    }

    // ── findCodeMasterById: 미존재 → ApplicationException(404) ───────────────

    @Test
    void findCodeMasterById_notFound_throwsApplicationException() {
        given(codeMasterPort.findCodeMasterById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> codeMasterService.findCodeMasterById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("CODE_MASTER_NOT_FOUND");
                });
    }

    // ── updateCodeMaster: 존재 검증 후 port.update 호출 ──────────────────────

    @Test
    void updateCodeMaster_existsAndCallsPortUpdate() {
        UpdateCodeMasterCommand command = new UpdateCodeMasterCommand("수정된 그룹명", "설명", 2, true);
        CodeMaster existing = CodeMaster.create("USER_STATUS", "사용자 상태", null, 1, true);
        given(codeMasterPort.findCodeMasterById(1L)).willReturn(Optional.of(existing));

        codeMasterService.updateCodeMaster(1L, command);

        then(codeMasterPort).should().update(eq(1L), any(CodeMaster.class));
    }

    // ── deleteCodeMasterById: detail 없음 → 정상 삭제 ────────────────────────

    @Test
    void deleteCodeMasterById_noDetail_callsPortDelete() {
        given(codeDetailPort.countByMasterId(5L)).willReturn(0L);

        codeMasterService.deleteCodeMasterById(5L);

        then(codeMasterPort).should().deleteCodeMasterById(5L);
    }

    // ── deleteCodeMasterById: detail 존재 → CONFLICT 예외 ────────────────────

    @Test
    void deleteCodeMasterById_hasDetail_throwsConflict() {
        given(codeDetailPort.countByMasterId(5L)).willReturn(3L);

        assertThatThrownBy(() -> codeMasterService.deleteCodeMasterById(5L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("CODE_MASTER_HAS_DETAIL_CANNOT_DELETE");
                });
    }
}
