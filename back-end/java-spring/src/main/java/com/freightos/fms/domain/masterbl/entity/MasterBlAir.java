package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.domain.housebl.enums.Bound;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** E-04 Master B/L 항공 확장. master_bl + master_bl_air JOIN. */
@Entity
@Table(name = "master_bl_air")
@DiscriminatorValue("AIR")
@PrimaryKeyJoinColumn(name = "master_bl_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlAir extends MasterBl {

    @Column(name = "airline_code", length = 10) private String airlineCode;
    @Column(name = "departure_code", length = 10) private String departureCode;
    @Column(name = "mawb_no", length = 50) private String mawbNo;

    @Column(name = "charge_weight_kg", columnDefinition = "NUMERIC(12,3)") private BigDecimal chargeWeightKg;
    @Column(name = "volume_weight_kg", columnDefinition = "NUMERIC(12,3)") private BigDecimal volumeWeightKg;
    @Column(name = "rate_class", length = 10) private String rateClass;

    @Column(name = "currency_code", length = 5) private String currencyCode;
    @Column(name = "declared_value_carriage", length = 20) private String declaredValueCarriage;
    @Column(name = "declared_value_customs", length = 50) private String declaredValueCustoms;
    @Column(name = "insurance", length = 20) private String insurance;
    @Column(name = "account_information", length = 100) private String accountInformation;

    // 보안검색 (항공 수출 전용)
    @Column(name = "security_status", length = 20) private String securityStatus;
    @Column(name = "flight_type", length = 20) private String flightType;

    // 수출 전용 Issue
    @Column(name = "issue_date") private LocalDate issueDate;
    @Column(name = "issue_place", length = 50) private String issuePlace;
    @Column(name = "signature", length = 100) private String signature;

    public static MasterBlAir create(Bound bound) {
        MasterBlAir e = new MasterBlAir();
        e.declaredValueCarriage = "N.V.D.";
        e.insurance             = "NIL";
        return e;
    }
}
