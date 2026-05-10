package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — House B/L 항공 포장 치수 명세 (E-12 AIR 전용).
 * HouseBlAirJpaEntity 와 @ManyToOne(FK: house_bl_air_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "house_bl_air_dim")
@Getter
@NoArgsConstructor
public class HouseBlAirDimJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_air_dim_id", updatable = false, nullable = false)
    private Long houseBlAirDimId;

    @Column(name = "house_bl_air_id", nullable = false, insertable = false, updatable = false)
    private Long houseBlAirId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_bl_air_id", nullable = false, insertable = false, updatable = false)
    private HouseBlAirJpaEntity air;

    @Column(name = "length_cm", columnDefinition = "NUMERIC(10,2)")
    private BigDecimal lengthCm;

    @Column(name = "width_cm", columnDefinition = "NUMERIC(10,2)")
    private BigDecimal widthCm;

    @Column(name = "height_cm", columnDefinition = "NUMERIC(10,2)")
    private BigDecimal heightCm;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "cbm", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal cbm;

    @Column(name = "volume_weight_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal volumeWeightKg;

    public void setHouseBlAirDimId(Long v) { this.houseBlAirDimId = v; }
    public void setLengthCm(BigDecimal v) { this.lengthCm = v; }
    public void setWidthCm(BigDecimal v) { this.widthCm = v; }
    public void setHeightCm(BigDecimal v) { this.heightCm = v; }
    public void setQuantity(Integer v) { this.quantity = v; }
    public void setCbm(BigDecimal v) { this.cbm = v; }
    public void setVolumeWeightKg(BigDecimal v) { this.volumeWeightKg = v; }
}
