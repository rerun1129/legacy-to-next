package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — Master B/L Dim (치수).
 * MasterBlAirJpaEntity 와 @OneToMany(FK: master_bl_air_id) 관계 — AIR 단독 사용.
 */
@Entity
@Table(schema = "fms", name = "master_bl_dim")
@Getter
@NoArgsConstructor
@DynamicUpdate
public class MasterBlDimJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_dim_id", updatable = false, nullable = false)
    private Long masterBlDimId;

    @Column(name = "master_bl_air_id", nullable = false, insertable = false, updatable = false)
    private Long masterBlAirId;

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

    public void setMasterBlDimId(Long v)  { this.masterBlDimId = v; }
    public void setMasterBlAirId(Long v) { this.masterBlAirId = v; }
    public void setLengthCm(BigDecimal v) { this.lengthCm = v; }
    public void setWidthCm(BigDecimal v) { this.widthCm = v; }
    public void setHeightCm(BigDecimal v) { this.heightCm = v; }
    public void setQuantity(Integer v) { this.quantity = v; }
    public void setCbm(BigDecimal v) { this.cbm = v; }
    public void setVolumeWeightKg(BigDecimal v) { this.volumeWeightKg = v; }
}
