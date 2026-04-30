package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.domain.common.vo.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-01 Master B/L 공통 본체.
 * House B/L 계층과 별개 엔티티 계층.
 * PRD §S-04: Party 3슬롯 (Shipper / Consignee / Notify, DOC Partner 없음).
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class MasterBl extends BaseEntity {

    private BlNumber mblNo;             // 해상 수출: 미입력 허용 / 기타: 필수
    private BlNumber masterRefNo;       // 해상 수출 자동 채번
    private MasterBlJobDiv jobDiv;
    private Bound bound;

    // ── 당사자 (3슬롯) ──────────────────────────────────────────
    private CustomerCode shipperCode;
    private CustomerCode consigneeCode;
    private CustomerCode notifyCode;

    // ── 경로 ──────────────────────────────────────────────────────
    private PortCode polCode;
    private PortCode podCode;

    // ── 일정 ──────────────────────────────────────────────────────
    private BlDate etd;
    private BlDate eta;

    // ── 운임 ──────────────────────────────────────────────────────
    private FreightTerm freightTerm;

    // ── 담당 ──────────────────────────────────────────────────────
    private EmployeeCode operatorCode;
    private TeamCode teamCode;

    // ── Cargo 요약 ─────────────────────────────────────────────
    private Quantity pkgQty;
    private WeightUnit pkgUnit;
    private Weight grossWeightKg;
    private Volume cbm;
    //TODO : HSCode를 추가하는 필드가 없음
    //TODO : Main Item Code를 추가하는 필드가 없음

    protected MasterBl(Bound bound) {
        this.bound = bound;
    }

    protected MasterBl(MasterBlJobDiv jobDiv, Bound bound) {
        this.jobDiv = jobDiv;
        this.bound  = bound;
    }

    public void assignMblNo(BlNumber mblNo, BlNumber masterRefNo) {
        this.mblNo       = mblNo;
        this.masterRefNo = masterRefNo;
    }

    public void assignParties(CustomerCode shipperCode, CustomerCode consigneeCode, CustomerCode notifyCode) {
        this.shipperCode   = shipperCode;
        this.consigneeCode = consigneeCode;
        this.notifyCode    = notifyCode;
    }

    public void updateSchedule(PortCode polCode, PortCode podCode, BlDate etd, BlDate eta) {
        if (etd != null && eta != null && !etd.isBeforeOrEqual(eta)) {
            throw new IllegalArgumentException("etd must be before or equal to eta");
        }
        this.polCode = polCode;
        this.podCode = podCode;
        this.etd     = etd;
        this.eta     = eta;
    }

    public void updateFreightAndOperator(FreightTerm freightTerm,
                                         EmployeeCode operatorCode, TeamCode teamCode) {
        this.freightTerm  = freightTerm;
        this.operatorCode = operatorCode;
        this.teamCode     = teamCode;
    }

    public void updateCargoSummary(CargoSummary cargo) {
        this.pkgQty        = cargo.packageCount();
        this.pkgUnit       = cargo.packageUnit();
        this.grossWeightKg = cargo.grossWeight();
        this.cbm           = cargo.volume();
    }
}
