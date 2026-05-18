package com.freightos.admin.application.menupolicy.port.out;

import com.freightos.admin.application.menupolicy.command.SearchMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.projection.MenuPolicySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.menupolicy.entity.MenuPolicy;

import java.util.Optional;

public interface MenuPolicyPort {
    PagedResult<MenuPolicySummary> searchSummaries(SearchMenuPolicyCommand command);
    Optional<MenuPolicy> findMenuPolicyById(Long policyId);
    Long save(MenuPolicy menuPolicy);
    void deleteMenuPolicyById(Long policyId);
    boolean existsById(Long policyId);
    boolean existsByCompositeKey(Long menuId, String attributeKey, String requiredValue);
    boolean existsByMenuId(Long menuId);
    boolean existsByAttributeKey(String attributeKey);
}
