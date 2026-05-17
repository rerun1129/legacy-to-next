package com.freightos.admin.adapter.out.persistence.code;

import com.freightos.admin.application.code.command.SearchCodeCommand;
import com.freightos.admin.application.code.projection.CodeSummary;
import com.freightos.admin.common.response.PagedResult;

public interface CodeRepositoryCustom {
    PagedResult<CodeSummary> searchSummaries(SearchCodeCommand command);
}
