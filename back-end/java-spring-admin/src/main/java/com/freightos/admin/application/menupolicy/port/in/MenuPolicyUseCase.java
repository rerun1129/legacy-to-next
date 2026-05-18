package com.freightos.admin.application.menupolicy.port.in;

import com.freightos.admin.application.menupolicy.command.CreateMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.command.SearchMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.projection.MenuPolicySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.menupolicy.entity.MenuPolicy;

public interface MenuPolicyUseCase {
    PagedResult<MenuPolicySummary> searchMenuPolicies(SearchMenuPolicyCommand command);
    MenuPolicy findMenuPolicyById(Long policyId);
    Long createMenuPolicy(CreateMenuPolicyCommand command);
    void deleteMenuPolicyById(Long policyId);
}
