package com.freightos.admin.adapter.out.persistence.menupolicy;

import com.freightos.admin.application.menupolicy.command.SearchMenuPolicyCommand;
import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.application.menupolicy.projection.MenuPolicySummary;
import com.freightos.admin.common.exception.ApplicationException;
import com.freightos.admin.common.response.MessageCode;
import com.freightos.admin.common.response.PagedResult;
import com.freightos.admin.common.security.MenuEvalRow;
import com.freightos.admin.common.security.PolicyRow;
import com.freightos.admin.domain.menupolicy.entity.MenuPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MenuPolicyPersistenceAdapter implements MenuPolicyPort {

    private final MenuPolicyRepository menuPolicyRepository;
    private final MenuPolicyDomainToJpaMapper menuPolicyDomainToJpaMapper;
    private final MenuPolicyJpaToDomainMapper menuPolicyJpaToDomainMapper;

    @Override
    public PagedResult<MenuPolicySummary> searchSummaries(SearchMenuPolicyCommand command) {
        return menuPolicyRepository.searchSummaries(command);
    }

    @Override
    public Optional<MenuPolicy> findMenuPolicyById(Long policyId) {
        return menuPolicyRepository.findById(policyId).map(menuPolicyJpaToDomainMapper::toDomain);
    }

    @Override
    public Long save(MenuPolicy menuPolicy) {
        MenuPolicyJpaEntity entity = menuPolicyDomainToJpaMapper.toNewJpa(menuPolicy);
        menuPolicyRepository.save(entity);
        return entity.getId();
    }

    @Override
    public void deleteMenuPolicyById(Long policyId) {
        if (!menuPolicyRepository.existsById(policyId)) {
            throw ApplicationException.notFound("MENU_POLICY_NOT_FOUND", MessageCode.MENU_POLICY_NOT_FOUND.getMessage());
        }
        menuPolicyRepository.deleteById(policyId);
    }

    @Override
    public boolean existsById(Long policyId) {
        return menuPolicyRepository.existsById(policyId);
    }

    @Override
    public boolean existsByCompositeKey(Long menuId, String attributeKey, String requiredValue) {
        return menuPolicyRepository.existsByMenuIdAndAttributeKeyAndRequiredValue(menuId, attributeKey, requiredValue);
    }

    @Override
    public boolean existsByMenuId(Long menuId) {
        return menuPolicyRepository.existsByMenuId(menuId);
    }

    @Override
    public boolean existsByAttributeKey(String attributeKey) {
        return menuPolicyRepository.existsByAttributeKey(attributeKey);
    }

    @Override
    public List<MenuEvalRow> findAllActiveForEvaluation() {
        // active 메뉴와 그에 연결된 정책을 메모리에서 그룹화.
        // 데이터량이 작으므로 전체 fetch 후 in-memory join.
        List<MenuPolicyEvalProjection> rows = menuPolicyRepository.findAllActiveMenusWithPolicies();
        Map<Long, MenuEvalRow> byMenuId = new HashMap<>();
        for (MenuPolicyEvalProjection row : rows) {
            byMenuId.computeIfAbsent(row.getMenuId(), id -> new MenuEvalRow(id, row.getMenuCode(), new ArrayList<>()));
            if (row.getAttributeKey() != null) {
                // computeIfAbsent 로 생성된 row 는 mutable ArrayList 를 갖는다.
                ((ArrayList<PolicyRow>) byMenuId.get(row.getMenuId()).policies())
                        .add(new PolicyRow(row.getAttributeKey(), row.getRequiredValue()));
            }
        }
        return new ArrayList<>(byMenuId.values());
    }
}
