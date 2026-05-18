package com.freightos.admin.application.menupolicy.port.out;

import com.freightos.admin.application.menupolicy.command.SearchMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.projection.MenuPolicySummary;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.security.MenuEvalRow;
import com.freightos.admin.domain.menupolicy.entity.MenuPolicy;

import java.util.List;
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
    /** ABAC 평가용 — active 메뉴와 그 정책을 한번에 조회한다. */
    List<MenuEvalRow> findAllActiveForEvaluation();
}
