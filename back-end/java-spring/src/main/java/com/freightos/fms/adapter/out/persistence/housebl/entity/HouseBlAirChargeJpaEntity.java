package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — House B/L Air Charge 그리드 행.
 * AIR 모드 전용 1:N 자식. per는 Per.getCode() 문자열로 저장.
 */
@Entity
@Table(name = "house_bl_air_charge")
@Getter
@NoArgsConstructor
public class HouseBlAirChargeJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_air_charge_id", updatable = false, nullable = false)
    private Long houseBlAirChargeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false)
    private HouseBlJpaEntity houseBl;

    @Column(name = "freight_code", length = 20)
    private String freightCode;

    @Column(name = "currency_code", length = 5)
    private String currencyCode;

    // Per.getCode() 문자열로 저장 (@Enumerated 미사용)
    @Column(name = "per", length = 10)
    private String per;

    // FreightTerm.name() 문자열로 저장
    @Column(name = "freight_term", length = 10)
    private String freightTerm;

    @Column(name = "gross_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal grossWeightKg;

    // RateClass.name() 문자열로 저장
    @Column(name = "rate_class", length = 10)
    private String rateClass;

    @Column(name = "charge_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal chargeWeightKg;

    @Column(name = "rate", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal rate;

    public void setHouseBl(HouseBlJpaEntity v)        { this.houseBl       = v; }
    public void setFreightCode(String v)              { this.freightCode   = v; }
    public void setCurrencyCode(String v)             { this.currencyCode  = v; }
    public void setPer(String v)                      { this.per           = v; }
    public void setFreightTerm(String v)              { this.freightTerm   = v; }
    public void setGrossWeightKg(BigDecimal v)        { this.grossWeightKg = v; }
    public void setRateClass(String v)                { this.rateClass     = v; }
    public void setChargeWeightKg(BigDecimal v)       { this.chargeWeightKg = v; }
    public void setRate(BigDecimal v)                 { this.rate          = v; }
}
