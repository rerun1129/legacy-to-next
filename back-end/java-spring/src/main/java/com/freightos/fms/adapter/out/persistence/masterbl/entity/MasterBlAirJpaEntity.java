package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — Master B/L 항공 확장.
 * MasterBlJpaEntity 와 @OneToOne(FK: master_bl_id) 관계.
 */
@Entity
@Table(name = "master_bl_air")
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

    @Column(name = "departure_code", length = 10)
    private String departureCode;

    @Column(name = "mawb_no", length = 50)
    private String mawbNo;

    @Column(name = "charge_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal chargeWeightKg;

    @Column(name = "volume_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal volumeWeightKg;

    @Column(name = "rate_class", length = 10)
    private String rateClass;

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

    @Column(name = "security_status", length = 20)
    private String securityStatus;

    @Column(name = "flight_type", length = 20)
    private String flightType;

    @Column(name = "issue_date", length = 8)
    private String issueDate;

    @Column(name = "issue_place", length = 50)
    private String issuePlace;

    @Column(name = "signature", length = 100)
    private String signature;

    public void setMasterBl(MasterBlJpaEntity v) { this.masterBl = v; }
    public void setAirlineCode(String v) { this.airlineCode = v; }
    public void setDepartureCode(String v) { this.departureCode = v; }
    public void setMawbNo(String v) { this.mawbNo = v; }
    public void setChargeWeightKg(BigDecimal v) { this.chargeWeightKg = v; }
    public void setVolumeWeightKg(BigDecimal v) { this.volumeWeightKg = v; }
    public void setRateClass(String v) { this.rateClass = v; }
    public void setCurrencyCode(String v) { this.currencyCode = v; }
    public void setDeclaredValueCarriage(String v) { this.declaredValueCarriage = v; }
    public void setDeclaredValueCustoms(String v) { this.declaredValueCustoms = v; }
    public void setInsurance(String v) { this.insurance = v; }
    public void setAccountInformation(String v) { this.accountInformation = v; }
    public void setSecurityStatus(String v) { this.securityStatus = v; }
    public void setFlightType(String v) { this.flightType = v; }
    public void setIssueDate(String v) { this.issueDate = v; }
    public void setIssuePlace(String v) { this.issuePlace = v; }
    public void setSignature(String v) { this.signature = v; }
}
