package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * E-04 Master B/L 항공 확장.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlAir extends MasterBl {

    private String airlineCode;
    private String departureCode;
    private String mawbNo;

    private BigDecimal chargeWeightKg;
    private BigDecimal volumeWeightKg;
    private String rateClass;

    private String currencyCode;
    private String declaredValueCarriage;
    private String declaredValueCustoms;
    private String insurance;
    private String accountInformation;

    // 보안검색 (항공 수출 전용)
    private String securityStatus;
    private String flightType;

    // 수출 전용 Issue — yyyyMMdd
    private String issueDate;
    private String issuePlace;
    private String signature;

    protected MasterBlAir(Bound bound) {
        super("AIR", bound);
    }

    public static MasterBlAir create(Bound bound) {
        MasterBlAir e = new MasterBlAir(bound);
        e.declaredValueCarriage = "N.V.D.";
        e.insurance             = "NIL";
        return e;
    }

    public static record AirFields(
            String airlineCode, String departureCode, String mawbNo,
            BigDecimal chargeWeightKg, BigDecimal volumeWeightKg,
            String rateClass, String currencyCode,
            String declaredValueCarriage, String declaredValueCustoms,
            String insurance, String accountInformation,
            String securityStatus, String flightType,
            String issueDate, String issuePlace, String signature) {}

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
