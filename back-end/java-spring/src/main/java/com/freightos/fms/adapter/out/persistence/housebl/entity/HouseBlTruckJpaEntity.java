package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA ORM 엔티티 — House B/L 트럭 확장.
 * PRD §S-06: Vessel/Voyage는 "TRUCK" 고정값으로 저장.
 * HouseBlJpaEntity 와 @OneToOne(FK: house_bl_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "house_bl_truck")
@Getter
@NoArgsConstructor
public class HouseBlTruckJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_truck_id", updatable = false, nullable = false)
    private Long houseBlTruckId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false, unique = true)
    private HouseBlJpaEntity houseBl;

    @Column(name = "vessel_name", length = 10, nullable = false)
    private String vesselName = "TRUCK";

    @Column(name = "voyage_no", length = 20)
    private String voyageNo;

    @Column(name = "pickup_date", length = 8)
    private String pickupDate;

    @Column(name = "pickup_tm", length = 4)
    private String pickupTm;

    @Column(name = "etd_tm", length = 4)
    private String etdTm;

    @Column(name = "eta_tm", length = 4)
    private String etaTm;

    @Column(name = "trucker_code", length = 20)
    private String truckerCode;

    @Column(name = "trucker_pic", length = 100)
    private String truckerPic;

    @Column(name = "charge_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal chargeWeightKg;

    @Column(name = "load_type", length = 10)
    @Enumerated(EnumType.STRING)
    private LoadType loadType;

    @Column(name = "service_term", length = 15)
    @Enumerated(EnumType.STRING)
    private ServiceTerm serviceTerm;

    // TRUCK 전용 오더 그리드. house_bl_truck_id FK로 소유.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @JoinColumn(name = "house_bl_truck_id", nullable = false, updatable = false)
    private List<HouseBlTruckOrderJpaEntity> truckOrders = new ArrayList<>();

    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setVesselName(String v) { this.vesselName = v; }
    public void setVoyageNo(String v) { this.voyageNo = v; }
    public void setPickupDate(String v) { this.pickupDate = v; }
    public void setPickupTm(String v) { this.pickupTm = v; }
    public void setEtdTm(String v) { this.etdTm = v; }
    public void setEtaTm(String v) { this.etaTm = v; }
    public void setTruckerCode(String v) { this.truckerCode = v; }
    public void setTruckerPic(String v) { this.truckerPic = v; }
    public void setChargeWeightKg(BigDecimal v) { this.chargeWeightKg = v; }
    public void setLoadType(LoadType v) { this.loadType = v; }
    public void setServiceTerm(ServiceTerm v) { this.serviceTerm = v; }

    public void syncTruckOrders(List<HouseBlTruckOrderJpaEntity> newOrders) {
        this.truckOrders.clear();
        this.truckOrders.addAll(newOrders);
    }
}
