package com.freightos.admin.adapter.out.persistence.codemaster;

import com.freightos.admin.application.codemaster.command.SearchCodeMasterCommand;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.response.PagedResult;

public interface CodeMasterRepositoryCustom {
    PagedResult<CodeMasterSummary> searchSummaries(SearchCodeMasterCommand command);
}
