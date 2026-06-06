package com.freightos.pms.application.pms;

import com.freightos.pms.application.pms.projection.PmsCargoRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PmsCargoNumerics 순수 계산 단위 테스트.
 * I/O 없음 — 결정적, 비결정적 요소 없음.
 */
class PmsCargoNumericsTest {

    // ── deriveRton ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("SEA R/Ton: gross_weight_kg 1000kg → MT 1.0 > cbm 0.5 이면 MT 선택")
    void seaRton_weightDominates() {
        PmsCargoRow cargo = cargoWith(new BigDecimal("1000"), new BigDecimal("0.5"), null, null, null, null, null, null, null);
        BigDecimal rton = PmsCargoNumerics.deriveRton("SEA", cargo);
        assertThat(rton).isEqualByComparingTo(new BigDecimal("1.0000"));
    }

    @Test
    @DisplayName("SEA R/Ton: cbm 2.5 > gross_weight_kg 500kg → MT 0.5 이면 cbm 선택")
    void seaRton_cbmDominates() {
        PmsCargoRow cargo = cargoWith(new BigDecimal("500"), new BigDecimal("2.5"), null, null, null, null, null, null, null);
        BigDecimal rton = PmsCargoNumerics.deriveRton("SEA", cargo);
        assertThat(rton).isEqualByComparingTo(new BigDecimal("2.5"));
    }

    @Test
    @DisplayName("SEA R/Ton: 둘 다 null이면 null 반환")
    void seaRton_bothNull() {
        PmsCargoRow cargo = cargoWith(null, null, null, null, null, null, null, null, null);
        BigDecimal rton = PmsCargoNumerics.deriveRton("SEA", cargo);
        assertThat(rton).isNull();
    }

    @Test
    @DisplayName("SEA R/Ton: cbm null이면 grossWeightKg/1000으로만 계산")
    void seaRton_cbmNull() {
        PmsCargoRow cargo = cargoWith(new BigDecimal("3000"), null, null, null, null, null, null, null, null);
        BigDecimal rton = PmsCargoNumerics.deriveRton("SEA", cargo);
        assertThat(rton).isEqualByComparingTo(new BigDecimal("3.0000"));
    }

    @Test
    @DisplayName("NON_BL R/Ton: house_bl_non_bl.rton 저장값 반환")
    void nonBlRton_storedValue() {
        PmsCargoRow cargo = cargoWith(null, null, null, null, null, null, null, null, new BigDecimal("5.25"));
        BigDecimal rton = PmsCargoNumerics.deriveRton("NON_BL", cargo);
        assertThat(rton).isEqualByComparingTo(new BigDecimal("5.25"));
    }

    @Test
    @DisplayName("AIR R/Ton: null 반환")
    void airRton_null() {
        PmsCargoRow cargo = cargoWith(null, null, null, new BigDecimal("100"), null, null, null, null, null);
        BigDecimal rton = PmsCargoNumerics.deriveRton("AIR", cargo);
        assertThat(rton).isNull();
    }

    @Test
    @DisplayName("TRUCK R/Ton: null 반환")
    void truckRton_null() {
        PmsCargoRow cargo = cargoWith(null, null, null, null, new BigDecimal("200"), null, null, null, null);
        BigDecimal rton = PmsCargoNumerics.deriveRton("TRUCK", cargo);
        assertThat(rton).isNull();
    }

    // ── deriveProfit ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Profit: (Invoice+Debit) - (Payment+Credit)")
    void profit_basic() {
        BigDecimal profit = PmsCargoNumerics.deriveProfit(
            new BigDecimal("1000"), new BigDecimal("200"),
            new BigDecimal("500"), new BigDecimal("100")
        );
        assertThat(profit).isEqualByComparingTo(new BigDecimal("600"));
    }

    @Test
    @DisplayName("Profit: null 금액은 ZERO 처리")
    void profit_nullsAsZero() {
        BigDecimal profit = PmsCargoNumerics.deriveProfit(
            new BigDecimal("1000"), null, null, null
        );
        assertThat(profit).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    @DisplayName("Profit: 손실(음수)도 정상 반환")
    void profit_negative() {
        BigDecimal profit = PmsCargoNumerics.deriveProfit(
            new BigDecimal("100"), null,
            new BigDecimal("500"), null
        );
        assertThat(profit).isEqualByComparingTo(new BigDecimal("-400"));
    }

    // ── deriveLoadType / deriveChargeWeightKg ──────────────────────────────────

    @Test
    @DisplayName("loadType: SEA → seaLoadType 반환")
    void loadType_sea() {
        PmsCargoRow cargo = cargoWith(null, null, "FCL", null, null, null, null, null, null);
        assertThat(PmsCargoNumerics.deriveLoadType("SEA", cargo)).isEqualTo("FCL");
    }

    @Test
    @DisplayName("loadType: AIR → null 반환")
    void loadType_air() {
        PmsCargoRow cargo = cargoWith(null, null, null, null, null, null, null, null, null);
        assertThat(PmsCargoNumerics.deriveLoadType("AIR", cargo)).isNull();
    }

    @Test
    @DisplayName("chargeWeightKg: AIR → airChargeWeightKg 반환")
    void chargeWeight_air() {
        PmsCargoRow cargo = cargoWith(null, null, null, new BigDecimal("350"), null, null, null, null, null);
        assertThat(PmsCargoNumerics.deriveChargeWeightKg("AIR", cargo))
            .isEqualByComparingTo(new BigDecimal("350"));
    }

    @Test
    @DisplayName("chargeWeightKg: SEA → null 반환")
    void chargeWeight_sea() {
        PmsCargoRow cargo = cargoWith(null, null, null, new BigDecimal("999"), null, null, null, null, null);
        assertThat(PmsCargoNumerics.deriveChargeWeightKg("SEA", cargo)).isNull();
    }

    // ── 헬퍼 ──────────────────────────────────────────────────────────────────────

    private PmsCargoRow cargoWith(
            BigDecimal grossWeightKg, BigDecimal cbm,
            String seaLoadType, BigDecimal airChargeWeightKg,
            BigDecimal truckChargeWeightKg, String truckLoadType,
            Integer pkgQty, Object unused, BigDecimal nonBlRton) {
        return new PmsCargoRow(
            1L, pkgQty, cbm, grossWeightKg,
            seaLoadType, airChargeWeightKg,
            truckChargeWeightKg, truckLoadType,
            nonBlRton
        );
    }
}
