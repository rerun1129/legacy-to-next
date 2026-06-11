package com.freightos.pms.application.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CommonCodeGroupAliases 순수 단위 테스트.
 * I/O 없음 — 결정적, 비결정적 요소 없음.
 */
class CommonCodeGroupAliasesTest {

    @Test
    @DisplayName("JobDiv API 키는 housebl.JobDiv DB group_code로 변환된다")
    void jobDiv_mapsToDbGroup() {
        assertThat(CommonCodeGroupAliases.toDbGroup("JobDiv")).isEqualTo("housebl.JobDiv");
    }

    @Test
    @DisplayName("JobDiv는 별칭이 등록되어 있다")
    void jobDiv_hasAlias() {
        assertThat(CommonCodeGroupAliases.hasAlias("JobDiv")).isTrue();
    }

    @ParameterizedTest
    @DisplayName("별칭 미등록 키는 원 키 그대로 반환된다")
    @ValueSource(strings = {"AggregationBasis", "Bound", "DateKind", "PortKind", "DocumentType", "DocumentStatus"})
    void nonAliasedKeys_returnAsIs(String apiKey) {
        assertThat(CommonCodeGroupAliases.toDbGroup(apiKey)).isEqualTo(apiKey);
    }

    @ParameterizedTest
    @DisplayName("별칭 미등록 키는 hasAlias가 false를 반환한다")
    @ValueSource(strings = {"AggregationBasis", "Bound", "DateKind", "PortKind", "DocumentType", "DocumentStatus"})
    void nonAliasedKeys_hasAliasFalse(String apiKey) {
        assertThat(CommonCodeGroupAliases.hasAlias(apiKey)).isFalse();
    }
}
