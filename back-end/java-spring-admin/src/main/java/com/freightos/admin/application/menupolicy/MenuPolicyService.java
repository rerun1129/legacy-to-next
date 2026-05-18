package com.freightos.admin.application.menupolicy;

import com.freightos.admin.application.attributedefinition.port.out.AttributeDefinitionPort;
import com.freightos.admin.application.menu.port.out.MenuPort;
import com.freightos.admin.application.menupolicy.command.CreateMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.command.SearchMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.port.in.MenuPolicyUseCase;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.application.menupolicy.projection.MenuPolicySummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.domain.menupolicy.entity.MenuPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuPolicyService implements MenuPolicyUseCase {

    private final MenuPolicyPort menuPolicyPort;
    private final MenuPort menuPort;
    private final AttributeDefinitionPort attributeDefinitionPort;

    @Override
    public PagedResult<MenuPolicySummary> searchMenuPolicies(SearchMenuPolicyCommand command) {
        return menuPolicyPort.searchSummaries(command);
    }

    @Override
    public MenuPolicy findMenuPolicyById(Long policyId) {
        return menuPolicyPort.findMenuPolicyById(policyId)
                .orElseThrow(() -> ApplicationException.notFound("MENU_POLICY_NOT_FOUND", MessageCode.MENU_POLICY_NOT_FOUND.getMessage()));
    }

    @Override
    @Transactional
    public Long createMenuPolicy(CreateMenuPolicyCommand command) {
        if (!menuPort.existsById(command.menuId())) {
            throw ApplicationException.notFound("MENU_NOT_FOUND", MessageCode.MENU_NOT_FOUND.getMessage());
        }
        if (!attributeDefinitionPort.existsByKey(command.attributeKey())) {
            throw ApplicationException.notFound("ATTRIBUTE_DEFINITION_NOT_FOUND", MessageCode.ATTRIBUTE_DEFINITION_NOT_FOUND.getMessage());
        }
        if (menuPolicyPort.existsByCompositeKey(command.menuId(), command.attributeKey(), command.requiredValue())) {
            throw ApplicationException.conflict("MENU_POLICY_DUPLICATE", MessageCode.MENU_POLICY_DUPLICATE.getMessage());
        }
        MenuPolicy menuPolicy = MenuPolicy.create(command.menuId(), command.attributeKey(), command.requiredValue());
        return menuPolicyPort.save(menuPolicy);
    }

    @Override
    @Transactional
    public void deleteMenuPoliciesByIds(List<Long> ids) {
        for (Long id : ids) {
            deleteMenuPolicyById(id);
        }
    }

    @Override
    @Transactional
    public void deleteMenuPolicyById(Long policyId) {
        if (!menuPolicyPort.existsById(policyId)) {
            throw ApplicationException.notFound("MENU_POLICY_NOT_FOUND", MessageCode.MENU_POLICY_NOT_FOUND.getMessage());
        }
        menuPolicyPort.deleteMenuPolicyById(policyId);
    }
}
