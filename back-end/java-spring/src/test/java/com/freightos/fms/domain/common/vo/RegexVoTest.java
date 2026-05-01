package com.freightos.fms.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("정규식 검증 VO 단위 테스트")
class RegexVoTest {

    // ────────────────────────────────────────────────
    // ContainerNumber  — 정규식: ^[A-Z]{4}\d{7}$
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("ContainerNumber")
    class ContainerNumberTests {

        @Test
        @DisplayName("유효한 컨테이너 번호 'ABCD1234567' 정상 생성")
        void valid_containerNumber() {
            ContainerNumber cn = new ContainerNumber("ABCD1234567");
            assertThat(cn.value()).isEqualTo("ABCD1234567");
        }

        @Test
        @DisplayName("소문자 포함 'abcd1234567' — IllegalArgumentException")
        void lowercase_throwsIae() {
            assertThatThrownBy(() -> new ContainerNumber("abcd1234567"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid container number format");
        }

        @Test
        @DisplayName("자리 수 부족 'ABCD12345' (9자리) — IllegalArgumentException")
        void tooShort_throwsIae() {
            assertThatThrownBy(() -> new ContainerNumber("ABCD12345"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid container number format");
        }

        @Test
        @DisplayName("자리 수 초과 'ABCD12345678' (12자리) — IllegalArgumentException")
        void tooLong_throwsIae() {
            assertThatThrownBy(() -> new ContainerNumber("ABCD12345678"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid container number format");
        }

        @Test
        @DisplayName("숫자 시작 '1234ABCDEFG' — IllegalArgumentException")
        void digitsFirst_throwsIae() {
            assertThatThrownBy(() -> new ContainerNumber("1234ABCDEFG"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid container number format");
        }

        @Test
        @DisplayName("null 전달 시 NullPointerException")
        void null_throwsNpe() {
            assertThatThrownBy(() -> new ContainerNumber(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("ContainerNumber value must not be null");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void blank_throwsIae() {
            assertThatThrownBy(() -> new ContainerNumber(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ContainerNumber value must not be blank");
        }

        @Test
        @DisplayName("of() — null 입력 시 null 반환")
        void of_null_returnsNull() {
            assertThat(ContainerNumber.of(null)).isNull();
        }

        @Test
        @DisplayName("of() — blank 입력 시 null 반환")
        void of_blank_returnsNull() {
            assertThat(ContainerNumber.of("  ")).isNull();
        }

        @Test
        @DisplayName("of() — 잘못된 형식 입력 시 IllegalArgumentException 전파")
        void of_invalidFormat_throwsIae() {
            assertThatThrownBy(() -> ContainerNumber.of("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid container number format");
        }
    }

    // ────────────────────────────────────────────────
    // CurrencyCode  — 정규식: ^[A-Z]{3}$
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("CurrencyCode")
    class CurrencyCodeTests {

        @ParameterizedTest(name = "유효한 통화 코드 ''{0}'' 정상 생성")
        @ValueSource(strings = {"USD", "KRW"})
        @DisplayName("유효한 3자리 대문자 통화 코드 정상 생성")
        void valid_currencyCode(String code) {
            CurrencyCode cc = new CurrencyCode(code);
            assertThat(cc.value()).isEqualTo(code);
        }

        @Test
        @DisplayName("소문자 'usd' — IllegalArgumentException")
        void lowercase_throwsIae() {
            assertThatThrownBy(() -> new CurrencyCode("usd"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid ISO 4217 currency code");
        }

        @Test
        @DisplayName("2자리 'US' — IllegalArgumentException")
        void twoChars_throwsIae() {
            assertThatThrownBy(() -> new CurrencyCode("US"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid ISO 4217 currency code");
        }

        @Test
        @DisplayName("4자리 'USDD' — IllegalArgumentException")
        void fourChars_throwsIae() {
            assertThatThrownBy(() -> new CurrencyCode("USDD"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid ISO 4217 currency code");
        }

        @Test
        @DisplayName("숫자 3자리 '123' — IllegalArgumentException")
        void digits_throwsIae() {
            assertThatThrownBy(() -> new CurrencyCode("123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid ISO 4217 currency code");
        }

        @Test
        @DisplayName("null 전달 시 IllegalArgumentException (CurrencyCode는 null 체크에 IAE 사용)")
        void null_throwsIae() {
            assertThatThrownBy(() -> new CurrencyCode(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CurrencyCode value must not be blank");
        }

        @Test
        @DisplayName("blank 전달 시 IllegalArgumentException")
        void blank_throwsIae() {
            assertThatThrownBy(() -> new CurrencyCode(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CurrencyCode value must not be blank");
        }

        @Test
        @DisplayName("of() — null 입력 시 null 반환")
        void of_null_returnsNull() {
            assertThat(CurrencyCode.of(null)).isNull();
        }

        @Test
        @DisplayName("of() — blank 입력 시 null 반환")
        void of_blank_returnsNull() {
            assertThat(CurrencyCode.of("  ")).isNull();
        }

        @Test
        @DisplayName("of() — 잘못된 형식 입력 시 IllegalArgumentException 전파")
        void of_invalidFormat_throwsIae() {
            assertThatThrownBy(() -> CurrencyCode.of("usd"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid ISO 4217 currency code");
        }
    }
}
