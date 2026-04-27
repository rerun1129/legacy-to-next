package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.housebl.enums.BlType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.ShipmentType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
    private String hblNo;              // EXP: Auto on save / IMP: 필수 입력 (§S-02 PRD)
    private JobDiv jobDiv;
    private Bound bound;               // EXP / IMP

    // ── 상태 ──────────────────────────────────────────────────────
    private ShipmentType shipmentType; // HOUSE / DIRECT
    private BlType blType;             // SEA 수출만 적용
    private FreightTerm freightTerm;

    // ── 당사자 (코드 참조) ─────────────────────────────────────────
    private String shipperCode;
    private String consigneeCode;
    private String notifyCode;
    private String docPartnerCode;     // SEA/AIR House 전용

    // ── 경로 ──────────────────────────────────────────────────────
    private String polCode;            // Port of Loading (UNLOC)
    private String podCode;            // Port of Discharge
    private String deliveryCode;

    // ── 일정 ──────────────────────────────────────────────────────
    private String etd;
    private String eta;

    // ── 화물 요약 (비정규화 — 빠른 리스트 조회용) ────────────────────
    private Integer pkgQty;
    private String pkgUnit;
    private BigDecimal grossWeightKg;
    private BigDecimal cbm;

    // ── 영업·담당 ──────────────────────────────────────────────────
    private String actualCustomerCode;
    private String operatorCode;
    private String teamCode;
    private String salesManCode;

    // ── Master B/L 연결 ─────────────────────────────────────────
    private Long masterBlId;

    // ── 컨테이너 (SEA 전용, 다른 모드에서는 빈 컬렉션) ──────────────
    private List<HouseBlContainer> containers = new ArrayList<>();

    // ── 도메인 메서드 ────────────────────────────────────────────

    protected HouseBl(JobDiv jobDiv, Bound bound) {
        this.jobDiv = jobDiv;
        this.bound  = bound;
    }

    public void assignHblNo(String hblNo) {
        this.hblNo = hblNo;
    }

    public void updateSchedule(String polCode, String podCode, String etd, String eta) {
        this.polCode = polCode;
        this.podCode = podCode;
        this.etd     = etd;
        this.eta     = eta;
    }

    public void assignOperator(String actualCustomerCode, String operatorCode,
                               String teamCode, String salesManCode) {
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

    public void assignParties(String shipperCode, String consigneeCode, String notifyCode,
                              String docPartnerCode, String deliveryCode) {
        this.shipperCode     = shipperCode;
        this.consigneeCode   = consigneeCode;
        this.notifyCode      = notifyCode;
        this.docPartnerCode  = docPartnerCode;
        this.deliveryCode    = deliveryCode;
    }

    public void updateCargoSummary(Integer pkgQty, String pkgUnit,
                                   java.math.BigDecimal grossWeightKg, java.math.BigDecimal cbm) {
        this.pkgQty        = pkgQty;
        this.pkgUnit       = pkgUnit;
        this.grossWeightKg = grossWeightKg;
        this.cbm           = cbm;
    }

    public void initContainers(List<HouseBlContainer> containers) {
        this.containers = new ArrayList<>(containers);
    }
}
