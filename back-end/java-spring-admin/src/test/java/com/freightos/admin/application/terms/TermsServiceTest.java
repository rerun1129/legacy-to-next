package com.freightos.admin.application.terms;

import com.freightos.admin.application.terms.command.CreateTermsCommand;
import com.freightos.admin.application.terms.command.UpdateTermsCommand;
import com.freightos.admin.application.terms.port.out.TermsPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.terms.entity.Terms;
import com.freightos.admin.domain.terms.entity.TermsType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TermsServiceTest {

    @Mock
    private TermsPort termsPort;

    @Mock
    private TermsFactory termsFactory;

    @InjectMocks
    private TermsService termsService;

    // ── createTerms: 동일 type/version 이미 존재 → 409 CONFLICT ─────────────

    @Test
    void createTerms_existingTypeAndVersion_throwsConflict() {
        CreateTermsCommand command = new CreateTermsCommand("TOS", 1, LocalDateTime.of(2024, 1, 1, 0, 0), "내용", null);
        given(termsPort.existsByTypeAndVersion(TermsType.TOS, 1)).willReturn(true);

        assertThatThrownBy(() -> termsService.createTerms(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("TERMS_ALREADY_EXISTS");
                });
    }

    // ── getTermsById: not_found → 404 TERMS_NOT_FOUND ────────────────────────

    @Test
    void getTermsById_notFound_throwsNotFound() {
        given(termsPort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> termsService.getTermsById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("TERMS_NOT_FOUND");
                });
    }

    // ── updateTerms: 이미 삭제된 약관 → 409 TERMS_ALREADY_DELETED ────────────

    @Test
    void updateTerms_alreadyDeleted_throwsConflict() {
        Terms deleted = Terms.create(TermsType.TOS, 1, LocalDateTime.of(2024, 1, 1, 0, 0), "내용", null);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 6, 1, 0, 0));
        UpdateTermsCommand command = new UpdateTermsCommand("수정 내용", null, LocalDateTime.of(2024, 2, 1, 0, 0));
        given(termsPort.findById(1L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> termsService.updateTerms(1L, command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("TERMS_ALREADY_DELETED");
                });
    }

    // ── deleteTerms: 정상 → port.softDelete 호출 ─────────────────────────────

    @Test
    void deleteTerms_normal_callsSoftDelete() {
        Terms existing = Terms.create(TermsType.TOS, 1, LocalDateTime.of(2024, 1, 1, 0, 0), "내용", null);
        given(termsPort.findById(5L)).willReturn(Optional.of(existing));

        termsService.deleteTerms(5L);

        then(termsPort).should().softDelete(eq(5L));
    }

    // ── deleteTerms: 이미 삭제된 약관 → 409 TERMS_ALREADY_DELETED ────────────

    @Test
    void deleteTerms_alreadyDeleted_throwsConflict() {
        Terms deleted = Terms.create(TermsType.PRIVACY, 2, LocalDateTime.of(2024, 1, 1, 0, 0), "내용", null);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 6, 1, 0, 0));
        given(termsPort.findById(5L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> termsService.deleteTerms(5L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("TERMS_ALREADY_DELETED");
                });
    }
}
