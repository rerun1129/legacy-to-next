package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.housebl.enums.BlType;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.ShipmentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA ORM 엔티티 — House B/L 공통 본체.
 * 도메인 엔티티(HouseBl)와 분리된 영속성 계층 객체.
 */
@Entity
@Table(name = "house_bl")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "job_div", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class HouseBlJpaEntity extends BaseJpaEntity {

    @Column(name = "hbl_no", length = 50)
    private String hblNo;

    @Column(name = "job_div", insertable = false, updatable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private JobDiv jobDiv;

    @Column(name = "bound", nullable = false, length = 3)
    @Enumerated(EnumType.STRING)
    private Bound bound;

    @Column(name = "shipment_type", length = 10)
    @Enumerated(EnumType.STRING)
    private ShipmentType shipmentType;

    @Column(name = "bl_type", length = 15)
    @Enumerated(EnumType.STRING)
    private BlType blType;

    @Column(name = "freight_term", length = 10)
    @Enumerated(EnumType.STRING)
    private FreightTerm freightTerm;

    @Column(name = "shipper_code", length = 20)
    private String shipperCode;

    @Column(name = "consignee_code", length = 20)
    private String consigneeCode;

    @Column(name = "notify_code", length = 20)
    private String notifyCode;

    @Column(name = "doc_partner_code", length = 20)
    private String docPartnerCode;

    @Column(name = "pol_code", length = 10)
    private String polCode;

    @Column(name = "pod_code", length = 10)
    private String podCode;

    @Column(name = "delivery_code", length = 10)
    private String deliveryCode;

    @Column(name = "etd")
    private LocalDate etd;

    @Column(name = "eta")
    private LocalDate eta;

    @Column(name = "pkg_qty")
    private Integer pkgQty;

    @Column(name = "pkg_unit", length = 10)
    private String pkgUnit;

    @Column(name = "gross_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal grossWeightKg;

    @Column(name = "cbm", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal cbm;

    @Column(name = "actual_customer_code", length = 20)
    private String actualCustomerCode;

    @Column(name = "operator_code", length = 20)
    private String operatorCode;

    @Column(name = "team_code", length = 20)
    private String teamCode;

    @Column(name = "sales_man_code", length = 20)
    private String salesManCode;

    @Column(name = "master_bl_id")
    private UUID masterBlId;

    @OneToMany(mappedBy = "houseBl", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HouseBlContainerJpaEntity> containers = new ArrayList<>();

    protected HouseBlJpaEntity(JobDiv jobDiv, Bound bound) {
        this.jobDiv = jobDiv;
        this.bound  = bound;
    }

    public void setBound(Bound bound) { this.bound = bound; }
    public void setHblNo(String hblNo) { this.hblNo = hblNo; }
    public void setPolCode(String polCode) { this.polCode = polCode; }
    public void setPodCode(String podCode) { this.podCode = podCode; }
    public void setEtd(LocalDate etd) { this.etd = etd; }
    public void setEta(LocalDate eta) { this.eta = eta; }
    public void setActualCustomerCode(String code) { this.actualCustomerCode = code; }
    public void setOperatorCode(String code) { this.operatorCode = code; }
    public void setTeamCode(String code) { this.teamCode = code; }
    public void setSalesManCode(String code) { this.salesManCode = code; }
    public void setMasterBlId(UUID masterBlId) { this.masterBlId = masterBlId; }
    public void setShipmentType(ShipmentType shipmentType) { this.shipmentType = shipmentType; }
    public void setBlType(BlType blType) { this.blType = blType; }
    public void setFreightTerm(FreightTerm freightTerm) { this.freightTerm = freightTerm; }
    public void setShipperCode(String shipperCode) { this.shipperCode = shipperCode; }
    public void setConsigneeCode(String consigneeCode) { this.consigneeCode = consigneeCode; }
    public void setNotifyCode(String notifyCode) { this.notifyCode = notifyCode; }
    public void setDocPartnerCode(String docPartnerCode) { this.docPartnerCode = docPartnerCode; }
    public void setDeliveryCode(String deliveryCode) { this.deliveryCode = deliveryCode; }
    public void setPkgQty(Integer pkgQty) { this.pkgQty = pkgQty; }
    public void setPkgUnit(String pkgUnit) { this.pkgUnit = pkgUnit; }
    public void setGrossWeightKg(BigDecimal grossWeightKg) { this.grossWeightKg = grossWeightKg; }
    public void setCbm(BigDecimal cbm) { this.cbm = cbm; }
}
