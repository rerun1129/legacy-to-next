package com.freightos.admin.adapter.out.persistence.menu;

import com.freightos.admin.application.menu.command.SearchMenuCommand;
import com.freightos.admin.application.menu.projection.MenuSummary;
import com.freightos.admin.common.response.PagedResult;

public interface MenuRepositoryCustom {
    PagedResult<MenuSummary> searchSummaries(SearchMenuCommand command);
}
