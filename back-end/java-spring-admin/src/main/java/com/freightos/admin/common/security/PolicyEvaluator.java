package com.freightos.admin.common.security;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ABAC 정책 평가기.
 *
 * 평가 의미론:
 *  - 같은 attribute_key 내 여러 required_value → OR (하나라도 일치하면 통과)
 *  - 다른 attribute_key 간 → AND (모든 key 를 통과해야 최종 허용)
 *  - 정책이 0개인 메뉴/버튼 → 모든 사용자 허용 (deny-by-default 아님)
 */
@Component
public class PolicyEvaluator {

    /**
     * 접근 가능한 menu_code 집합을 반환한다.
     *
     * @param attrs      사용자 ABAC 속성 맵 (attribute_key → 값 목록)
     * @param menuRows   메뉴별 평가 데이터 목록
     * @return 접근 허용된 menuCode 집합
     */
    public Set<String> accessibleMenuCodes(Map<String, List<String>> attrs, List<MenuEvalRow> menuRows) {
        Set<String> accessible = new HashSet<>();
        for (MenuEvalRow row : menuRows) {
            if (evaluate(attrs, row.policies())) {
                accessible.add(row.menuCode());
            }
        }
        return accessible;
    }

    /**
     * 접근 가능한 button_code 집합을 반환한다.
     *
     * @param attrs       사용자 ABAC 속성 맵 (attribute_key → 값 목록)
     * @param buttonRows  버튼별 평가 데이터 목록
     * @return 접근 허용된 buttonCode 집합
     */
    public Set<String> accessibleButtonCodes(Map<String, List<String>> attrs, List<ButtonEvalRow> buttonRows) {
        Set<String> accessible = new HashSet<>();
        for (ButtonEvalRow row : buttonRows) {
            if (evaluate(attrs, row.policies())) {
                accessible.add(row.buttonCode());
            }
        }
        return accessible;
    }

    /**
     * 단일 메뉴/버튼에 대한 정책 평가.
     * 정책 목록이 비어 있으면 허용(deny-by-default 미적용).
     *
     * @param attrs    사용자 ABAC 속성 맵
     * @param policies 해당 리소스의 정책 목록
     * @return 접근 허용 여부
     */
    public boolean evaluate(Map<String, List<String>> attrs, List<PolicyRow> policies) {
        if (policies == null || policies.isEmpty()) {
            return true;
        }
        // key 별로 required_value 집합을 그룹화 (같은 key 내 OR)
        Map<String, Set<String>> requiredByKey = new HashMap<>();
        for (PolicyRow policy : policies) {
            requiredByKey.computeIfAbsent(policy.attributeKey(), k -> new HashSet<>())
                         .add(policy.requiredValue());
        }
        // 모든 key 에 대해 AND 검사
        for (Map.Entry<String, Set<String>> entry : requiredByKey.entrySet()) {
            String key = entry.getKey();
            Set<String> requiredValues = entry.getValue();
            List<String> userValues = attrs.getOrDefault(key, List.of());
            boolean anyMatch = userValues.stream().anyMatch(requiredValues::contains);
            if (!anyMatch) {
                return false;
            }
        }
        return true;
    }
}
