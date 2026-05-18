package com.freightos.admin.adapter.out.persistence.menupolicy;

import com.freightos.admin.application.menupolicy.command.SearchMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.projection.MenuPolicySummary;
import com.freightos.admin.common.response.PagedResult;

public interface MenuPolicyRepositoryCustom {
    PagedResult<MenuPolicySummary> searchSummaries(SearchMenuPolicyCommand command);
}
