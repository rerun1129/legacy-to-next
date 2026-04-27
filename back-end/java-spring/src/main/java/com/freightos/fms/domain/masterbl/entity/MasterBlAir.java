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

    // 수출 전용 Issue — ISO 8601 문자열 (yyyy-MM-dd)
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

    public void updateAirFields(String airlineCode, String departureCode, String mawbNo,
                                BigDecimal chargeWeightKg, BigDecimal volumeWeightKg,
                                String rateClass, String currencyCode,
                                String declaredValueCarriage, String declaredValueCustoms,
                                String insurance, String accountInformation,
                                String securityStatus, String flightType,
                                String issueDate, String issuePlace, String signature) {
        this.airlineCode            = airlineCode;
        this.departureCode          = departureCode;
        this.mawbNo                 = mawbNo;
        this.chargeWeightKg         = chargeWeightKg;
        this.volumeWeightKg         = volumeWeightKg;
        this.rateClass              = rateClass;
        this.currencyCode           = currencyCode;
        this.declaredValueCarriage  = declaredValueCarriage;
        this.declaredValueCustoms   = declaredValueCustoms;
        this.insurance              = insurance;
        this.accountInformation     = accountInformation;
        this.securityStatus         = securityStatus;
        this.flightType             = flightType;
        this.issueDate              = issueDate;
        this.issuePlace             = issuePlace;
        this.signature              = signature;
    }
}
