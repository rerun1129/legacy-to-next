package com.freightos.admin.adapter.out.persistence.buttonpolicy;

import com.freightos.admin.application.buttonpolicy.command.SearchButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.projection.ButtonPolicySummary;
import com.freightos.admin.common.response.PagedResult;

public interface ButtonPolicyRepositoryCustom {
    PagedResult<ButtonPolicySummary> searchSummaries(SearchButtonPolicyCommand command);
}
