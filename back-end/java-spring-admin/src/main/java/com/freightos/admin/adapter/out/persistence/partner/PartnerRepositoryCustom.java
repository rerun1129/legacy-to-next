package com.freightos.admin.adapter.out.persistence.partner;

import com.freightos.admin.application.partner.command.SearchPartnerCommand;
import com.freightos.admin.application.partner.projection.PartnerSummary;
import com.freightos.admin.common.response.PagedResult;

public interface PartnerRepositoryCustom {
    PagedResult<PartnerSummary> searchSummaries(SearchPartnerCommand command);
}
