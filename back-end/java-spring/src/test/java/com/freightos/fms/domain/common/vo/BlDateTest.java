package com.freightos.fms.domain.common.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BlDate 단위 테스트")
class BlDateTest {

    @Test
    @DisplayName("of('20260101') 정상 파싱 — value()로 LocalDate 확인")
    void of_validDate_parsesCorrectly() {
        BlDate blDate = BlDate.of("20260101");
        assertThat(blDate.value()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("of() 정상 파싱 후 asString() 반환 형식 확인")
    void of_validDate_asStringRoundTrips() {
        BlDate blDate = BlDate.of("20261231");
        assertThat(blDate.asString()).isEqualTo("20261231");
    }

    @Test
    @DisplayName("of('invalid') — IllegalArgumentException")
    void of_invalidString_throwsIae() {
        assertThatThrownBy(() -> BlDate.of("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date format (expected yyyyMMdd)");
    }

    @Test
    @DisplayName("of('2026-01-01') 잘못된 구분자 형식 — IllegalArgumentException")
    void of_hyphenFormat_throwsIae() {
        assertThatThrownBy(() -> BlDate.of("2026-01-01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date format (expected yyyyMMdd)");
    }

    @Test
    @DisplayName("of('20261399') 존재하지 않는 날짜 — IllegalArgumentException")
    void of_nonExistentDate_throwsIae() {
        assertThatThrownBy(() -> BlDate.of("20261399"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date format (expected yyyyMMdd)");
    }

    @Test
    @DisplayName("new BlDate(null) — NullPointerException (compact constructor에서 requireNonNull)")
    void constructor_null_throwsNpe() {
        assertThatThrownBy(() -> new BlDate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("BlDate value must not be null");
    }

    @Test
    @DisplayName("of(null) — null 반환 (팩토리 메서드는 null 허용)")
    void of_null_returnsNull() {
        assertThat(BlDate.of(null)).isNull();
    }

    @Test
    @DisplayName("of(blank) — null 반환")
    void of_blank_returnsNull() {
        assertThat(BlDate.of("  ")).isNull();
    }

    // ────────────────────────────────────────────────
    // isBeforeOrEqual
    // ────────────────────────────────────────────────

    @Test
    @DisplayName("isBeforeOrEqual — A < B 이면 true")
    void isBeforeOrEqual_aBeforeB_returnsTrue() {
        BlDate a = BlDate.of("20260101");
        BlDate b = BlDate.of("20260201");
        assertThat(a.isBeforeOrEqual(b)).isTrue();
    }

    @Test
    @DisplayName("isBeforeOrEqual — A == B 이면 true")
    void isBeforeOrEqual_equal_returnsTrue() {
        BlDate a = BlDate.of("20260101");
        BlDate b = BlDate.of("20260101");
        assertThat(a.isBeforeOrEqual(b)).isTrue();
    }

    @Test
    @DisplayName("isBeforeOrEqual — A > B 이면 false")
    void isBeforeOrEqual_aAfterB_returnsFalse() {
        BlDate a = BlDate.of("20260201");
        BlDate b = BlDate.of("20260101");
        assertThat(a.isBeforeOrEqual(b)).isFalse();
    }

    @Test
    @DisplayName("isBeforeOrEqual — null 파라미터 전달 시 NullPointerException")
    void isBeforeOrEqual_nullOther_throwsNpe() {
        BlDate a = BlDate.of("20260101");
        assertThatThrownBy(() -> a.isBeforeOrEqual(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ────────────────────────────────────────────────
    // isAfter
    // ────────────────────────────────────────────────

    @Test
    @DisplayName("isAfter — A > B 이면 true")
    void isAfter_aAfterB_returnsTrue() {
        BlDate a = BlDate.of("20260201");
        BlDate b = BlDate.of("20260101");
        assertThat(a.isAfter(b)).isTrue();
    }

    @Test
    @DisplayName("isAfter — A < B 이면 false")
    void isAfter_aBeforeB_returnsFalse() {
        BlDate a = BlDate.of("20260101");
        BlDate b = BlDate.of("20260201");
        assertThat(a.isAfter(b)).isFalse();
    }

    @Test
    @DisplayName("isAfter — A == B 이면 false")
    void isAfter_equal_returnsFalse() {
        BlDate a = BlDate.of("20260101");
        BlDate b = BlDate.of("20260101");
        assertThat(a.isAfter(b)).isFalse();
    }

    @Test
    @DisplayName("isAfter — null 파라미터 전달 시 NullPointerException")
    void isAfter_nullOther_throwsNpe() {
        BlDate a = BlDate.of("20260101");
        assertThatThrownBy(() -> a.isAfter(null))
                .isInstanceOf(NullPointerException.class);
    }
}
