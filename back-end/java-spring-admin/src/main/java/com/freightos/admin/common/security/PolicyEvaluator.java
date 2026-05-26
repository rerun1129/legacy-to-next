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
 *
 * 정책 0개 처리 방식은 호출자가 선택한다:
 *  - denyIfNoPolicies=false (기본): 정책 없음 → 허용 (기존 동작 유지)
 *  - denyIfNoPolicies=true : 정책 없음 → 차단 (deny-by-default)
 */
@Component
public class PolicyEvaluator {

    /**
     * 접근 가능한 menu_code 집합을 반환한다. 정책 0개 메뉴는 허용(기존 동작).
     *
     * @param attrs      사용자 ABAC 속성 맵 (attribute_key → 값 목록)
     * @param menuRows   메뉴별 평가 데이터 목록
     * @return 접근 허용된 menuCode 집합
     */
    public Set<String> accessibleMenuCodes(Map<String, List<String>> attrs, List<MenuEvalRow> menuRows) {
        return accessibleMenuCodes(attrs, menuRows, false);
    }

    /**
     * 접근 가능한 menu_code 집합을 반환한다.
     *
     * @param attrs              사용자 ABAC 속성 맵 (attribute_key → 값 목록)
     * @param menuRows           메뉴별 평가 데이터 목록
     * @param denyIfNoPolicies   true 이면 정책 0개 메뉴를 차단(deny-by-default)
     * @return 접근 허용된 menuCode 집합
     */
    public Set<String> accessibleMenuCodes(Map<String, List<String>> attrs, List<MenuEvalRow> menuRows, boolean denyIfNoPolicies) {
        Set<String> accessible = new HashSet<>();
        for (MenuEvalRow row : menuRows) {
            if (evaluate(attrs, row.policies(), denyIfNoPolicies)) {
                accessible.add(row.menuCode());
            }
        }
        return accessible;
    }

    /**
     * 접근 가능한 button_code 집합을 반환한다. 정책 0개 버튼은 허용(기존 동작).
     *
     * @param attrs       사용자 ABAC 속성 맵 (attribute_key → 값 목록)
     * @param buttonRows  버튼별 평가 데이터 목록
     * @return 접근 허용된 buttonCode 집합
     */
    public Set<String> accessibleButtonCodes(Map<String, List<String>> attrs, List<ButtonEvalRow> buttonRows) {
        return accessibleButtonCodes(attrs, buttonRows, false);
    }

    /**
     * 접근 가능한 button_code 집합을 반환한다.
     *
     * @param attrs              사용자 ABAC 속성 맵 (attribute_key → 값 목록)
     * @param buttonRows         버튼별 평가 데이터 목록
     * @param denyIfNoPolicies   true 이면 정책 0개 버튼을 차단(deny-by-default)
     * @return 접근 허용된 buttonCode 집합
     */
    public Set<String> accessibleButtonCodes(Map<String, List<String>> attrs, List<ButtonEvalRow> buttonRows, boolean denyIfNoPolicies) {
        Set<String> accessible = new HashSet<>();
        for (ButtonEvalRow row : buttonRows) {
            if (evaluate(attrs, row.policies(), denyIfNoPolicies)) {
                accessible.add(row.buttonCode());
            }
        }
        return accessible;
    }

    /**
     * 단일 메뉴/버튼에 대한 정책 평가. 정책 0개이면 허용(기존 동작).
     *
     * @param attrs    사용자 ABAC 속성 맵
     * @param policies 해당 리소스의 정책 목록
     * @return 접근 허용 여부
     */
    public boolean evaluate(Map<String, List<String>> attrs, List<PolicyRow> policies) {
        return evaluate(attrs, policies, false);
    }

    /**
     * 단일 메뉴/버튼에 대한 정책 평가.
     *
     * @param attrs              사용자 ABAC 속성 맵
     * @param policies           해당 리소스의 정책 목록
     * @param denyIfNoPolicies   true 이면 정책 0개 리소스를 차단(deny-by-default)
     * @return 접근 허용 여부
     */
    public boolean evaluate(Map<String, List<String>> attrs, List<PolicyRow> policies, boolean denyIfNoPolicies) {
        if (policies == null || policies.isEmpty()) {
            return !denyIfNoPolicies;
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
