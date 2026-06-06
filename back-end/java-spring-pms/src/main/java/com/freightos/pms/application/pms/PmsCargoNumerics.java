package com.freightos.pms.application.pms;

import com.freightos.pms.application.pms.projection.PmsCargoRow;

import java.math.BigDecimal;

/**
 * cargo 수치 파생 순수 계산 헬퍼. I/O 없음.
 *
 * R/Ton 파생 규칙 (01_PMS_DOMAIN §3.3):
 *   SEA  = max(gross_weight_kg / 1000 (kg→MT), cbm) — house_bl_sea.rton은 사용 안 함
 *   NON_BL = house_bl_non_bl.rton (저장값 그대로)
 *   AIR, TRUCK = null (공란)
 */
public final class PmsCargoNumerics {

    private PmsCargoNumerics() {}

    /** job_div 기반 load_type 결정. SEA→seaLoadType, TRUCK→truckLoadType, 그 외 null. */
    public static String deriveLoadType(String jobDiv, PmsCargoRow cargo) {
        if (cargo == null) return null;
        if ("SEA".equals(jobDiv)) return cargo.seaLoadType();
        if ("TRUCK".equals(jobDiv)) return cargo.truckLoadType();
        return null;
    }

    /** job_div 기반 charge_weight_kg 결정. AIR·TRUCK만 값, 그 외 null. */
    public static BigDecimal deriveChargeWeightKg(String jobDiv, PmsCargoRow cargo) {
        if (cargo == null) return null;
        if ("AIR".equals(jobDiv)) return cargo.airChargeWeightKg();
        if ("TRUCK".equals(jobDiv)) return cargo.truckChargeWeightKg();
        return null;
    }

    /**
     * R/Ton 파생.
     * SEA: max(grossWeightKg/1000, cbm). 둘 다 null이면 null.
     * NON_BL: nonBlRton 저장값.
     * AIR, TRUCK: null.
     */
    public static BigDecimal deriveRton(String jobDiv, PmsCargoRow cargo) {
        if (cargo == null) return null;
        return switch (jobDiv != null ? jobDiv : "") {
            case "SEA" -> deriveSeaRton(cargo.grossWeightKg(), cargo.cbm());
            case "NON_BL" -> cargo.nonBlRton();
            default -> null;
        };
    }

    /**
     * SEA R/Ton: max(grossWeightKg kg→MT, cbm).
     * null 값은 0으로 취급(비교에서 제외하는 것이 아닌, 0이 더 작으므로 상대 값 선택됨).
     * 둘 다 null이면 null 반환.
     */
    private static BigDecimal deriveSeaRton(BigDecimal grossWeightKg, BigDecimal cbm) {
        if (grossWeightKg == null && cbm == null) return null;
        BigDecimal wt = grossWeightKg != null
            ? grossWeightKg.divide(BigDecimal.valueOf(1000), 4, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        BigDecimal cb = cbm != null ? cbm : BigDecimal.ZERO;
        return wt.compareTo(cb) >= 0 ? wt : cb;
    }

    /**
     * Profit = (Invoice+Debit) - (Payment+Credit).
     * null 금액은 ZERO로 처리.
     */
    public static BigDecimal deriveProfit(
            BigDecimal invoiceAmt,
            BigDecimal debitAmt,
            BigDecimal paymentAmt,
            BigDecimal creditAmt) {
        BigDecimal revenue = nvl(invoiceAmt).add(nvl(debitAmt));
        BigDecimal cost = nvl(paymentAmt).add(nvl(creditAmt));
        return revenue.subtract(cost);
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
