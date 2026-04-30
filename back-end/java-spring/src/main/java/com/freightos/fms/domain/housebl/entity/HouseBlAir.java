package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.housebl.enums.CargoType;
import com.freightos.fms.domain.housebl.enums.Fhd;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-11 House B/L 항공 확장.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlAir extends HouseBl {

    private AirlineCode airlineCode;

    // 청구중량 (Chargeable Weight = max(grossWeight, volumeWeight))
    private Weight chargeWeightKg;
    private Weight volumeWeightKg;
    private RateClass rateClass;

    // 항공 Trade 필드
    private CurrencyCode currencyCode;
    private String declaredValueCarriage;   // D.V Carriage, 기본값 N.V.D.
    private String declaredValueCustoms;
    private String insurance;               // 기본값 NIL
    private String accountInformation;
    private FreightTerm otherTerm;

    // 수출 전용
    private BlDate issueDate;
    private PortCode issuePlace;
    private String signature;

    // 수입 전용
    private Fhd fhd;

    private HandlingInformation handlingInformation;
    private String originOfGoods;
    private CargoType cargoType;

    protected HouseBlAir(Bound bound) {
        super(JobDiv.AIR, bound);
    }

    public static HouseBlAir create(Bound bound) {
        HouseBlAir entity = new HouseBlAir(bound);
        entity.declaredValueCarriage = "N.V.D.";
        entity.insurance             = "NIL";
        return entity;
    }

    public static record AirFields(
            AirlineCode airlineCode,
            Weight chargeWeightKg, Weight volumeWeightKg,
            RateClass rateClass, CurrencyCode currencyCode,
            String declaredValueCarriage, String declaredValueCustoms,
            String insurance, String accountInformation, FreightTerm otherTerm,
            BlDate issueDate, PortCode issuePlace, String signature,
            Fhd fhd,
            HandlingInformation handlingInformation,
            String originOfGoods,
            CargoType cargoType) {}

    public void updateAirFields(AirFields f) {
        this.airlineCode           = f.airlineCode();
        this.chargeWeightKg        = f.chargeWeightKg();
        this.volumeWeightKg        = f.volumeWeightKg();
        this.rateClass             = f.rateClass();
        this.currencyCode          = f.currencyCode();
        this.declaredValueCarriage = f.declaredValueCarriage();
        this.declaredValueCustoms  = f.declaredValueCustoms();
        this.insurance             = f.insurance();
        this.accountInformation    = f.accountInformation();
        this.otherTerm             = f.otherTerm();
        this.issueDate             = f.issueDate();
        this.issuePlace            = f.issuePlace();
        this.signature             = f.signature();
        this.fhd                   = f.fhd();
        this.handlingInformation   = f.handlingInformation();
        this.originOfGoods         = f.originOfGoods();
        this.cargoType             = f.cargoType();
    }
}
