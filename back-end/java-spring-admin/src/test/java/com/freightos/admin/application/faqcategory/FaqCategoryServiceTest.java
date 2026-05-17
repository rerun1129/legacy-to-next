package com.freightos.admin.application.faqcategory;

import com.freightos.admin.application.faqcategory.command.CreateFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.command.UpdateFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.port.out.FaqCategoryPort;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.domain.faqcategory.entity.FaqCategory;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FaqCategoryServiceTest {

    @Mock
    private FaqCategoryPort faqCategoryPort;

    @Mock
    private FaqCategoryFactory faqCategoryFactory;

    @InjectMocks
    private FaqCategoryService faqCategoryService;

    // ── createFaqCategory: 동일 이름 존재 → 409 FAQ_CATEGORY_ALREADY_EXISTS ────

    @Test
    void createFaqCategory_duplicateName_throwsConflict() {
        CreateFaqCategoryCommand command = new CreateFaqCategoryCommand("중복카테고리", 0, true);
        given(faqCategoryPort.existsByName("중복카테고리")).willReturn(true);

        assertThatThrownBy(() -> faqCategoryService.createFaqCategory(command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("FAQ_CATEGORY_ALREADY_EXISTS");
                });
    }

    // ── updateFaqCategory: 다른 카테고리와 이름 중복 → 409 ───────────────────

    @Test
    void updateFaqCategory_renameToExistingName_throwsConflict() {
        FaqCategory existing = FaqCategory.create("원래이름", 0, true);
        existing.assignIdentity(1L, null, null, null, null);
        UpdateFaqCategoryCommand command = new UpdateFaqCategoryCommand("중복이름", 0, true);

        given(faqCategoryPort.findById(1L)).willReturn(Optional.of(existing));
        given(faqCategoryPort.existsByNameExcludingId("중복이름", 1L)).willReturn(true);

        assertThatThrownBy(() -> faqCategoryService.updateFaqCategory(1L, command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("FAQ_CATEGORY_ALREADY_EXISTS");
                });
    }

    // ── getFaqCategoryById: not_found → 404 FAQ_CATEGORY_NOT_FOUND ───────────

    @Test
    void getFaqCategoryById_notFound_throwsNotFound() {
        given(faqCategoryPort.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> faqCategoryService.getFaqCategoryById(99L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(appEx.getErrorCode()).isEqualTo("FAQ_CATEGORY_NOT_FOUND");
                });
    }

    // ── updateFaqCategory: 이미 삭제된 카테고리 → 409 ────────────────────────

    @Test
    void updateFaqCategory_alreadyDeleted_throwsConflict() {
        FaqCategory deleted = FaqCategory.create("카테고리A", 0, true);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 6, 1, 0, 0));
        UpdateFaqCategoryCommand command = new UpdateFaqCategoryCommand("카테고리A", 0, true);

        given(faqCategoryPort.findById(1L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> faqCategoryService.updateFaqCategory(1L, command))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("FAQ_CATEGORY_ALREADY_DELETED");
                });
    }

    // ── deleteFaqCategory: 이미 삭제된 카테고리 → 409 ────────────────────────

    @Test
    void deleteFaqCategory_alreadyDeleted_throwsConflict() {
        FaqCategory deleted = FaqCategory.create("카테고리B", 0, true);
        deleted.assignDeletedAt(LocalDateTime.of(2024, 6, 1, 0, 0));

        given(faqCategoryPort.findById(2L)).willReturn(Optional.of(deleted));

        assertThatThrownBy(() -> faqCategoryService.deleteFaqCategory(2L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(appEx.getErrorCode()).isEqualTo("FAQ_CATEGORY_ALREADY_DELETED");
                });
    }

    // ── deleteFaqCategory: 정상 → port.softDelete 호출 ──────────────────────

    @Test
    void deleteFaqCategory_normal_callsSoftDelete() {
        FaqCategory existing = FaqCategory.create("카테고리C", 0, true);

        given(faqCategoryPort.findById(3L)).willReturn(Optional.of(existing));

        faqCategoryService.deleteFaqCategory(3L);

        then(faqCategoryPort).should().softDelete(eq(3L));
    }
}
