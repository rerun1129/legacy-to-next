package com.freightos.admin.application.faqcategory;

import com.freightos.admin.application.faqcategory.command.CreateFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.command.SearchFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.command.UpdateFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.port.in.FaqCategoryUseCase;
import com.freightos.admin.application.faqcategory.port.out.FaqCategoryPort;
import com.freightos.admin.application.faqcategory.projection.FaqCategorySummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faqcategory.entity.FaqCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqCategoryService implements FaqCategoryUseCase {

    private final FaqCategoryPort faqCategoryPort;
    private final FaqCategoryFactory faqCategoryFactory;

    @Override
    public PagedResult<FaqCategorySummary> searchFaqCategories(SearchFaqCategoryCommand command) {
        return faqCategoryPort.searchSummaries(command);
    }

    @Override
    public FaqCategory getFaqCategoryById(Long id) {
        return faqCategoryPort.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("FAQ_CATEGORY_NOT_FOUND", MessageCode.FAQ_CATEGORY_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createFaqCategory(CreateFaqCategoryCommand command) {
        if (faqCategoryPort.existsByName(command.name())) {
            throw ApplicationException.conflict("FAQ_CATEGORY_ALREADY_EXISTS", "동일한 이름의 FAQ 카테고리가 이미 존재합니다.");
        }
        return faqCategoryPort.save(faqCategoryFactory.from(command));
    }

    @Override
    @Transactional
    public void updateFaqCategory(Long id, UpdateFaqCategoryCommand command) {
        FaqCategory existing = getFaqCategoryById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("FAQ_CATEGORY_ALREADY_DELETED", MessageCode.FAQ_CATEGORY_ALREADY_DELETED.getMessage());
        }
        if (!existing.getName().equals(command.name()) && faqCategoryPort.existsByNameExcludingId(command.name(), id)) {
            throw ApplicationException.conflict("FAQ_CATEGORY_ALREADY_EXISTS", "동일한 이름의 FAQ 카테고리가 이미 존재합니다.");
        }
        existing.applyUpdate(command.name(), command.sortOrder(), command.active());
        faqCategoryPort.update(id, existing);
    }

    @Override
    @Transactional
    public void deleteFaqCategory(Long id) {
        FaqCategory existing = getFaqCategoryById(id);
        if (existing.isDeleted()) {
            throw ApplicationException.conflict("FAQ_CATEGORY_ALREADY_DELETED", MessageCode.FAQ_CATEGORY_ALREADY_DELETED.getMessage());
        }
        faqCategoryPort.softDelete(id);
    }
}
