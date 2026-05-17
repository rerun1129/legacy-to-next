package com.freightos.admin.adapter.in.web.faq;

import com.freightos.admin.adapter.in.web.faq.dto.CreateFaqRequest;
import com.freightos.admin.adapter.in.web.faq.dto.FaqDetailResponse;
import com.freightos.admin.adapter.in.web.faq.dto.FaqSummaryResponse;
import com.freightos.admin.adapter.in.web.faq.dto.SearchFaqRequest;
import com.freightos.admin.adapter.in.web.faq.dto.UpdateFaqRequest;
import com.freightos.admin.application.faq.command.CreateFaqCommand;
import com.freightos.admin.application.faq.command.SearchFaqCommand;
import com.freightos.admin.application.faq.command.UpdateFaqCommand;
import com.freightos.admin.application.faq.projection.FaqSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.faq.entity.Faq;
import org.springframework.stereotype.Component;

@Component
public class FaqAssembler {

    public SearchFaqCommand toSearchCommand(SearchFaqRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchFaqCommand(req.faqCategoryId(), req.question(), req.scope(), req.page(), size);
    }

    public CreateFaqCommand toCreateCommand(CreateFaqRequest req) {
        return new CreateFaqCommand(req.faqCategoryId(), req.question(), req.answer(), req.sortOrder(), req.active());
    }

    public UpdateFaqCommand toUpdateCommand(UpdateFaqRequest req) {
        return new UpdateFaqCommand(req.faqCategoryId(), req.question(), req.answer(), req.sortOrder(), req.active());
    }

    public FaqSummaryResponse toSummaryResponse(FaqSummary s) {
        return new FaqSummaryResponse(s.faqId(), s.faqCategoryId(), s.question(), s.sortOrder(), s.active(), s.deletedAt(), s.updatedAt());
    }

    public FaqDetailResponse toDetail(Faq domain) {
        return new FaqDetailResponse(domain.getId(), domain.getFaqCategoryId(), domain.getQuestion(), domain.getAnswer(), domain.getSortOrder(), domain.isActive(), domain.getDeletedAt(), domain.getCreatedAt(), domain.getUpdatedAt(), domain.getCreatedBy(), domain.getUpdatedBy());
    }

    public PagedResult<FaqSummaryResponse> toSummaryPage(PagedResult<FaqSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
