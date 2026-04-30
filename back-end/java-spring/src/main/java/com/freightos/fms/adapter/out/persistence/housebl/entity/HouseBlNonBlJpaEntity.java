package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import com.freightos.fms.domain.housebl.entity.HouseBlNonBl;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — House B/L Non-B/L 확장.
 * HouseBlJpaEntity 와 @OneToOne(FK: house_bl_id) 관계.
 */
@Entity
@Table(name = "house_bl_non_bl")
@Getter
@NoArgsConstructor
public class HouseBlNonBlJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_non_bl_id", updatable = false, nullable = false)
    private Long houseBlNonBlId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false, unique = true)
    private HouseBlJpaEntity houseBl;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_bl_id", referencedColumnName = "house_bl_id", insertable = false, updatable = false)
    private HouseBlDescJpaEntity desc;

    @Column(name = "work_division", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private HouseBlNonBl.WorkDivision workDivision;

    @Column(name = "original_bl_ref", length = 50)
    private String originalBlRef;

    @Column(name = "rton", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal rton;

    @Column(name = "volume_wt_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal volumeWtKg;

    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setWorkDivision(HouseBlNonBl.WorkDivision v) { this.workDivision = v; }
    public void setOriginalBlRef(String v) { this.originalBlRef = v; }
    public void setRton(BigDecimal v) { this.rton = v; }
    public void setVolumeWtKg(BigDecimal v) { this.volumeWtKg = v; }
}
