package com.freightos.admin.application.faq;

import com.freightos.admin.application.faq.command.CreateFaqCommand;
import com.freightos.admin.application.faq.command.UpdateFaqCommand;
import com.freightos.admin.application.faq.port.out.FaqPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.faq.entity.Faq;
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
class FaqServiceTest {

    @Mock
    private FaqPort faqPort;

    @Mock
    private FaqFactory faqFactory;

    @InjectMocks
    private FaqService faqService;

    // ── createFaq: 정상 → faqPort.save 호출 후 id 반환 ──────────────────────

    @Test
    void createFaq_normal_returnsId() {
        CreateFaqCommand command = new CreateFaqCommand(1L, "질문", "답변", 0, true);
        Faq faq = Faq.create(1L, "질문", "답변", 0, true);

        given(faqFactory.from(command)).willReturn(faq);
        given(faqPort.save(faq)).willReturn(10L);

        Long id = faqService.createFaq(command);

        assertThat(id).isEqualTo(10L);
        then(faqPort).should().save(eq(faq));
    }

    // ── getFaqById: not_found → 404 FAQ_NOT_FOUND ────────────────────────────

    @Test
    void getFaqById_notFound_throwsNotFound() {
        given(faqPort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> faqService.getFaqById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("FAQ_NOT_FOUND");
                });
    }

    // ── updateFaq: 이미 삭제된 FAQ → 409 FAQ_ALREADY_DELETED ─────────────────

    @Test
    void updateFaq_alreadyDeleted_throwsConflict() {
        Faq deleted = Faq.create(1L, "질문", "답변", 0, true);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 6, 1, 0, 0));
        UpdateFaqCommand command = new UpdateFaqCommand(1L, "수정질문", "수정답변", 0, true);

        given(faqPort.findById(5L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> faqService.updateFaq(5L, command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("FAQ_ALREADY_DELETED");
                });
    }

    // ── deleteFaq: 이미 삭제된 FAQ → 409 FAQ_ALREADY_DELETED ─────────────────

    @Test
    void deleteFaq_alreadyDeleted_throwsConflict() {
        Faq deleted = Faq.create(1L, "질문", "답변", 0, true);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 6, 1, 0, 0));

        given(faqPort.findById(7L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> faqService.deleteFaq(7L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("FAQ_ALREADY_DELETED");
                });
    }

    // ── deleteFaq: 정상 → port.softDelete 호출 ───────────────────────────────

    @Test
    void deleteFaq_normal_callsSoftDelete() {
        Faq existing = Faq.create(1L, "질문", "답변", 0, true);

        given(faqPort.findById(8L)).willReturn(Optional.of(existing));

        faqService.deleteFaq(8L);

        then(faqPort).should().softDelete(eq(8L));
    }
}
