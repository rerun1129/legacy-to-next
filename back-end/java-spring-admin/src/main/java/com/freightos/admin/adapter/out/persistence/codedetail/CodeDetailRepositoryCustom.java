package com.freightos.admin.adapter.out.persistence.codedetail;

import com.freightos.admin.application.codedetail.command.SearchCodeDetailCommand;
import com.freightos.admin.application.codedetail.projection.CodeDetailSummary;
import com.freightos.admin.common.response.PagedResult;

public interface CodeDetailRepositoryCustom {
    PagedResult<CodeDetailSummary> searchSummaries(SearchCodeDetailCommand command);
}
