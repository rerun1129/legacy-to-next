package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.Per;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — Master B/L Air Charge 그리드 행.
 * AIR 모드 전용 1:N 자식.
 */
@Entity
@Table(name = "master_bl_air_charge")
@Getter
@NoArgsConstructor
public class MasterBlAirChargeJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_air_charge_id", updatable = false, nullable = false)
    private Long masterBlAirChargeId;

    @Column(name = "master_bl_id", nullable = false, insertable = false, updatable = false)
    private Long masterBlId;

    @Column(name = "freight_code", length = 20)
    private String freightCode;

    @Column(name = "currency_code", length = 5)
    private String currencyCode;

    @Column(name = "per", length = 10)
    @Enumerated(EnumType.STRING)
    private Per per;

    @Column(name = "freight_term", length = 10)
    @Enumerated(EnumType.STRING)
    private FreightTerm freightTerm;

    @Column(name = "gross_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal grossWeightKg;

    @Column(name = "rate_class", length = 10)
    @Enumerated(EnumType.STRING)
    private RateClass rateClass;

    @Column(name = "charge_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal chargeWeightKg;

    @Column(name = "rate", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal rate;

    public void setMasterBlId(Long v)                  { this.masterBlId    = v; }
    public void setFreightCode(String v)               { this.freightCode   = v; }
    public void setCurrencyCode(String v)              { this.currencyCode  = v; }
    public void setPer(Per v)                          { this.per           = v; }
    public void setFreightTerm(FreightTerm v)          { this.freightTerm   = v; }
    public void setGrossWeightKg(BigDecimal v)         { this.grossWeightKg = v; }
    public void setRateClass(RateClass v)               { this.rateClass     = v; }
    public void setChargeWeightKg(BigDecimal v)        { this.chargeWeightKg = v; }
    public void setRate(BigDecimal v)                  { this.rate          = v; }
}
