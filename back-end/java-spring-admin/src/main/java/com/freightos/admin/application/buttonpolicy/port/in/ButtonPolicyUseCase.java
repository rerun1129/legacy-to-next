package com.freightos.admin.application.buttonpolicy.port.in;

import com.freightos.admin.application.buttonpolicy.command.CreateButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.command.SearchButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.projection.ButtonPolicySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.buttonpolicy.entity.ButtonPolicy;

public interface ButtonPolicyUseCase {
    PagedResult<ButtonPolicySummary> searchButtonPolicies(SearchButtonPolicyCommand command);
    ButtonPolicy findButtonPolicyById(Long policyId);
    Long createButtonPolicy(CreateButtonPolicyCommand command);
    void deleteButtonPolicyById(Long policyId);
}
