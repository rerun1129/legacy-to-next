package com.freightos.admin.adapter.in.web.faqcategory;

import com.freightos.admin.adapter.in.web.faqcategory.dto.CreateFaqCategoryRequest;
import com.freightos.admin.adapter.in.web.faqcategory.dto.FaqCategoryDetailResponse;
import com.freightos.admin.adapter.in.web.faqcategory.dto.FaqCategorySummaryResponse;
import com.freightos.admin.adapter.in.web.faqcategory.dto.SearchFaqCategoryRequest;
import com.freightos.admin.adapter.in.web.faqcategory.dto.UpdateFaqCategoryRequest;
import com.freightos.admin.application.faqcategory.command.CreateFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.command.SearchFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.command.UpdateFaqCategoryCommand;
import com.freightos.admin.application.faqcategory.projection.FaqCategorySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faqcategory.entity.FaqCategory;
import org.springframework.stereotype.Component;

@Component
public class FaqCategoryAssembler {

    public SearchFaqCategoryCommand toSearchCommand(SearchFaqCategoryRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchFaqCategoryCommand(req.name(), req.scope(), req.page(), size);
    }

    public CreateFaqCategoryCommand toCreateCommand(CreateFaqCategoryRequest req) {
        return new CreateFaqCategoryCommand(req.name(), req.sortOrder(), req.active());
    }

    public UpdateFaqCategoryCommand toUpdateCommand(UpdateFaqCategoryRequest req) {
        return new UpdateFaqCategoryCommand(req.name(), req.sortOrder(), req.active());
    }

    public FaqCategorySummaryResponse toSummaryResponse(FaqCategorySummary s) {
        return new FaqCategorySummaryResponse(s.faqCategoryId(), s.name(), s.sortOrder(), s.active(), s.deletedAt(), s.updatedAt());
    }

    public FaqCategoryDetailResponse toDetail(FaqCategory domain) {
        return new FaqCategoryDetailResponse(domain.getId(), domain.getName(), domain.getSortOrder(), domain.isActive(), domain.getDeletedAt(), domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy());
    }

    public PagedResult<FaqCategorySummaryResponse> toSummaryPage(PagedResult<FaqCategorySummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
