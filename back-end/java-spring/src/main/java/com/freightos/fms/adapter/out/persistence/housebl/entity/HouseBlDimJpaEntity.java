package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — E-12 House B/L 치수 (항공 Dimension 그리드).
 */
@Entity
@Table(name = "house_bl_dim")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlDimJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_dim_id", updatable = false, nullable = false)
    private Long houseBlDimId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false)
    private HouseBlJpaEntity houseBl;

    @Column(name = "length_cm")
    private Double lengthCm;

    @Column(name = "width_cm")
    private Double widthCm;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "cbm", columnDefinition = "NUMERIC(10,3)")
    private Double cbm;

    @Column(name = "volume_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private Double volumeWeightKg;

    @Column(name = "seq", nullable = false)
    private int seq = 1;

    public void setHouseBlDimId(Long v) { this.houseBlDimId = v; }
    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setLengthCm(Double v) { this.lengthCm = v; }
    public void setWidthCm(Double v) { this.widthCm = v; }
    public void setHeightCm(Double v) { this.heightCm = v; }
    public void setQuantity(Integer v) { this.quantity = v; }
    public void setCbm(Double v) { this.cbm = v; }
    public void setVolumeWeightKg(Double v) { this.volumeWeightKg = v; }
    public void setSeq(int v) { this.seq = v; }
}
