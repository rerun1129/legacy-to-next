package com.freightos.admin.application.buttonpolicy.port.out;

import com.freightos.admin.application.buttonpolicy.command.SearchButtonPolicyCommand;
import com.freightos.admin.application.buttonpolicy.projection.ButtonPolicySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.buttonpolicy.entity.ButtonPolicy;

import java.util.Optional;

public interface ButtonPolicyPort {
    PagedResult<ButtonPolicySummary> searchSummaries(SearchButtonPolicyCommand command);
    Optional<ButtonPolicy> findButtonPolicyById(Long policyId);
    Long save(ButtonPolicy buttonPolicy);
    void deleteButtonPolicyById(Long policyId);
    boolean existsById(Long policyId);
    boolean existsByCompositeKey(Long buttonId, String attributeKey, String requiredValue);
    boolean existsByButtonId(Long buttonId);
    boolean existsByAttributeKey(String attributeKey);
}
