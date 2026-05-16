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
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA ORM 엔티티 — Master B/L 항공 확장.
 * MasterBlJpaEntity 와 @OneToOne(FK: master_bl_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "master_bl_air")
@Getter
@NoArgsConstructor
@DynamicUpdate
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

    @Column(name = "remark", length = 1000)
    private String remark;

    // AIR 단독 치수 명세. master_bl_air_id FK로 소유 (Step 4.2 — 부모 FK 이전).
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JoinColumn(name = "master_bl_air_id", nullable = false, updatable = false)
    private List<MasterBlDimJpaEntity> dims = new ArrayList<>();

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

    public void setMasterBlAirId(Long v) { this.masterBlAirId = v; }
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
    public void setRemark(String v) { this.remark = v; }

    /**
     * AIR DIM merge-by-id (§6.28 자식 row PUT + merge-by-id).
     * incoming id가 기존 영속 엔티티와 일치하면 필드 mutate(UPDATE), 없으면 신규 추가(INSERT).
     * orphanRemoval이 제거된 엔티티를 자동 DELETE한다.
     */
    public void mergeDims(List<MasterBlDimJpaEntity> incoming) {
        Map<Long, MasterBlDimJpaEntity> existingById = new HashMap<>();
        for (MasterBlDimJpaEntity e : this.dims) {
            if (e.getMasterBlDimId() != null) existingById.put(e.getMasterBlDimId(), e);
        }
        List<MasterBlDimJpaEntity> merged = new ArrayList<>();
        for (MasterBlDimJpaEntity inc : incoming) {
            if (inc.getMasterBlDimId() != null && existingById.containsKey(inc.getMasterBlDimId())) {
                MasterBlDimJpaEntity existing = existingById.get(inc.getMasterBlDimId());
                copyDimFields(inc, existing);
                merged.add(existing);
            } else {
                merged.add(inc);
            }
        }
        this.dims.clear();
        this.dims.addAll(merged);
    }

    /**
     * AIR ScheduleLeg merge-by-id (§6.28 자식 row PUT + merge-by-id).
     * incoming id가 기존 영속 엔티티와 일치하면 필드 mutate(UPDATE), 없으면 신규 추가(INSERT).
     * orphanRemoval이 제거된 엔티티를 자동 DELETE한다.
     */
    public void mergeScheduleLegs(List<MasterBlScheduleLegJpaEntity> incoming) {
        Map<Long, MasterBlScheduleLegJpaEntity> existingById = new HashMap<>();
        for (MasterBlScheduleLegJpaEntity e : this.scheduleLegs) {
            if (e.getMasterBlScheduleLegId() != null) existingById.put(e.getMasterBlScheduleLegId(), e);
        }
        List<MasterBlScheduleLegJpaEntity> merged = new ArrayList<>();
        for (MasterBlScheduleLegJpaEntity inc : incoming) {
            if (inc.getMasterBlScheduleLegId() != null && existingById.containsKey(inc.getMasterBlScheduleLegId())) {
                MasterBlScheduleLegJpaEntity existing = existingById.get(inc.getMasterBlScheduleLegId());
                copyScheduleLegFields(inc, existing);
                merged.add(existing);
            } else {
                merged.add(inc);
            }
        }
        this.scheduleLegs.clear();
        this.scheduleLegs.addAll(merged);
    }

    /**
     * AIR AirCharge merge-by-id (§6.28 자식 row PUT + merge-by-id).
     * incoming id가 기존 영속 엔티티와 일치하면 필드 mutate(UPDATE), 없으면 신규 추가(INSERT).
     * orphanRemoval이 제거된 엔티티를 자동 DELETE한다.
     */
    public void mergeAirCharges(List<MasterBlAirChargeJpaEntity> incoming) {
        Map<Long, MasterBlAirChargeJpaEntity> existingById = new HashMap<>();
        for (MasterBlAirChargeJpaEntity e : this.airCharges) {
            if (e.getMasterBlAirChargeId() != null) existingById.put(e.getMasterBlAirChargeId(), e);
        }
        List<MasterBlAirChargeJpaEntity> merged = new ArrayList<>();
        for (MasterBlAirChargeJpaEntity inc : incoming) {
            if (inc.getMasterBlAirChargeId() != null && existingById.containsKey(inc.getMasterBlAirChargeId())) {
                MasterBlAirChargeJpaEntity existing = existingById.get(inc.getMasterBlAirChargeId());
                copyAirChargeFields(inc, existing);
                merged.add(existing);
            } else {
                merged.add(inc);
            }
        }
        this.airCharges.clear();
        this.airCharges.addAll(merged);
    }

    private static void copyDimFields(MasterBlDimJpaEntity src, MasterBlDimJpaEntity dst) {
        dst.setLengthCm(src.getLengthCm());
        dst.setWidthCm(src.getWidthCm());
        dst.setHeightCm(src.getHeightCm());
        dst.setQuantity(src.getQuantity());
        dst.setCbm(src.getCbm());
        dst.setVolumeWeightKg(src.getVolumeWeightKg());
    }

    private static void copyScheduleLegFields(MasterBlScheduleLegJpaEntity src, MasterBlScheduleLegJpaEntity dst) {
        dst.setToCode(src.getToCode());
        dst.setByCarrier(src.getByCarrier());
        dst.setFlightNo(src.getFlightNo());
        dst.setOnBoardDt(src.getOnBoardDt());
        dst.setOnBoardTm(src.getOnBoardTm());
        dst.setArrivalDt(src.getArrivalDt());
        dst.setArrivalTm(src.getArrivalTm());
    }

    private static void copyAirChargeFields(MasterBlAirChargeJpaEntity src, MasterBlAirChargeJpaEntity dst) {
        dst.setFreightCode(src.getFreightCode());
        dst.setCurrencyCode(src.getCurrencyCode());
        dst.setPer(src.getPer());
        dst.setFreightTerm(src.getFreightTerm());
        dst.setGrossWeightKg(src.getGrossWeightKg());
        dst.setRateClass(src.getRateClass());
        dst.setChargeWeightKg(src.getChargeWeightKg());
        dst.setRate(src.getRate());
    }
}
