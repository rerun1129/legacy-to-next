package com.freightos.admin.application.faqcategory.port.out;

import com.freightos.admin.application.faqcategory.command.SearchFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.projection.FaqCategorySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faqcategory.entity.FaqCategory;

import java.util.Optional;

public interface FaqCategoryPort {
    PagedResult<FaqCategorySummary> searchSummaries(SearchFaqCategoryCommand command);
    Optional<FaqCategory> findById(Long id);
    Long save(FaqCategory faqCategory);
    void update(Long id, FaqCategory patchData);
    void softDelete(Long id);
    boolean existsByName(String name);
    boolean existsByNameExcludingId(String name, Long id);
}
