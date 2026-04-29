package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — Master B/L 치수 (E-05).
 * MasterBlJpaEntity 와 @ManyToOne(FK: master_bl_id) 관계.
 * 항공 Master B/L에서만 사용되는 Dimension 그리드 행.
 */
@Entity
@Table(name = "master_bl_dim")
@Getter
@NoArgsConstructor
public class MasterBlDimJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_dim_id", updatable = false, nullable = false)
    private Long masterBlDimId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_bl_id", nullable = false)
    private MasterBlJpaEntity masterBl;

    @Column(name = "length", columnDefinition = "NUMERIC(10,2)")
    private BigDecimal length;

    @Column(name = "width", columnDefinition = "NUMERIC(10,2)")
    private BigDecimal width;

    @Column(name = "height", columnDefinition = "NUMERIC(10,2)")
    private BigDecimal height;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "cbm", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal cbm;

    @Column(name = "volume_wt", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal volumeWt;

    @Column(name = "divisor", length = 20)
    private String divisor;

    @Column(name = "seq")
    private Integer seq;

    public void setMasterBl(MasterBlJpaEntity v) { this.masterBl = v; }
    public void setLength(BigDecimal v) { this.length = v; }
    public void setWidth(BigDecimal v) { this.width = v; }
    public void setHeight(BigDecimal v) { this.height = v; }
    public void setQty(Integer v) { this.qty = v; }
    public void setCbm(BigDecimal v) { this.cbm = v; }
    public void setVolumeWt(BigDecimal v) { this.volumeWt = v; }
    public void setDivisor(String v) { this.divisor = v; }
    public void setSeq(Integer v) { this.seq = v; }
}
