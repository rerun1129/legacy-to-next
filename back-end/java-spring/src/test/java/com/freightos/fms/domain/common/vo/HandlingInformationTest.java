package com.freightos.fms.domain.common.vo;

import com.freightos.fms.domain.housebl.enums.HandlingInfoCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HandlingInformation.of() 동작 규칙:
 *   - code == null AND (description == null OR blank) → null 반환
 *   - 그 외 모든 조합 → HandlingInformation 인스턴스 반환
 *   (code가 null이어도 description이 있으면 인스턴스를 반환하고,
 *    description이 없어도 code가 있으면 인스턴스를 반환)
 */
@DisplayName("HandlingInformation 단위 테스트")
class HandlingInformationTest {

    @Test
    @DisplayName("code 있음 + description 있음 → non-null HandlingInformation 반환")
    void of_codeAndDesc_returnsInstance() {
        HandlingInformation result = HandlingInformation.of(HandlingInfoCode.A, "ATTACHED : COMM INV & P/LIST");
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo(HandlingInfoCode.A);
        assertThat(result.description()).isEqualTo("ATTACHED : COMM INV & P/LIST");
    }

    @Test
    @DisplayName("code null + description null → null 반환")
    void of_bothNull_returnsNull() {
        HandlingInformation result = HandlingInformation.of(null, null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("code null + description blank → null 반환")
    void of_nullCode_blankDesc_returnsNull() {
        HandlingInformation result = HandlingInformation.of(null, "  ");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("code null + description 있음 → non-null 반환 (descEmpty=false 이므로 조건 불충족)")
    void of_nullCode_validDesc_returnsInstance() {
        // code==null, descEmpty==false → codeEmpty&&descEmpty 불충족 → 인스턴스 반환
        HandlingInformation result = HandlingInformation.of(null, "SOME DESCRIPTION");
        assertThat(result).isNotNull();
        assertThat(result.code()).isNull();
        assertThat(result.description()).isEqualTo("SOME DESCRIPTION");
    }

    @Test
    @DisplayName("code 있음 + description null → non-null 반환 (codeEmpty=false 이므로 조건 불충족)")
    void of_validCode_nullDesc_returnsInstance() {
        // codeEmpty==false → codeEmpty&&descEmpty 불충족 → 인스턴스 반환
        HandlingInformation result = HandlingInformation.of(HandlingInfoCode.B, null);
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo(HandlingInfoCode.B);
        assertThat(result.description()).isNull();
    }

    @Test
    @DisplayName("code 있음 + description blank → non-null 반환 (codeEmpty=false 이므로 조건 불충족)")
    void of_validCode_blankDesc_returnsInstance() {
        HandlingInformation result = HandlingInformation.of(HandlingInfoCode.B, "  ");
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo(HandlingInfoCode.B);
        assertThat(result.description()).isEqualTo("  ");
    }
}
