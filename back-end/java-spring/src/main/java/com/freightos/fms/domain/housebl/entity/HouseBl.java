package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.Incoterms;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.SalesClass;
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
    private WeightUnit pkgUnit;
    private Weight grossWeightKg;
    private Volume cbm;

    // ── 영업·담당 ──────────────────────────────────────────────────
    private CustomerCode actualCustomerCode;
    private EmployeeCode operatorCode;
    private TeamCode teamCode;
    private EmployeeCode salesManCode;

    // ── Master B/L 연결 ─────────────────────────────────────────
    private Long masterBlId;
    private MblNo mblNo;
    private String masterRefNo;

    // ── 거래 조건 ─────────────────────────────────────────────────
    private Incoterms incoterms;
    private SalesClass salesClass;
    private String mainItemName;
    private String hsCode;

    // ── 컨테이너 (SEA/NON_BL에서 채워짐, AIR/TRUCK은 빈 컬렉션이 정상.
    //    본체 위치는 @OneToMany mappedBy 정합성 확보를 위한 설계 결정) ───
    private List<HouseBlContainer> containers = new ArrayList<>();

    // ── Dim (AIR/NON_BL/TRUCK에서 채워짐, SEA는 빈 컬렉션이 정상.
    //    본체 위치는 @OneToMany mappedBy 정합성 확보를 위한 설계 결정) ───
    private List<HouseBlDim> dims = new ArrayList<>();

    // ── ScheduleLeg (AIR에서 채워짐, 다른 모드는 빈 컬렉션이 정상.
    //    본체 위치는 @OneToMany mappedBy 정합성 + 구조 일관성 확보) ───
    private List<HouseBlScheduleLeg> scheduleLegs = new ArrayList<>();

    // ── License (AIR/SEA에서 채워짐, 다른 모드는 빈 컬렉션이 정상.
    //    본체 위치는 @OneToMany mappedBy 정합성 + 구조 일관성 확보) ───
    private List<HouseBlLicense> licenses = new ArrayList<>();

    // ── TruckOrder (TRUCK에서만 채워짐, 다른 모드는 빈 컬렉션이 정상.
    //    본체 위치는 @OneToMany mappedBy 정합성 + 구조 일관성 확보) ───
    private List<HouseBlTruckOrder> truckOrders = new ArrayList<>();

    // ── Desc (AIR/SEA/NON_BL에서 채워짐, TRUCK은 null이 정상.
    //    본체 위치는 @OneToOne mappedBy 정합성 + 구조 일관성 확보) ───
    private HouseBlDesc desc;

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

    public void assignMasterReference(MblNo mblNo, String masterRefNo) {
        this.mblNo = mblNo;
        this.masterRefNo = masterRefNo;
    }

    public void updateTradeInfo(Incoterms incoterms, SalesClass salesClass, String mainItemName, String hsCode) {
        this.incoterms    = incoterms;
        this.salesClass   = salesClass;
        this.mainItemName = mainItemName;
        this.hsCode       = hsCode;
    }

    public void updateBlStatus(ShipmentType shipmentType, FreightTerm freightTerm) {
        this.shipmentType = shipmentType;
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

    public void initDims(List<HouseBlDim> dims) {
        this.dims = new ArrayList<>(dims);
    }

    public void initScheduleLegs(List<HouseBlScheduleLeg> scheduleLegs) {
        this.scheduleLegs = new ArrayList<>(scheduleLegs);
    }

    public void initLicenses(List<HouseBlLicense> licenses) {
        this.licenses = new ArrayList<>(licenses);
    }

    public void initTruckOrders(List<HouseBlTruckOrder> truckOrders) {
        this.truckOrders = new ArrayList<>(truckOrders);
    }

    // ── AirCharge (AIR에서만 채워짐, 다른 모드는 빈 컬렉션이 정상.
    //    본체 위치는 @OneToMany mappedBy 정합성 + 구조 일관성 확보) ───
    private List<HouseBlAirCharge> airCharges = new ArrayList<>();

    public void initAirCharges(List<HouseBlAirCharge> airCharges) {
        this.airCharges = new ArrayList<>(airCharges);
    }

    public void initDesc(HouseBlDesc desc) {
        this.desc = desc;
    }
}
