package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.FreightTerm;
import com.freightos.fms.domain.common.enums.RateClass;
import com.freightos.fms.domain.common.enums.VolumeDivisor;
import com.freightos.fms.domain.housebl.enums.CargoType;
import com.freightos.fms.domain.housebl.enums.Fhd;
import com.freightos.fms.domain.housebl.enums.HandlingInfoCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA ORM 엔티티 — House B/L 항공 확장.
 * HouseBlJpaEntity 와 @OneToOne(FK: house_bl_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "house_bl_air")
@Getter
@NoArgsConstructor
public class HouseBlAirJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_air_id", updatable = false, nullable = false)
    private Long houseBlAirId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false, unique = true, updatable = false)
    private HouseBlJpaEntity houseBl;

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

    @Column(name = "other_term", length = 100)
    @Enumerated(EnumType.STRING)
    private FreightTerm otherTerm;

    @Column(name = "issue_date", length = 8)
    private String issueDate;

    @Column(name = "issue_place", length = 50)
    private String issuePlace;

    @Column(name = "signature", length = 100)
    private String signature;

    @Column(name = "fhd", length = 10)
    @Enumerated(EnumType.STRING)
    private Fhd fhd;

    @Column(name = "volume_divisor", length = 10)
    @Enumerated(EnumType.STRING)
    private VolumeDivisor volumeDivisor;

    @Column(name = "handling_info_code", length = 30)
    @Enumerated(EnumType.STRING)
    private HandlingInfoCode handlingInfoCode;

    @Column(name = "handling_info_text", length = 500)
    private String handlingInfoText;

    @Column(name = "origin_of_goods", length = 100)
    private String originOfGoods;

    @Column(name = "cargo_type", length = 30)
    @Enumerated(EnumType.STRING)
    private CargoType cargoType;

    @Column(name = "remark", length = 1000)
    private String remark;

    // AIR 전용 치수 명세. house_bl_air_id FK로 소유.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JoinColumn(name = "house_bl_air_id", nullable = false, updatable = false)
    private List<HouseBlAirDimJpaEntity> dims = new ArrayList<>();

    // AIR 전용 스케줄 구간. house_bl_air_id FK로 소유.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JoinColumn(name = "house_bl_air_id", nullable = false, updatable = false)
    private List<HouseBlScheduleLegJpaEntity> scheduleLegs = new ArrayList<>();

    // AIR 전용 운임 항목. house_bl_air_id FK로 소유.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JoinColumn(name = "house_bl_air_id", nullable = false, updatable = false)
    private List<HouseBlAirChargeJpaEntity> airCharges = new ArrayList<>();

    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setAirlineCode(String v) { this.airlineCode = v; }
    public void setChargeWeightKg(BigDecimal v) { this.chargeWeightKg = v; }
    public void setVolumeWeightKg(BigDecimal v) { this.volumeWeightKg = v; }
    public void setRateClass(RateClass v) { this.rateClass = v; }
    public void setCurrencyCode(String v) { this.currencyCode = v; }
    public void setDeclaredValueCarriage(String v) { this.declaredValueCarriage = v; }
    public void setDeclaredValueCustoms(String v) { this.declaredValueCustoms = v; }
    public void setInsurance(String v) { this.insurance = v; }
    public void setAccountInformation(String v) { this.accountInformation = v; }
    public void setOtherTerm(FreightTerm v) { this.otherTerm = v; }
    public void setIssueDate(String v) { this.issueDate = v; }
    public void setIssuePlace(String v) { this.issuePlace = v; }
    public void setSignature(String v) { this.signature = v; }
    public void setFhd(Fhd v) { this.fhd = v; }
    public void setVolumeDivisor(VolumeDivisor v) { this.volumeDivisor = v; }
    public void setHandlingInfoCode(HandlingInfoCode v) { this.handlingInfoCode = v; }
    public void setHandlingInfoText(String v) { this.handlingInfoText = v; }
    public void setOriginOfGoods(String v) { this.originOfGoods = v; }
    public void setCargoType(CargoType v) { this.cargoType = v; }
    public void setRemark(String v) { this.remark = v; }

    public List<HouseBlAirDimJpaEntity> getDims() { return dims; }

    public void syncDims(List<HouseBlAirDimJpaEntity> newDims) {
        this.dims.clear();
        this.dims.addAll(newDims);
    }

    public void syncScheduleLegs(List<HouseBlScheduleLegJpaEntity> newLegs) {
        this.scheduleLegs.clear();
        this.scheduleLegs.addAll(newLegs);
    }

    public void syncAirCharges(List<HouseBlAirChargeJpaEntity> newCharges) {
        this.airCharges.clear();
        this.airCharges.addAll(newCharges);
    }

    /**
     * AIR DIM merge-by-id (§6.28 자식 row PUT + merge-by-id).
     * incoming id가 기존 영속 엔티티와 일치하면 필드 mutate(UPDATE), 없으면 신규 추가(INSERT).
     * orphanRemoval이 제거된 엔티티를 자동 DELETE한다.
     */
    public void mergeDims(List<HouseBlAirDimJpaEntity> incoming) {
        Map<Long, HouseBlAirDimJpaEntity> existingById = new HashMap<>();
        for (HouseBlAirDimJpaEntity e : this.dims) {
            if (e.getHouseBlAirDimId() != null) existingById.put(e.getHouseBlAirDimId(), e);
        }
        List<HouseBlAirDimJpaEntity> merged = new ArrayList<>();
        for (HouseBlAirDimJpaEntity inc : incoming) {
            if (inc.getHouseBlAirDimId() != null && existingById.containsKey(inc.getHouseBlAirDimId())) {
                HouseBlAirDimJpaEntity existing = existingById.get(inc.getHouseBlAirDimId());
                copyAirDimFields(inc, existing);
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
    public void mergeScheduleLegs(List<HouseBlScheduleLegJpaEntity> incoming) {
        Map<Long, HouseBlScheduleLegJpaEntity> existingById = new HashMap<>();
        for (HouseBlScheduleLegJpaEntity e : this.scheduleLegs) {
            if (e.getHouseBlScheduleLegId() != null) existingById.put(e.getHouseBlScheduleLegId(), e);
        }
        List<HouseBlScheduleLegJpaEntity> merged = new ArrayList<>();
        for (HouseBlScheduleLegJpaEntity inc : incoming) {
            if (inc.getHouseBlScheduleLegId() != null && existingById.containsKey(inc.getHouseBlScheduleLegId())) {
                HouseBlScheduleLegJpaEntity existing = existingById.get(inc.getHouseBlScheduleLegId());
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
    public void mergeAirCharges(List<HouseBlAirChargeJpaEntity> incoming) {
        Map<Long, HouseBlAirChargeJpaEntity> existingById = new HashMap<>();
        for (HouseBlAirChargeJpaEntity e : this.airCharges) {
            if (e.getHouseBlAirChargeId() != null) existingById.put(e.getHouseBlAirChargeId(), e);
        }
        List<HouseBlAirChargeJpaEntity> merged = new ArrayList<>();
        for (HouseBlAirChargeJpaEntity inc : incoming) {
            if (inc.getHouseBlAirChargeId() != null && existingById.containsKey(inc.getHouseBlAirChargeId())) {
                HouseBlAirChargeJpaEntity existing = existingById.get(inc.getHouseBlAirChargeId());
                copyAirChargeFields(inc, existing);
                merged.add(existing);
            } else {
                merged.add(inc);
            }
        }
        this.airCharges.clear();
        this.airCharges.addAll(merged);
    }

    private void copyAirDimFields(HouseBlAirDimJpaEntity src, HouseBlAirDimJpaEntity dst) {
        dst.setLengthCm(src.getLengthCm());
        dst.setWidthCm(src.getWidthCm());
        dst.setHeightCm(src.getHeightCm());
        dst.setQuantity(src.getQuantity());
        dst.setCbm(src.getCbm());
        dst.setVolumeWeightKg(src.getVolumeWeightKg());
    }

    private void copyScheduleLegFields(HouseBlScheduleLegJpaEntity src, HouseBlScheduleLegJpaEntity dst) {
        dst.setToCode(src.getToCode());
        dst.setByCarrier(src.getByCarrier());
        dst.setFlightNo(src.getFlightNo());
        dst.setOnBoardDt(src.getOnBoardDt());
        dst.setOnBoardTm(src.getOnBoardTm());
        dst.setArrivalDt(src.getArrivalDt());
        dst.setArrivalTm(src.getArrivalTm());
    }

    private void copyAirChargeFields(HouseBlAirChargeJpaEntity src, HouseBlAirChargeJpaEntity dst) {
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
