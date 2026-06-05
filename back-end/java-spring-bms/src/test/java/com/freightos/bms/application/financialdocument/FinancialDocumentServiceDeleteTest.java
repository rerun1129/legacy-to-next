package com.freightos.bms.application.financialdocument;

import com.freightos.bms.application.financialdocument.port.out.DocumentNumberGenerator;
import com.freightos.bms.application.financialdocument.port.out.DocumentSummary;
import com.freightos.bms.application.financialdocument.port.out.FinancialDocumentPort;
import com.freightos.bms.application.port.out.CodeNameResolver;
import com.freightos.bms.common.response.MessageCode;
import com.freightos.common.exception.FmsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

/**
 * FinancialDocumentService.deleteDocument 상태 가드 단위 테스트.
 * CREATED → 삭제 허용 / 비CREATED → CONFLICT 차단.
 * 고정 입력·고정 기대값. 시간/랜덤/시퀀스 절대값 없음(T1).
 */
@ExtendWith(MockitoExtension.class)
class FinancialDocumentServiceDeleteTest {

    @Mock
    private FinancialDocumentPort financialDocumentPort;

    @Mock
    private DocumentNumberGenerator documentNumberGenerator;

    @Mock
    private CodeNameResolver codeNameResolver;

    @Mock
    private FinancialDocumentQueryService queryService;

    @InjectMocks
    private FinancialDocumentService service;

    // ── 픽스처 헬퍼 ──────────────────────────────────────────────────────────

    private DocumentSummary summary(String status) {
        return new DocumentSummary(1L, "SI260600001", "CUST001", "INVOICE", "20260601", status);
    }

    // ── 테스트 케이스 ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("CREATED 상태 서류 삭제 → unlinkLines + deleteDocument 호출")
    void deleteDocument_created_succeeds() {
        given(financialDocumentPort.loadDocumentSummary(1L)).willReturn(Optional.of(summary("CREATED")));

        assertThatCode(() -> service.deleteDocument(1L)).doesNotThrowAnyException();

        then(financialDocumentPort).should().unlinkLinesByDocument(1L);
        then(financialDocumentPort).should().deleteDocument(1L);
    }

    @Test
    @DisplayName("GROUPED 상태 서류 삭제 → CONFLICT 차단, unlinkLines·deleteDocument 미호출")
    void deleteDocument_grouped_throwsConflict() {
        given(financialDocumentPort.loadDocumentSummary(2L)).willReturn(Optional.of(
            new DocumentSummary(2L, "GI260600001", "CUST001", "INVOICE", "20260601", "GROUPED")
        ));

        assertThatThrownBy(() -> service.deleteDocument(2L))
            .isInstanceOf(FmsException.class)
            .satisfies(ex -> {
                FmsException fmsEx = (FmsException) ex;
                assertThat(fmsEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(fmsEx.getErrorCode()).isEqualTo(MessageCode.FINANCIAL_DOCUMENT_DELETE_NOT_CREATED.name());
            });

        then(financialDocumentPort).should(never()).unlinkLinesByDocument(2L);
        then(financialDocumentPort).should(never()).deleteDocument(2L);
    }

    @Test
    @DisplayName("존재하지 않는 서류 삭제 → CONFLICT(NOT_FOUND) 차단")
    void deleteDocument_notFound_throwsConflict() {
        given(financialDocumentPort.loadDocumentSummary(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteDocument(99L))
            .isInstanceOf(FmsException.class)
            .satisfies(ex -> {
                FmsException fmsEx = (FmsException) ex;
                assertThat(fmsEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(fmsEx.getErrorCode()).isEqualTo(MessageCode.FINANCIAL_DOCUMENT_NOT_FOUND.name());
            });

        then(financialDocumentPort).should(never()).unlinkLinesByDocument(99L);
        then(financialDocumentPort).should(never()).deleteDocument(99L);
    }
}
