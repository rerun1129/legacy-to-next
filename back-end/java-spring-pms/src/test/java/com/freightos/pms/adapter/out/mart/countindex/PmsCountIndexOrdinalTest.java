package com.freightos.pms.adapter.out.mart.countindex;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 결정적 ordinal 산식 및 overflow 가드 단위 테스트.
 * Redis/Mongo 없이 순수 산식만 검증한다.
 */
class PmsCountIndexOrdinalTest {

    // ── ordinal 산식 ─────────────────────────────────────────────────────────

    @Test
    void HOUSE_blId_0은_ordinal_0이다() {
        assertThat(PmsCountIndexKeys.toOrdinal(0L, "HOUSE")).isEqualTo(0);
    }

    @Test
    void MASTER_blId_0은_ordinal_1이다() {
        assertThat(PmsCountIndexKeys.toOrdinal(0L, "MASTER")).isEqualTo(1);
    }

    @Test
    void HOUSE_blId_1은_ordinal_2이다() {
        assertThat(PmsCountIndexKeys.toOrdinal(1L, "HOUSE")).isEqualTo(2);
    }

    @Test
    void MASTER_blId_1은_ordinal_3이다() {
        assertThat(PmsCountIndexKeys.toOrdinal(1L, "MASTER")).isEqualTo(3);
    }

    @Test
    void HOUSE와_MASTER_ordinal은_절대로_충돌하지않는다() {
        // HOUSE: 짝수, MASTER: 홀수 — 교차점 없음
        for (long id = 0; id <= 100; id++) {
            int h = PmsCountIndexKeys.toOrdinal(id, "HOUSE");
            int m = PmsCountIndexKeys.toOrdinal(id, "MASTER");
            assertThat(h).isNotEqualTo(m);
            assertThat(h % 2).as("HOUSE ordinal은 짝수여야 한다 id=%d", id).isEqualTo(0);
            assertThat(m % 2).as("MASTER ordinal은 홀수여야 한다 id=%d", id).isEqualTo(1);
        }
    }

    @Test
    void 상한_blId의_ordinal은_Integer_MAX_VALUE이하이다() {
        long maxBlId = PmsCountIndexKeys.ORDINAL_MAX_BL_ID;
        int ordinal = PmsCountIndexKeys.toOrdinal(maxBlId, "MASTER");
        assertThat(ordinal).isLessThanOrEqualTo(Integer.MAX_VALUE);
        assertThat(ordinal).isGreaterThanOrEqualTo(0);
    }

    // ── overflow 가드 ─────────────────────────────────────────────────────────

    @Test
    void null은_overflow이다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(null)).isTrue();
    }

    @Test
    void 음수는_overflow이다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(-1L)).isTrue();
        assertThat(PmsCountIndexKeys.isBlIdOverflow(Long.MIN_VALUE)).isTrue();
    }

    @Test
    void 상한_초과는_overflow이다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(PmsCountIndexKeys.ORDINAL_MAX_BL_ID + 1)).isTrue();
    }

    @Test
    void 상한_자체는_overflow아니다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(PmsCountIndexKeys.ORDINAL_MAX_BL_ID)).isFalse();
    }

    @Test
    void blId_0은_overflow아니다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(0L)).isFalse();
    }

    @Test
    void 일반값은_overflow아니다() {
        assertThat(PmsCountIndexKeys.isBlIdOverflow(1_000_000L)).isFalse();
        assertThat(PmsCountIndexKeys.isBlIdOverflow(2_000_000L)).isFalse();
    }
}
