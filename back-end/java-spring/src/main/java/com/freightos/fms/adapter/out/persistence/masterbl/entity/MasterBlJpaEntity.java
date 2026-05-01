package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA ORM 엔티티 — Master B/L 공통 본체.
 * 도메인 엔티티(MasterBl)와 분리된 영속성 계층 객체.
 * 해상/항공 확장은 별도 독립 테이블(@OneToOne FK).
 */
@Entity
@Table(name = "master_bl")
@Getter
@NoArgsConstructor
public class MasterBlJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_id", updatable = false, nullable = false)
    private Long masterBlId;

    @Column(name = "mbl_no", length = 50)
    private String mblNo;

    @Column(name = "master_ref_no", length = 50)
    private String masterRefNo;

    @Column(name = "job_div", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private MasterBlJobDiv jobDiv;

    @Column(name = "bound", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Bound bound;

    @Column(name = "shipper_code", length = 20)
    private String shipperCode;

    @Column(name = "shipper_address", length = 500)
    private String shipperAddress;

    @Column(name = "consignee_code", length = 20)
    private String consigneeCode;

    @Column(name = "consignee_address", length = 500)
    private String consigneeAddress;

    @Column(name = "notify_code", length = 20)
    private String notifyCode;

    @Column(name = "notify_address", length = 500)
    private String notifyAddress;

    @Column(name = "pol_code", length = 10)
    private String polCode;

    @Column(name = "pod_code", length = 10)
    private String podCode;

    @Column(name = "etd", length = 8)
    private String etd;

    @Column(name = "eta", length = 8)
    private String eta;

    @Column(name = "freight_term", length = 10)
    @Enumerated(EnumType.STRING)
    private FreightTerm freightTerm;

    @Column(name = "operator_code", length = 20)
    private String operatorCode;

    @Column(name = "team_code", length = 20)
    private String teamCode;

    @Column(name = "pkg_qty")
    private Integer pkgQty;

    @Column(name = "pkg_unit", length = 10)
    private String pkgUnit;

    @Column(name = "gross_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal grossWeightKg;

    @Column(name = "cbm", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal cbm;

    @Column(name = "hs_code", length = 12)
    private String hsCode;

    @Column(name = "main_item_name", length = 100)
    private String mainItemName;

    @Column(name = "settle_partner_code", length = 20)
    private String settlePartnerCode;

    // SEA/AIR 모두 채워질 수 있음, null 허용
    @OneToOne(mappedBy = "masterBl", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private MasterBlDescJpaEntity desc;

    // AIR/NON_BL 등에서 채워짐, 다른 모드는 빈 컬렉션이 정상
    @OneToMany(mappedBy = "masterBl", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<MasterBlDimJpaEntity> dims = new ArrayList<>();

    // AIR에서 채워짐, 다른 모드는 빈 컬렉션이 정상
    @OneToMany(mappedBy = "masterBl", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<MasterBlScheduleLegJpaEntity> scheduleLegs = new ArrayList<>();

    // AIR에서만 채워짐, 다른 모드는 빈 컬렉션이 정상
    @OneToMany(mappedBy = "masterBl", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<MasterBlAirChargeJpaEntity> airCharges = new ArrayList<>();

    public void setMasterBlId(Long v) { this.masterBlId = v; }
    public void setMblNo(String v) { this.mblNo = v; }
    public void setMasterRefNo(String v) { this.masterRefNo = v; }
    public void setJobDiv(MasterBlJobDiv v) { this.jobDiv = v; }
    public void setBound(Bound v) { this.bound = v; }
    public void setShipperCode(String v) { this.shipperCode = v; }
    public void setShipperAddress(String v) { this.shipperAddress = v; }
    public void setConsigneeCode(String v) { this.consigneeCode = v; }
    public void setConsigneeAddress(String v) { this.consigneeAddress = v; }
    public void setNotifyCode(String v) { this.notifyCode = v; }
    public void setNotifyAddress(String v) { this.notifyAddress = v; }
    public void setPolCode(String v) { this.polCode = v; }
    public void setPodCode(String v) { this.podCode = v; }
    public void setEtd(String v) { this.etd = v; }
    public void setEta(String v) { this.eta = v; }
    public void setFreightTerm(FreightTerm v) { this.freightTerm = v; }
    public void setOperatorCode(String v) { this.operatorCode = v; }
    public void setTeamCode(String v) { this.teamCode = v; }
    public void setPkgQty(Integer v) { this.pkgQty = v; }
    public void setPkgUnit(String v) { this.pkgUnit = v; }
    public void setGrossWeightKg(BigDecimal v) { this.grossWeightKg = v; }
    public void setCbm(BigDecimal v) { this.cbm = v; }
    public void setHsCode(String v) { this.hsCode = v; }
    public void setMainItemName(String v) { this.mainItemName = v; }
    public void setSettlePartnerCode(String v) { this.settlePartnerCode = v; }

    public void syncAirCharges(List<MasterBlAirChargeJpaEntity> v) {
        this.airCharges.clear();
        if (v != null) this.airCharges.addAll(v);
    }

    public void syncDims(List<MasterBlDimJpaEntity> v) {
        this.dims.clear();
        if (v != null) this.dims.addAll(v);
    }

    public void syncScheduleLegs(List<MasterBlScheduleLegJpaEntity> v) {
        this.scheduleLegs.clear();
        if (v != null) this.scheduleLegs.addAll(v);
    }

    public void replaceDesc(MasterBlDescJpaEntity newDesc) {
        if (this.desc != null) this.desc.setMasterBl(null);
        this.desc = newDesc;
    }
}
