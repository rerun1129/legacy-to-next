package com.freightos.admin.application.codedetail;

import com.freightos.admin.application.codedetail.command.CreateCodeDetailCommand;
import com.freightos.admin.application.codedetail.command.SearchCodeDetailCommand;
import com.freightos.admin.application.codedetail.command.UpdateCodeDetailCommand;
import com.freightos.admin.application.codedetail.port.out.CodeDetailPort;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.application.codemaster.port.out.CodeMasterPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.codedetail.entity.CodeDetail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CodeDetailServiceTest {

    @Mock
    private CodeDetailPort codeDetailPort;

    @Mock
    private CodeDetailFactory codeDetailFactory;

    @Mock
    private CodeMasterPort codeMasterPort;

    @InjectMocks
    private CodeDetailService codeDetailService;

    // ── createCodeDetail: master 존재 → id 반환 ────────────────────────────────

    @Test
    void createCodeDetail_masterExists_returnsId() {
        CreateCodeDetailCommand command = new CreateCodeDetailCommand(1L, "ACTIVE", "활성", 1, true, null);
        CodeDetail domain = CodeDetail.create(1L, "ACTIVE", "활성", 1, true, null);
        given(codeMasterPort.existsById(1L)).willReturn(true);
        given(codeDetailFactory.from(command)).willReturn(domain);
        given(codeDetailPort.save(domain)).willReturn(20L);

        Long id = codeDetailService.createCodeDetail(command);

        assertThat(id).isEqualTo(20L);
        then(codeDetailFactory).should().from(command);
        then(codeDetailPort).should().save(domain);
    }

    // ── createCodeDetail: master 미존재 → CODE_MASTER_NOT_FOUND ──────────────

    @Test
    void createCodeDetail_masterNotFound_throwsNotFound() {
        CreateCodeDetailCommand command = new CreateCodeDetailCommand(999L, "ACTIVE", "활성", 1, true, null);
        given(codeMasterPort.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> codeDetailService.createCodeDetail(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("CODE_MASTER_NOT_FOUND");
                });
    }

    // ── findCodeDetailById: 미존재 → ApplicationException(404) ───────────────

    @Test
    void findCodeDetailById_notFound_throwsApplicationException() {
        given(codeDetailPort.findCodeDetailById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> codeDetailService.findCodeDetailById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("CODE_DETAIL_NOT_FOUND");
                });
    }

    // ── updateCodeDetail: 존재 검증 후 port.update 호출 ──────────────────────

    @Test
    void updateCodeDetail_existsAndCallsPortUpdate() {
        UpdateCodeDetailCommand command = new UpdateCodeDetailCommand("수정된 레이블", 2, false, "비고");
        CodeDetail existing = CodeDetail.create(1L, "ACTIVE", "활성", 1, true, null);
        given(codeDetailPort.findCodeDetailById(1L)).willReturn(Optional.of(existing));

        codeDetailService.updateCodeDetail(1L, command);

        then(codeDetailPort).should().update(eq(1L), any(CodeDetail.class));
    }

    // ── deleteCodeDetailById: port.deleteCodeDetailById 호출 ─────────────────

    @Test
    void deleteCodeDetailById_callsPortDelete() {
        codeDetailService.deleteCodeDetailById(5L);

        then(codeDetailPort).should().deleteCodeDetailById(5L);
    }

    // ── searchCodeDetails: masterId 조건으로 목록 조회 ───────────────────────

    @Test
    void searchCodeDetails_byMaster_returnsSummaries() {
        SearchCodeDetailCommand command = new SearchCodeDetailCommand(1L, null, null, null, 0, 20);
        CodeDetailSummary summary = new CodeDetailSummary(10L, 1L, "ACTIVE", "활성", 1, true, LocalDateTime.of(2024, 1, 1, 0, 0));
        PagedResult<CodeDetailSummary> page = PagedResult.of(List.of(summary), 1L, 1, 0, 20);
        given(codeDetailPort.searchSummaries(command)).willReturn(page);

        PagedResult<CodeDetailSummary> result = codeDetailService.searchCodeDetails(command);

        assertThat(result.getTotalElements()).isEqualTo(1L);
        assertThat(result.getContent().get(0).codeValue()).isEqualTo("ACTIVE");
    }
}
