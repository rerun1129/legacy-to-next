package com.freightos.admin.adapter.out.persistence.button;

import com.freightos.admin.application.button.command.SearchButtonCommand;
import com.freightos.admin.application.button.projection.ButtonSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface ButtonRepositoryCustom {
    PagedResult<ButtonSummary> searchSummaries(SearchButtonCommand command);
    List<AutocompleteItem> autocompleteButtonCodes(String query, int limit);
}
