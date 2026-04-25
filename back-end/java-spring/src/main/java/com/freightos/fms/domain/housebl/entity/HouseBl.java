package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.housebl.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * E-08 House B/L 공통 본체.
 * PRD §2.1: "공통 본체 + 모드별 확장" 구조.
 * JPA JOINED 전략: job_div 값에 따라 확장 테이블이 결정된다.
 *   SEA   → house_bl_sea  (E-10)
 *   AIR   → house_bl_air  (E-11)
 *   TRUCK → house_bl_truck (E-20)
 *   NON_BL→ house_bl_non_bl (E-24)
 */
@Entity
@Table(name = "house_bl")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "job_div", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class HouseBl extends BaseEntity {

    // ── 식별 ──────────────────────────────────────────────────────
    @Column(name = "hbl_no", length = 50)
    private String hblNo;              // EXP: Auto on save / IMP: 필수 입력 (§S-02 PRD)

    @Column(name = "job_div", insertable = false, updatable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private JobDiv jobDiv;

    @Column(name = "bound", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Bound bound;               // EXP / IMP

    // ── 상태 ──────────────────────────────────────────────────────
    @Column(name = "shipment_type", length = 10)
    @Enumerated(EnumType.STRING)
    private ShipmentType shipmentType; // HOUSE / DIRECT

    @Column(name = "bl_type", length = 15)
    @Enumerated(EnumType.STRING)
    private BlType blType;             // SEA 수출만 적용

    @Column(name = "freight_term", length = 10)
    @Enumerated(EnumType.STRING)
    private FreightTerm freightTerm;

    // ── 당사자 (코드 참조) ─────────────────────────────────────────
    /** Shipper 거래처 코드. 거래처 마스터 참조 */
    @Column(name = "shipper_code", length = 20)
    private String shipperCode;

    @Column(name = "consignee_code", length = 20)
    private String consigneeCode;

    @Column(name = "notify_code", length = 20)
    private String notifyCode;

    @Column(name = "doc_partner_code", length = 20)
    private String docPartnerCode;     // SEA/AIR House 전용

    // ── 경로 ──────────────────────────────────────────────────────
    @Column(name = "pol_code", length = 10)
    private String polCode;            // Port of Loading (UNLOC)

    @Column(name = "pod_code", length = 10)
    private String podCode;            // Port of Discharge

    @Column(name = "delivery_code", length = 10)
    private String deliveryCode;

    // ── 일정 ──────────────────────────────────────────────────────
    @Column(name = "etd")
    private LocalDate etd;

    @Column(name = "eta")
    private LocalDate eta;

    // ── 화물 요약 (비정규화 — 빠른 리스트 조회용) ────────────────────
    @Column(name = "pkg_qty")
    private Integer pkgQty;

    @Column(name = "pkg_unit", length = 10)
    private String pkgUnit;

    @Column(name = "gross_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private java.math.BigDecimal grossWeightKg;

    @Column(name = "cbm", columnDefinition = "NUMERIC(10,3)")
    private java.math.BigDecimal cbm;

    // ── 영업·담당 ──────────────────────────────────────────────────
    @Column(name = "actual_customer_code", length = 20)
    private String actualCustomerCode;

    @Column(name = "operator_code", length = 20)
    private String operatorCode;

    @Column(name = "team_code", length = 20)
    private String teamCode;

    @Column(name = "sales_man_code", length = 20)
    private String salesManCode;

    // ── Master B/L 연결 ─────────────────────────────────────────
    @Column(name = "master_bl_id")
    private java.util.UUID masterBlId;

    // ── 컨테이너 (SEA 전용, 다른 모드에서는 빈 컬렉션) ──────────────
    @OneToMany(mappedBy = "houseBl", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HouseBlContainer> containers = new ArrayList<>();

    // ── 도메인 메서드 ────────────────────────────────────────────

    protected HouseBl(JobDiv jobDiv, Bound bound) {
        this.jobDiv = jobDiv;
        this.bound  = bound;
    }

    public void assignHblNo(String hblNo) {
        this.hblNo = hblNo;
    }

    public void updateSchedule(String polCode, String podCode, LocalDate etd, LocalDate eta) {
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

    public void linkToMaster(java.util.UUID masterBlId) {
        this.masterBlId = masterBlId;
    }
}
