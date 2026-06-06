package com.freightos.pms.application.pms.projection;

import java.math.BigDecimal;

/**
 * House B/L 키 조회 결과 한 행. house_bl_id 기준 keyed 조회.
 * cargo 수치(pkgQty/cbm/grossWeightKg 등)와 house B/L 식별 정보를 함께 보관.
 * 식별 필드는 Phase-2 keyed lookup에서 채워지며, 집계 쿼리에서는 사용되지 않음.
 */
public record PmsCargoRow(
    Long houseBlId,
    Integer pkgQty,
    BigDecimal cbm,
    BigDecimal grossWeightKg,
    // SEA 전용
    String seaLoadType,
    // AIR 전용
    BigDecimal airChargeWeightKg,
    // TRUCK 전용
    BigDecimal truckChargeWeightKg,
    String truckLoadType,
    // NON_BL 전용
    BigDecimal nonBlRton,
    // 식별 정보 (Phase-2 keyed lookup에서 채움)
    String hblNo,
    String mblNo,
    String jobDiv,
    String bound,
    String etd,
    String eta,
    String polCode,
    String podCode,
    String salesManCode,
    String incoterms,
    String teamCode
) {
    /**
     * cargo 수치만으로 생성하는 편의 생성자(기존 positional 호출부 호환).
     * 식별 필드는 null로 초기화 — withIdentity()로 보완.
     */
    public PmsCargoRow(
            Long houseBlId,
            Integer pkgQty,
            BigDecimal cbm,
            BigDecimal grossWeightKg,
            String seaLoadType,
            BigDecimal airChargeWeightKg,
            BigDecimal truckChargeWeightKg,
            String truckLoadType,
            BigDecimal nonBlRton) {
        this(houseBlId, pkgQty, cbm, grossWeightKg,
             seaLoadType, airChargeWeightKg,
             truckChargeWeightKg, truckLoadType,
             nonBlRton,
             null, null, null, null, null, null, null, null, null, null, null);
    }

    /** 식별 필드를 채운 새 인스턴스를 반환. cargo 수치는 보존. */
    public PmsCargoRow withIdentity(
            String hblNo, String mblNo, String jobDiv, String bound,
            String etd, String eta, String polCode, String podCode,
            String salesManCode, String incoterms, String teamCode) {
        return new PmsCargoRow(
            this.houseBlId, this.pkgQty, this.cbm, this.grossWeightKg,
            this.seaLoadType, this.airChargeWeightKg,
            this.truckChargeWeightKg, this.truckLoadType,
            this.nonBlRton,
            hblNo, mblNo, jobDiv, bound, etd, eta, polCode, podCode,
            salesManCode, incoterms, teamCode
        );
    }
}
