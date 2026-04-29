package com.freightos.fms.adapter.out.persistence.switchbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — Switch B/L 본체.
 * HouseBlJpaEntity 와 @OneToOne(FK: house_bl_id) 관계.
 */
@Entity
@Table(name = "switch_bl")
@Getter
@NoArgsConstructor
public class SwitchBlJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "switch_bl_id", updatable = false, nullable = false)
    private Long switchBlId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false, unique = true)
    private HouseBlJpaEntity houseBl;

    @OneToOne(mappedBy = "switchBl", fetch = FetchType.LAZY)
    private SwitchBlDescriptionJpaEntity description;

    @Column(name = "switch_bl_no", length = 50)
    private String switchBlNo;

    @Column(name = "bl_type", length = 15)
    private String blType;

    @Column(length = 10)
    private String incoterms;

    @Column(name = "shipper_code", nullable = false, length = 20)
    private String shipperCode;

    @Column(name = "consignee_code", length = 20)
    private String consigneeCode;

    @Column(name = "notify_code", length = 20)
    private String notifyCode;

    public void setHouseBl(HouseBlJpaEntity v)  { this.houseBl      = v; }
    public void setSwitchBlNo(String v)          { this.switchBlNo   = v; }
    public void setBlType(String v)              { this.blType       = v; }
    public void setIncoterms(String v)           { this.incoterms    = v; }
    public void setShipperCode(String v)         { this.shipperCode  = v; }
    public void setConsigneeCode(String v)       { this.consigneeCode = v; }
    public void setNotifyCode(String v)          { this.notifyCode   = v; }
}
