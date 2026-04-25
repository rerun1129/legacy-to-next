package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.housebl.enums.Bound;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * E-11 House B/L 항공 확장.
 * house_bl + house_bl_air JOIN.
 */
@Entity
@Table(name = "house_bl_air")
@DiscriminatorValue("AIR")
@PrimaryKeyJoinColumn(name = "house_bl_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlAir extends HouseBl {

    @Column(name = "airline_code", length = 10)
    private String airlineCode;

    @Column(name = "departure_code", length = 10)
    private String departureCode;       // 출발 공항 IATA

    @Column(name = "mawb_no", length = 50)
    private String mawbNo;

    // 청구중량 (Chargeable Weight = max(grossWeight, volumeWeight))
    @Column(name = "charge_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal chargeWeightKg;

    @Column(name = "volume_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal volumeWeightKg;

    @Column(name = "rate_class", length = 10)
    private String rateClass;           // GCR / SCR / CCR 등 IATA 운임 분류

    // 항공 Trade 필드
    @Column(name = "currency_code", length = 5)
    private String currencyCode;

    @Column(name = "declared_value_carriage", length = 20)
    private String declaredValueCarriage;  // D.V Carriage, 기본값 N.V.D.

    @Column(name = "declared_value_customs", length = 50)
    private String declaredValueCustoms;

    @Column(name = "insurance", length = 20)
    private String insurance;              // 기본값 NIL

    @Column(name = "account_information", length = 100)
    private String accountInformation;

    @Column(name = "other_term", length = 100)
    private String otherTerm;

    // 수출 전용
    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "issue_place", length = 50)
    private String issuePlace;

    @Column(name = "signature", length = 100)
    private String signature;

    // 수입 전용
    @Column(name = "fhd", length = 10)  // Not / F.H.D / To Door
    private String fhd;

    @Column(name = "incoterms", length = 10)
    private String incoterms;

    @Column(name = "freight_term", length = 10)
    private String freightTermAir;

    public static HouseBlAir create(Bound bound) {
        HouseBlAir entity = new HouseBlAir();
        entity.declaredValueCarriage = "N.V.D.";
        entity.insurance             = "NIL";
        return entity;
    }
}
