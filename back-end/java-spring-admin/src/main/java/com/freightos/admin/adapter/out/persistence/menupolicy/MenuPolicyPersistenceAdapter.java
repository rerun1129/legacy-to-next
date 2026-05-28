package com.freightos.admin.adapter.out.persistence.menupolicy;

import com.freightos.admin.application.menupolicy.port.out.MenuPolicyPort;
import com.freightos.admin.common.security.MenuEvalRow;
import com.freightos.admin.common.security.PolicyRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MenuPolicyPersistenceAdapter implements MenuPolicyPort {

    private final MenuPolicyRepository menuPolicyRepository;

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

    @Override
    public List<String> findDistinctAttributeKeysByModuleCode(String moduleCode) {
        return menuPolicyRepository.findDistinctAttributeKeysByModuleCode(moduleCode);
    }
}
