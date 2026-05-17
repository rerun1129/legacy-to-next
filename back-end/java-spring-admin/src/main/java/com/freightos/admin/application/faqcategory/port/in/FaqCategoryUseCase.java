package com.freightos.admin.application.faqcategory.port.in;

import com.freightos.admin.application.faqcategory.command.CreateFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.command.SearchFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.command.UpdateFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.projection.FaqCategorySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faqcategory.entity.FaqCategory;

public interface FaqCategoryUseCase {
    PagedResult<FaqCategorySummary> searchFaqCategories(SearchFaqCategoryCommand command);
    FaqCategory getFaqCategoryById(Long id);
    Long createFaqCategory(CreateFaqCategoryCommand command);
    void updateFaqCategory(Long id, UpdateFaqCategoryCommand command);
    void deleteFaqCategory(Long id);
}
