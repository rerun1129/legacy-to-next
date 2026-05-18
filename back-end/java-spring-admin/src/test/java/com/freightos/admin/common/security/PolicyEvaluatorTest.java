package com.freightos.admin.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PolicyEvaluator 단위 테스트.
 *
 * 핵심 의미론:
 *  - 정책 0개 → 모든 사용자 허용
 *  - 같은 key 내 OR: 여러 required_value 중 하나라도 일치하면 통과
 *  - 다른 key 간 AND: 모든 key 를 통과해야 최종 허용
 */
class PolicyEvaluatorTest {

    private PolicyEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new PolicyEvaluator();
    }

    // ── 정책 0개 → 항상 허용 ─────────────────────────────────────────────────

    @Test
    void evaluate_noPolicies_returnsTrue() {
        Map<String, List<String>> attrs = Map.of("role", List.of("USER"));

        boolean result = evaluator.evaluate(attrs, List.of());

        assertThat(result).isTrue();
    }

    // ── 단일 정책 일치 → 허용 ────────────────────────────────────────────────

    @Test
    void evaluate_singlePolicyMatch_returnsTrue() {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        List<PolicyRow> policies = List.of(new PolicyRow("role", "ADMIN"));

        boolean result = evaluator.evaluate(attrs, policies);

        assertThat(result).isTrue();
    }

    // ── 단일 정책 불일치 → 거부 ──────────────────────────────────────────────

    @Test
    void evaluate_singlePolicyNoMatch_returnsFalse() {
        Map<String, List<String>> attrs = Map.of("role", List.of("USER"));
        List<PolicyRow> policies = List.of(new PolicyRow("role", "ADMIN"));

        boolean result = evaluator.evaluate(attrs, policies);

        assertThat(result).isFalse();
    }

    // ── 같은 key 내 OR: 하나라도 일치 → 허용 ─────────────────────────────────

    @Test
    void evaluate_sameKeyOrSemantics_oneMatchReturnsTrue() {
        Map<String, List<String>> attrs = Map.of("role", List.of("USER"));
        List<PolicyRow> policies = List.of(
                new PolicyRow("role", "ADMIN"),
                new PolicyRow("role", "USER")
        );

        boolean result = evaluator.evaluate(attrs, policies);

        assertThat(result).isTrue();
    }

    // ── 다른 key 간 AND: 모두 일치 → 허용 ────────────────────────────────────

    @Test
    void evaluate_differentKeyAndSemantics_allMatchReturnsTrue() {
        Map<String, List<String>> attrs = Map.of(
                "role", List.of("ADMIN"),
                "region", List.of("ASIA")
        );
        List<PolicyRow> policies = List.of(
                new PolicyRow("role", "ADMIN"),
                new PolicyRow("region", "ASIA")
        );

        boolean result = evaluator.evaluate(attrs, policies);

        assertThat(result).isTrue();
    }

    // ── 다른 key 간 AND: 하나 불일치 → 거부 ──────────────────────────────────

    @Test
    void evaluate_differentKeyAndSemantics_oneKeyMissingReturnsFalse() {
        Map<String, List<String>> attrs = Map.of(
                "role", List.of("ADMIN")
                // region 없음
        );
        List<PolicyRow> policies = List.of(
                new PolicyRow("role", "ADMIN"),
                new PolicyRow("region", "ASIA")
        );

        boolean result = evaluator.evaluate(attrs, policies);

        assertThat(result).isFalse();
    }

    // ── 사용자 attributes 에 key 자체가 없음 → 거부 ──────────────────────────

    @Test
    void evaluate_userHasNoAttributeKey_returnsFalse() {
        Map<String, List<String>> attrs = Map.of();
        List<PolicyRow> policies = List.of(new PolicyRow("role", "ADMIN"));

        boolean result = evaluator.evaluate(attrs, policies);

        assertThat(result).isFalse();
    }

    // ── accessibleMenuCodes: 빈 정책 메뉴 포함 + 정책 있는 메뉴 필터링 ──────

    @Test
    void accessibleMenuCodes_mixedPolicies_correctlyFilters() {
        Map<String, List<String>> attrs = Map.of("role", List.of("ADMIN"));
        List<PolicyRow> adminPolicy = List.of(new PolicyRow("role", "ADMIN"));
        List<MenuEvalRow> menuRows = List.of(
                new MenuEvalRow(1L, "MENU_A", List.of()),               // 정책 없음 → 허용
                new MenuEvalRow(2L, "MENU_B", adminPolicy),             // ADMIN 일치 → 허용
                new MenuEvalRow(3L, "MENU_C", List.of(new PolicyRow("role", "SUPERADMIN")))  // 불일치 → 거부
        );

        Set<String> result = evaluator.accessibleMenuCodes(attrs, menuRows);

        assertThat(result).containsExactlyInAnyOrder("MENU_A", "MENU_B");
        assertThat(result).doesNotContain("MENU_C");
    }

    // ── accessibleButtonCodes: 버튼 접근 가능 집합 반환 ──────────────────────

    @Test
    void accessibleButtonCodes_returnsOnlyMatchingButtons() {
        Map<String, List<String>> attrs = Map.of("role", List.of("USER"));
        List<ButtonEvalRow> buttonRows = List.of(
                new ButtonEvalRow(1L, "BTN_CREATE", List.of(new PolicyRow("role", "ADMIN"))),
                new ButtonEvalRow(2L, "BTN_VIEW", List.of(new PolicyRow("role", "USER"))),
                new ButtonEvalRow(3L, "BTN_PUBLIC", List.of())
        );

        Set<String> result = evaluator.accessibleButtonCodes(attrs, buttonRows);

        assertThat(result).containsExactlyInAnyOrder("BTN_VIEW", "BTN_PUBLIC");
        assertThat(result).doesNotContain("BTN_CREATE");
    }
}
