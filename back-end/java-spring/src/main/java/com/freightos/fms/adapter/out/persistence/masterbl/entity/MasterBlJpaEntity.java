package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — Master B/L 공통 본체.
 * 도메인 엔티티(MasterBl)와 분리된 영속성 계층 객체.
 * 해상/항공 확장은 별도 독립 테이블(@OneToOne FK).
 */
@Entity
@Table(name = "master_bl")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private String jobDiv;

    @Column(name = "bound", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Bound bound;

    @Column(name = "shipper_code", length = 20)
    private String shipperCode;

    @Column(name = "consignee_code", length = 20)
    private String consigneeCode;

    @Column(name = "notify_code", length = 20)
    private String notifyCode;

    @Column(name = "pol_code", length = 10)
    private String polCode;

    @Column(name = "pod_code", length = 10)
    private String podCode;

    @Column(name = "etd", length = 10)
    private String etd;

    @Column(name = "eta", length = 10)
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

    @OneToOne(mappedBy = "masterBl", fetch = FetchType.LAZY)
    private MasterBlSeaJpaEntity seaExt;

    @OneToOne(mappedBy = "masterBl", fetch = FetchType.LAZY)
    private MasterBlAirJpaEntity airExt;

    public void setMasterBlId(Long v) { this.masterBlId = v; }
    public void setMblNo(String v) { this.mblNo = v; }
    public void setMasterRefNo(String v) { this.masterRefNo = v; }
    public void setJobDiv(String v) { this.jobDiv = v; }
    public void setBound(Bound v) { this.bound = v; }
    public void setShipperCode(String v) { this.shipperCode = v; }
    public void setConsigneeCode(String v) { this.consigneeCode = v; }
    public void setNotifyCode(String v) { this.notifyCode = v; }
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
}
