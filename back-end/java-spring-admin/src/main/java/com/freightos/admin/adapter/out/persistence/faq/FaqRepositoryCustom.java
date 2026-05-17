package com.freightos.admin.adapter.out.persistence.faq;

import com.freightos.admin.application.faq.command.SearchFaqCommand;
import com.freightos.admin.application.faq.projection.FaqSummary;
import com.freightos.admin.common.response.PagedResult;

public interface FaqRepositoryCustom {
    PagedResult<FaqSummary> searchSummaries(SearchFaqCommand command);
}
