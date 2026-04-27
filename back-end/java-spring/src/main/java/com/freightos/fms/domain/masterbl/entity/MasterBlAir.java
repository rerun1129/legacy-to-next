package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    // 수출 전용 Issue
    private LocalDate issueDate;
    private String issuePlace;
    private String signature;

    public static MasterBlAir create(Bound bound) {
        MasterBlAir e = new MasterBlAir();
        e.declaredValueCarriage = "N.V.D.";
        e.insurance             = "NIL";
        return e;
    }
}
