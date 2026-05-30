package com.freightos.admin.adapter.out.persistence.code.port;

import com.freightos.admin.application.code.port.command.SearchPortCommand;
import com.freightos.admin.application.code.port.projection.PortSummary;
import com.freightos.admin.common.response.AutocompleteItem;
import com.freightos.admin.common.response.PagedResult;

import java.util.List;

public interface PortRepositoryCustom {
    PagedResult<PortSummary> searchSummaries(SearchPortCommand command);
    List<AutocompleteItem> autocomplete(String query, String type, int limit);
}
