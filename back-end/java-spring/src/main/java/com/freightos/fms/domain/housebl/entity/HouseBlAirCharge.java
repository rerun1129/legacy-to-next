package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.vo.CurrencyCode;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.enums.Per;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * E-21 House B/L AIR Charge 행.
 * AIR 모드에서만 채워지는 1:N 그리드 엔티티.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlAirCharge extends BaseEntity {

    private Long houseBlId;
    private String freightCode;
    private CurrencyCode currencyCode;
    private Per per;
    private FreightTerm freightTerm;
    private Weight grossWeightKg;
    private RateClass rateClass;
    private Weight chargeWeightKg;
    private BigDecimal rate;

    public static HouseBlAirCharge create(Long houseBlId) {
        HouseBlAirCharge c = new HouseBlAirCharge();
        c.houseBlId = houseBlId;
        return c;
    }

    public record Details(
            String freightCode,
            CurrencyCode currencyCode,
            Per per,
            FreightTerm freightTerm,
            Weight grossWeightKg,
            RateClass rateClass,
            Weight chargeWeightKg,
            BigDecimal rate
    ) {}

    public void updateDetails(Details d) {
        this.freightCode    = d.freightCode();
        this.currencyCode   = d.currencyCode();
        this.per            = d.per();
        this.freightTerm    = d.freightTerm();
        this.grossWeightKg  = d.grossWeightKg();
        this.rateClass      = d.rateClass();
        this.chargeWeightKg = d.chargeWeightKg();
        this.rate           = d.rate();
    }
}
