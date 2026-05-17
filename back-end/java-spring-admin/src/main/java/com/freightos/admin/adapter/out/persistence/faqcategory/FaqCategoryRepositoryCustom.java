package com.freightos.admin.adapter.out.persistence.faqcategory;

import com.freightos.admin.application.faqcategory.command.SearchFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.projection.FaqCategorySummary;
import com.freightos.admin.common.response.PagedResult;

public interface FaqCategoryRepositoryCustom {
    PagedResult<FaqCategorySummary> searchSummaries(SearchFaqCategoryCommand command);
    boolean existsByName(String name);
    boolean existsByNameExcludingId(String name, Long id);
}
