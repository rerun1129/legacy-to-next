package com.freightos.fms.adapter.out.persistence.switchbl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — Switch B/L 본체.
 * HouseBlJpaEntity 와 @OneToOne(FK: house_bl_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "switch_bl")
@Getter
@NoArgsConstructor
public class SwitchBlJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "switch_bl_id", updatable = false, nullable = false)
    private Long switchBlId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false, unique = true, updatable = false)
    private HouseBlJpaEntity houseBl;

    @OneToOne(mappedBy = "switchBl", fetch = FetchType.LAZY)
    private SwitchBlDescriptionJpaEntity description;

    @Column(name = "switch_bl_no", length = 50, nullable = false)
    private String switchBlNo;

    @Column(name = "shipper_code", nullable = false, length = 20)
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

    @Column(name = "bl_type", length = 15)
    private String blType;

    @Column(name = "incoterms", length = 10)
    private String incoterms;

    public void setHouseBl(HouseBlJpaEntity v)       { this.houseBl          = v; }
    public void setSwitchBlNo(String v)              { this.switchBlNo       = v; }
    public void setShipperCode(String v)             { this.shipperCode      = v; }
    public void setShipperAddress(String v)          { this.shipperAddress   = v; }
    public void setConsigneeCode(String v)           { this.consigneeCode    = v; }
    public void setConsigneeAddress(String v)        { this.consigneeAddress = v; }
    public void setNotifyCode(String v)              { this.notifyCode       = v; }
    public void setNotifyAddress(String v)           { this.notifyAddress    = v; }
    public void setBlType(String v)                  { this.blType           = v; }
    public void setIncoterms(String v)               { this.incoterms        = v; }
}
