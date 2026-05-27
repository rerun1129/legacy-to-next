package com.freightos.admin.adapter.out.persistence.code.hscode;

import com.freightos.admin.application.code.hscode.command.SearchHsCodeCommand;
import com.freightos.admin.application.code.hscode.projection.HsCodeSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface HsCodeRepositoryCustom {
    PagedResult<HsCodeSummary> searchSummaries(SearchHsCodeCommand command);
    List<AutocompleteItem> autocomplete(String query, int limit);
}
