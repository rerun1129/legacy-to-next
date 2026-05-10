package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.FlightType;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.SecurityStatus;
import com.freightos.fms.domain.common.enums.VolumeDivisor;
import com.freightos.fms.domain.housebl.enums.HandlingInfoCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA ORM 엔티티 — Master B/L 항공 확장.
 * MasterBlJpaEntity 와 @OneToOne(FK: master_bl_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "master_bl_air")
@Getter
@NoArgsConstructor
public class MasterBlAirJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_air_id", updatable = false, nullable = false)
    private Long masterBlAirId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_bl_id", nullable = false, unique = true)
    private MasterBlJpaEntity masterBl;

    @Column(name = "airline_code", length = 10)
    private String airlineCode;

    @Column(name = "charge_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal chargeWeightKg;

    @Column(name = "volume_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal volumeWeightKg;

    @Column(name = "rate_class", length = 10)
    @Enumerated(EnumType.STRING)
    private RateClass rateClass;

    @Column(name = "currency_code", length = 5)
    private String currencyCode;

    @Column(name = "declared_value_carriage", length = 20)
    private String declaredValueCarriage;

    @Column(name = "declared_value_customs", length = 50)
    private String declaredValueCustoms;

    @Column(name = "insurance", length = 20)
    private String insurance;

    @Column(name = "account_information", length = 100)
    private String accountInformation;

    @Column(name = "security_status", length = 3)
    @Enumerated(EnumType.STRING)
    private SecurityStatus securityStatus;

    @Column(name = "flight_type", length = 30)
    @Enumerated(EnumType.STRING)
    private FlightType flightType;

    @Column(name = "issue_date", length = 8)
    private String issueDate;

    @Column(name = "issue_place", length = 50)
    private String issuePlace;

    @Column(name = "signature", length = 100)
    private String signature;

    @Column(name = "other_term", length = 100)
    @Enumerated(EnumType.STRING)
    private FreightTerm otherTerm;

    @Column(name = "handling_info_code", length = 30)
    @Enumerated(EnumType.STRING)
    private HandlingInfoCode handlingInfoCode;

    @Column(name = "handling_info_text", length = 500)
    private String handlingInfoText;

    @Column(name = "volume_divisor", length = 10)
    @Enumerated(EnumType.STRING)
    private VolumeDivisor volumeDivisor;

    // AIR 전용 스케줄 구간. master_bl_air_id FK로 소유.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JoinColumn(name = "master_bl_air_id", nullable = false, updatable = false)
    private List<MasterBlScheduleLegJpaEntity> scheduleLegs = new ArrayList<>();

    // AIR 전용 운임 그리드. master_bl_air_id FK로 소유 (Step 1.5).
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JoinColumn(name = "master_bl_air_id", nullable = false, updatable = false)
    private List<MasterBlAirChargeJpaEntity> airCharges = new ArrayList<>();

    public void setMasterBl(MasterBlJpaEntity v) { this.masterBl = v; }
    public void setAirlineCode(String v) { this.airlineCode = v; }
    public void setChargeWeightKg(BigDecimal v) { this.chargeWeightKg = v; }
    public void setVolumeWeightKg(BigDecimal v) { this.volumeWeightKg = v; }
    public void setRateClass(RateClass v) { this.rateClass = v; }
    public void setCurrencyCode(String v) { this.currencyCode = v; }
    public void setDeclaredValueCarriage(String v) { this.declaredValueCarriage = v; }
    public void setDeclaredValueCustoms(String v) { this.declaredValueCustoms = v; }
    public void setInsurance(String v) { this.insurance = v; }
    public void setAccountInformation(String v) { this.accountInformation = v; }
    public void setSecurityStatus(SecurityStatus v) { this.securityStatus = v; }
    public void setFlightType(FlightType v) { this.flightType = v; }
    public void setIssueDate(String v) { this.issueDate = v; }
    public void setIssuePlace(String v) { this.issuePlace = v; }
    public void setSignature(String v) { this.signature = v; }
    public void setOtherTerm(FreightTerm v) { this.otherTerm = v; }
    public void setHandlingInfoCode(HandlingInfoCode v) { this.handlingInfoCode = v; }
    public void setHandlingInfoText(String v) { this.handlingInfoText = v; }
    public void setVolumeDivisor(VolumeDivisor v) { this.volumeDivisor = v; }

    public void syncScheduleLegs(List<MasterBlScheduleLegJpaEntity> newLegs) {
        this.scheduleLegs.clear();
        this.scheduleLegs.addAll(newLegs);
    }

    public void syncAirCharges(List<MasterBlAirChargeJpaEntity> newCharges) {
        this.airCharges.clear();
        this.airCharges.addAll(newCharges);
    }
}
