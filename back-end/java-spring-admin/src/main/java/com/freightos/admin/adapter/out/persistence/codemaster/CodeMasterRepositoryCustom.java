package com.freightos.admin.adapter.out.persistence.codemaster;

import com.freightos.admin.application.codemaster.command.SearchCodeMasterCommand;
import com.freightos.admin.application.codemaster.projection.CodeMasterSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface CodeMasterRepositoryCustom {
    PagedResult<CodeMasterSummary> searchSummaries(SearchCodeMasterCommand command);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
