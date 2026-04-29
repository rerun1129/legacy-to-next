package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.FlightType;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.SecurityStatus;
import com.freightos.fms.domain.common.vo.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-04 Master B/L 항공 확장.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlAir extends MasterBl {

    private AirlineCode airlineCode;
    private AirportCode departureCode;
    private BlNumber mawbNo; //TODO : MasterBl의 mblNo와 중복 필드. 만약 인덱싱을 위한 후보키라면 필드명을 맞출것

    private Weight chargeWeightKg;
    private Weight volumeWeightKg;
    private RateClass rateClass;

    private CurrencyCode currencyCode;
    private String declaredValueCarriage;
    private String declaredValueCustoms;
    private String insurance;
    private String accountInformation;

    private SecurityStatus securityStatus;
    private FlightType flightType;

    // 수출 전용 Issue
    private BlDate issueDate;
    private PortCode issuePlace;
    private String signature;

    protected MasterBlAir(Bound bound) {
        super(MasterBlJobDiv.AIR, bound);
    }

    public static MasterBlAir create(Bound bound) {
        MasterBlAir e = new MasterBlAir(bound);
        e.declaredValueCarriage = "N.V.D.";
        e.insurance             = "NIL";
        return e;
    }

    public static record AirFields(
            AirlineCode airlineCode, AirportCode departureCode, BlNumber mawbNo,
            Weight chargeWeightKg, Weight volumeWeightKg,
            RateClass rateClass, CurrencyCode currencyCode,
            String declaredValueCarriage, String declaredValueCustoms,
            String insurance, String accountInformation,
            SecurityStatus securityStatus, FlightType flightType,
            BlDate issueDate, PortCode issuePlace, String signature) {}

    public void updateAirFields(AirFields f) {
        this.airlineCode           = f.airlineCode();
        this.departureCode         = f.departureCode();
        this.mawbNo                = f.mawbNo();
        this.chargeWeightKg        = f.chargeWeightKg();
        this.volumeWeightKg        = f.volumeWeightKg();
        this.rateClass             = f.rateClass();
        this.currencyCode          = f.currencyCode();
        this.declaredValueCarriage = f.declaredValueCarriage();
        this.declaredValueCustoms  = f.declaredValueCustoms();
        this.insurance             = f.insurance();
        this.accountInformation    = f.accountInformation();
        this.securityStatus        = f.securityStatus();
        this.flightType            = f.flightType();
        this.issueDate             = f.issueDate();
        this.issuePlace            = f.issuePlace();
        this.signature             = f.signature();
    }
}
