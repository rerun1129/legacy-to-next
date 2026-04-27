package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * E-01 Master B/L 공통 본체.
 * House B/L 계층과 별개 엔티티 계층.
 * PRD §S-04: Party 3슬롯 (Shipper / Consignee / Notify, DOC Partner 없음).
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MasterBl extends BaseEntity {

    private String mblNo;              // 해상 수출: 미입력 허용 / 기타: 필수
    private String masterRefNo;        // 해상 수출 자동 채번
    private String jobDiv;
    private Bound bound;

    // ── 당사자 (3슬롯) ──────────────────────────────────────────
    private String shipperCode;
    private String consigneeCode;
    private String notifyCode;

    // ── 경로 ──────────────────────────────────────────────────────
    private String polCode;
    private String podCode;

    // ── 일정 ──────────────────────────────────────────────────────
    private LocalDate etd;
    private LocalDate eta;

    // ── 운임 ──────────────────────────────────────────────────────
    private FreightTerm freightTerm;

    // ── 담당 ──────────────────────────────────────────────────────
    private String operatorCode;
    private String teamCode;

    // ── Cargo 요약 ─────────────────────────────────────────────
    private Integer pkgQty;
    private String pkgUnit;
    private BigDecimal grossWeightKg;
    private BigDecimal cbm;

    protected MasterBl(Bound bound) {
        this.bound = bound;
    }

    public void assignMblNo(String mblNo, String masterRefNo) {
        this.mblNo        = mblNo;
        this.masterRefNo  = masterRefNo;
    }

    public void assignParties(String shipperCode, String consigneeCode, String notifyCode) {
        this.shipperCode   = shipperCode;
        this.consigneeCode = consigneeCode;
        this.notifyCode    = notifyCode;
    }

    public void updateSchedule(String polCode, String podCode, LocalDate etd, LocalDate eta) {
        this.polCode = polCode;
        this.podCode = podCode;
        this.etd     = etd;
        this.eta     = eta;
    }

    public void updateFreightAndOperator(FreightTerm freightTerm,
                                         String operatorCode, String teamCode) {
        this.freightTerm  = freightTerm;
        this.operatorCode = operatorCode;
        this.teamCode     = teamCode;
    }

    public void updateCargoSummary(Integer pkgQty, String pkgUnit,
                                   java.math.BigDecimal grossWeightKg, java.math.BigDecimal cbm) {
        this.pkgQty        = pkgQty;
        this.pkgUnit       = pkgUnit;
        this.grossWeightKg = grossWeightKg;
        this.cbm           = cbm;
    }
}
