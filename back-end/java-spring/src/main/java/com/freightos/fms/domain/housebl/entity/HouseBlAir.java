package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * E-11 House B/L 항공 확장.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlAir extends HouseBl {

    private String airlineCode;
    private String departureCode;       // 출발 공항 IATA
    private String mawbNo;

    // 청구중량 (Chargeable Weight = max(grossWeight, volumeWeight))
    private BigDecimal chargeWeightKg;
    private BigDecimal volumeWeightKg;
    private String rateClass;           // GCR / SCR / CCR 등 IATA 운임 분류

    // 항공 Trade 필드
    private String currencyCode;
    private String declaredValueCarriage;  // D.V Carriage, 기본값 N.V.D.
    private String declaredValueCustoms;
    private String insurance;              // 기본값 NIL
    private String accountInformation;
    private String otherTerm;

    // 수출 전용
    private LocalDate issueDate;
    private String issuePlace;
    private String signature;

    // 수입 전용
    private String fhd;                // Not / F.H.D / To Door
    private String incoterms;
    private String freightTermAir;

    public static HouseBlAir create(Bound bound) {
        HouseBlAir entity = new HouseBlAir();
        entity.declaredValueCarriage = "N.V.D.";
        entity.insurance             = "NIL";
        return entity;
    }
}
