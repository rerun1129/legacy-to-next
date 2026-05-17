package com.freightos.admin.adapter.in.web.terms;

import com.freightos.admin.adapter.in.web.terms.dto.CreateTermsRequest;
import com.freightos.admin.adapter.in.web.terms.dto.SearchTermsRequest;
import com.freightos.admin.adapter.in.web.terms.dto.TermsDetailResponse;
import com.freightos.admin.adapter.in.web.terms.dto.TermsSummaryResponse;
import com.freightos.admin.adapter.in.web.terms.dto.UpdateTermsRequest;
import com.freightos.admin.application.terms.command.CreateTermsCommand;
import com.freightos.admin.application.terms.command.SearchTermsCommand;
import com.freightos.admin.application.terms.command.UpdateTermsCommand;
import com.freightos.admin.application.terms.projection.TermsSummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.terms.entity.Terms;
import org.springframework.stereotype.Component;

@Component
public class TermsAssembler {

    public SearchTermsCommand toSearchCommand(SearchTermsRequest req) {
        int size = req.size() == 0 ? 20 : req.size();
        return new SearchTermsCommand(req.type(), req.scope(), req.version(), req.summary(), req.page(), size);
    }

    public CreateTermsCommand toCreateCommand(CreateTermsRequest req) {
        return new CreateTermsCommand(req.type(), req.version(), req.effectiveAt(), req.content(), req.summary());
    }

    public UpdateTermsCommand toUpdateCommand(UpdateTermsRequest req) {
        return new UpdateTermsCommand(req.content(), req.summary(), req.effectiveAt());
    }

    public TermsSummaryResponse toSummaryResponse(TermsSummary s) {
        return new TermsSummaryResponse(s.termsId(), s.type(), s.version(), s.effectiveAt(), s.summary(), s.deletedAt(), s.updatedAt());
    }

    public TermsDetailResponse toDetail(Terms domain) {
        return new TermsDetailResponse(
                domain.getId(),
                domain.getType().name(),
                domain.getVersion(),
                domain.getEffectiveAt(),
                domain.getContent(),
                domain.getSummary(),
                domain.getDeletedAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getCreatedBy(),
                domain.getUpdatedBy()
        );
    }

    public PagedResult<TermsSummaryResponse> toSummaryPage(PagedResult<TermsSummary> src) {
        return src.map(this::toSummaryResponse);
    }
}
