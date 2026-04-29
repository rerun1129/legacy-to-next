package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.PackageUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.enums.BlType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.ShipmentType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * E-08 House B/L 공통 본체.
 * PRD §2.1: "공통 본체 + 모드별 확장" 구조.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class HouseBl extends BaseEntity {

    // ── 식별 ──────────────────────────────────────────────────────
    private BlNumber hblNo;             // EXP: Auto on save / IMP: 필수 입력 (§S-02 PRD)
    private JobDiv jobDiv;
    private Bound bound;                // EXP / IMP

    // ── 상태 ──────────────────────────────────────────────────────
    private ShipmentType shipmentType;  // HOUSE / DIRECT
    private BlType blType;              // SEA 수출만 적용
    private FreightTerm freightTerm;

    // ── 당사자 (코드 참조) ─────────────────────────────────────────
    private CustomerCode shipperCode;
    private CustomerCode consigneeCode;
    private CustomerCode notifyCode;
    private CustomerCode docPartnerCode;   // SEA/AIR House 전용
    private CustomerCode settlePartnerCode;

    // ── 경로 ──────────────────────────────────────────────────────
    private PortCode polCode;           // Port of Loading (UNLOC)
    private PortCode podCode;           // Port of Discharge
    private PortCode deliveryCode;

    // ── 일정 ──────────────────────────────────────────────────────
    private BlDate etd;
    private BlDate eta;

    // ── 화물 요약 (비정규화 — 빠른 리스트 조회용) ────────────────────
    private Quantity pkgQty;
    private PackageUnit pkgUnit;
    private Weight grossWeightKg;
    private Volume cbm;

    // ── 영업·담당 ──────────────────────────────────────────────────
    private CustomerCode actualCustomerCode;
    private EmployeeCode operatorCode;
    private TeamCode teamCode;
    private EmployeeCode salesManCode;

    // ── Master B/L 연결 ─────────────────────────────────────────
    private Long masterBlId;

    // ── 컨테이너 (SEA/NON_BL 전용, 다른 모드에서는 빈 컬렉션) ─────────
    private List<HouseBlContainer> containers = new ArrayList<>();

    // ── 도메인 메서드 ────────────────────────────────────────────

    protected HouseBl(JobDiv jobDiv, Bound bound) {
        this.jobDiv = jobDiv;
        this.bound  = bound;
    }

    public void assignHblNo(BlNumber hblNo) {
        this.hblNo = hblNo;
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

    public void assignOperator(CustomerCode actualCustomerCode, EmployeeCode operatorCode,
                               TeamCode teamCode, EmployeeCode salesManCode) {
        this.actualCustomerCode = actualCustomerCode;
        this.operatorCode       = operatorCode;
        this.teamCode           = teamCode;
        this.salesManCode       = salesManCode;
    }

    public void linkToMaster(Long masterBlId) {
        this.masterBlId = masterBlId;
    }

    public void updateBlStatus(ShipmentType shipmentType, BlType blType, FreightTerm freightTerm) {
        this.shipmentType = shipmentType;
        this.blType       = blType;
        this.freightTerm  = freightTerm;
    }

    public void assignParties(CustomerCode shipperCode, CustomerCode consigneeCode, CustomerCode notifyCode,
                              CustomerCode docPartnerCode, PortCode deliveryCode) {
        this.shipperCode    = shipperCode;
        this.consigneeCode  = consigneeCode;
        this.notifyCode     = notifyCode;
        this.docPartnerCode = docPartnerCode;
        this.deliveryCode   = deliveryCode;
    }

    public void assignSettlePartner(CustomerCode settlePartnerCode) {
        this.settlePartnerCode = settlePartnerCode;
    }

    public void updateCargoSummary(CargoSummary cargo) {
        this.pkgQty        = cargo.packageCount();
        this.pkgUnit       = cargo.packageUnit();
        this.grossWeightKg = cargo.grossWeight();
        this.cbm           = cargo.volume();
    }

    public void initContainers(List<HouseBlContainer> containers) {
        this.containers = new ArrayList<>(containers);
    }
}
