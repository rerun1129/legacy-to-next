package com.freightos.fms.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("수치형 VO 단위 테스트")
class NumericVoTest {

    // ────────────────────────────────────────────────
    // Weight
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("Weight")
    class WeightTests {

        @Test
        @DisplayName("양수 kg 정상 생성")
        void positive_kg_valid() {
            Weight weight = new Weight(new BigDecimal("100.5"));
            assertThat(weight.kg()).isEqualByComparingTo("100.5");
        }

        @Test
        @DisplayName("0 kg 경계값 정상 생성")
        void zero_kg_valid() {
            Weight weight = new Weight(BigDecimal.ZERO);
            assertThat(weight.kg()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("음수 kg 전달 시 IllegalArgumentException")
        void negative_kg_throwsIae() {
            assertThatThrownBy(() -> new Weight(new BigDecimal("-0.1")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Weight kg must be >= 0");
        }

        @Test
        @DisplayName("add() — 두 Weight 합산")
        void add_returnsSum() {
            Weight w1 = new Weight(new BigDecimal("10.0"));
            Weight w2 = new Weight(new BigDecimal("5.5"));
            Weight sum = w1.add(w2);
            assertThat(sum.kg()).isEqualByComparingTo("15.5");
        }

        @Test
        @DisplayName("add() — other가 null이면 자기 자신 반환")
        void add_nullOther_returnsSelf() {
            Weight w = new Weight(new BigDecimal("7.0"));
            Weight result = w.add(null);
            assertThat(result).isSameAs(w);
        }

        @Test
        @DisplayName("isZero() — 0 kg이면 true")
        void isZero_zero_returnsTrue() {
            Weight weight = new Weight(BigDecimal.ZERO);
            assertThat(weight.isZero()).isTrue();
        }

        @Test
        @DisplayName("isZero() — 양수이면 false")
        void isZero_positive_returnsFalse() {
            Weight weight = new Weight(new BigDecimal("1.0"));
            assertThat(weight.isZero()).isFalse();
        }

        @Test
        @DisplayName("of() — null 입력 시 null 반환")
        void of_null_returnsNull() {
            assertThat(Weight.of(null)).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // Volume
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("Volume")
    class VolumeTests {

        @Test
        @DisplayName("양수 cbm 정상 생성")
        void positive_cbm_valid() {
            Volume volume = new Volume(new BigDecimal("2.5"));
            assertThat(volume.cbm()).isEqualByComparingTo("2.5");
        }

        @Test
        @DisplayName("0 cbm 경계값 정상 생성")
        void zero_cbm_valid() {
            Volume volume = new Volume(BigDecimal.ZERO);
            assertThat(volume.cbm()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("음수 cbm 전달 시 IllegalArgumentException")
        void negative_cbm_throwsIae() {
            assertThatThrownBy(() -> new Volume(new BigDecimal("-1.0")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Volume cbm must be >= 0");
        }

        @Test
        @DisplayName("of() — null 입력 시 null 반환")
        void of_null_returnsNull() {
            assertThat(Volume.of(null)).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // Quantity
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("Quantity")
    class QuantityTests {

        @Test
        @DisplayName("양수 count 정상 생성")
        void positive_count_valid() {
            Quantity quantity = new Quantity(10);
            assertThat(quantity.count()).isEqualTo(10);
        }

        @Test
        @DisplayName("0 count 경계값 정상 생성")
        void zero_count_valid() {
            Quantity quantity = new Quantity(0);
            assertThat(quantity.count()).isEqualTo(0);
        }

        @Test
        @DisplayName("음수 count 전달 시 IllegalArgumentException")
        void negative_count_throwsIae() {
            assertThatThrownBy(() -> new Quantity(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity count must be >= 0");
        }

        @Test
        @DisplayName("of() — null 입력 시 null 반환")
        void of_null_returnsNull() {
            assertThat(Quantity.of(null)).isNull();
        }
    }

    // ────────────────────────────────────────────────
    // Rton
    // ────────────────────────────────────────────────
    @Nested
    @DisplayName("Rton")
    class RtonTests {

        @Test
        @DisplayName("양수 ton 정상 생성")
        void positive_ton_valid() {
            Rton rton = new Rton(new BigDecimal("3.14"));
            assertThat(rton.ton()).isEqualByComparingTo("3.14");
        }

        @Test
        @DisplayName("0 ton 경계값 정상 생성")
        void zero_ton_valid() {
            Rton rton = new Rton(BigDecimal.ZERO);
            assertThat(rton.ton()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("음수 ton 전달 시 IllegalArgumentException")
        void negative_ton_throwsIae() {
            assertThatThrownBy(() -> new Rton(new BigDecimal("-0.01")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rton must be >= 0");
        }

        @Test
        @DisplayName("of() — null 입력 시 null 반환")
        void of_null_returnsNull() {
            assertThat(Rton.of(null)).isNull();
        }
    }
}
