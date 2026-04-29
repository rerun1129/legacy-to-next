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
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA ORM 엔티티 — House B/L 공통 본체.
 * 도메인 엔티티(HouseBl)와 분리된 영속성 계층 객체.
 * 해상/항공/트럭/Non-BL 확장은 별도 독립 테이블(@OneToOne FK).
 */
@Entity
@Table(name = "house_bl")
@Getter
@NoArgsConstructor
public class HouseBlJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_id", updatable = false, nullable = false)
    private Long houseBlId;

    @Column(name = "hbl_no", length = 50)
    private String hblNo;

    @Column(name = "job_div", nullable = false, length = 10)
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

    @Column(name = "doc_partner_code", length = 20)
    private String docPartnerCode;

    @Column(name = "doc_partner_address", length = 500)
    private String docPartnerAddress;

    @Column(name = "pol_code", length = 10)
    private String polCode;

    @Column(name = "pod_code", length = 10)
    private String podCode;

    @Column(name = "delivery_code", length = 10)
    private String deliveryCode;

    @Column(name = "etd", length = 8)
    private String etd;

    @Column(name = "eta", length = 8)
    private String eta;

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
    private Long masterBlId;

    @OneToOne(mappedBy = "houseBl", fetch = FetchType.LAZY)
    private HouseBlSeaJpaEntity seaExt;

    @OneToOne(mappedBy = "houseBl", fetch = FetchType.LAZY)
    private HouseBlAirJpaEntity airExt;

    @OneToOne(mappedBy = "houseBl", fetch = FetchType.LAZY)
    private HouseBlTruckJpaEntity truckExt;

    @OneToOne(mappedBy = "houseBl", fetch = FetchType.LAZY)
    private HouseBlNonBlJpaEntity nonBlExt;

    @OneToMany(mappedBy = "houseBl", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<HouseBlContainerJpaEntity> containers = new ArrayList<>();

    public void setHouseBlId(Long v) { this.houseBlId = v; }
    public void setBound(Bound bound) { this.bound = bound; }
    public void setJobDiv(JobDiv jobDiv) { this.jobDiv = jobDiv; }
    public void setHblNo(String hblNo) { this.hblNo = hblNo; }
    public void setPolCode(String polCode) { this.polCode = polCode; }
    public void setPodCode(String podCode) { this.podCode = podCode; }
    public void setEtd(String etd) { this.etd = etd; }
    public void setEta(String eta) { this.eta = eta; }
    public void setActualCustomerCode(String code) { this.actualCustomerCode = code; }
    public void setOperatorCode(String code) { this.operatorCode = code; }
    public void setTeamCode(String code) { this.teamCode = code; }
    public void setSalesManCode(String code) { this.salesManCode = code; }
    public void setMasterBlId(Long masterBlId) { this.masterBlId = masterBlId; }
    public void setShipmentType(ShipmentType shipmentType) { this.shipmentType = shipmentType; }
    public void setBlType(BlType blType) { this.blType = blType; }
    public void setFreightTerm(FreightTerm freightTerm) { this.freightTerm = freightTerm; }
    public void setShipperCode(String shipperCode) { this.shipperCode = shipperCode; }
    public void setShipperAddress(String shipperAddress) { this.shipperAddress = shipperAddress; }
    public void setConsigneeCode(String consigneeCode) { this.consigneeCode = consigneeCode; }
    public void setConsigneeAddress(String consigneeAddress) { this.consigneeAddress = consigneeAddress; }
    public void setNotifyCode(String notifyCode) { this.notifyCode = notifyCode; }
    public void setNotifyAddress(String notifyAddress) { this.notifyAddress = notifyAddress; }
    public void setDocPartnerCode(String docPartnerCode) { this.docPartnerCode = docPartnerCode; }
    public void setDocPartnerAddress(String docPartnerAddress) { this.docPartnerAddress = docPartnerAddress; }
    public void setDeliveryCode(String deliveryCode) { this.deliveryCode = deliveryCode; }
    public void setPkgQty(Integer pkgQty) { this.pkgQty = pkgQty; }
    public void setPkgUnit(String pkgUnit) { this.pkgUnit = pkgUnit; }
    public void setGrossWeightKg(BigDecimal grossWeightKg) { this.grossWeightKg = grossWeightKg; }
    public void setCbm(BigDecimal cbm) { this.cbm = cbm; }

    // orphanRemoval=true 컬렉션 동기화: 참조를 교체하지 않고 clear+addAll로 관리
    public void syncContainers(List<HouseBlContainerJpaEntity> newContainers) {
        this.containers.clear();
        this.containers.addAll(newContainers);
    }
}
