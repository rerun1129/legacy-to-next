package com.freightos.fms.adapter.out.persistence.nonbl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.adapter.out.persistence.housebl.entity.HouseBlJpaEntity;
import com.freightos.fms.domain.common.enums.VolumeDivisor;
import com.freightos.fms.domain.nonbl.entity.HouseBlNonBl;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — House B/L Non-B/L 확장.
 * HouseBlJpaEntity 와 @OneToOne(FK: house_bl_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "house_bl_non_bl")
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

    @Column(name = "work_division", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private HouseBlNonBl.WorkDivision workDivision;

    @Column(name = "original_bl_ref", length = 50)
    private String originalBlRef;

    @Column(name = "rton", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal rton;

    @Column(name = "volume_wt_kg", columnDefinition = "NUMERIC(12,3)")
    private BigDecimal volumeWtKg;

    @Column(name = "liner_code", length = 10)
    private String linerCode;

    @Column(name = "liner_name", length = 100)
    private String linerName;

    @Column(name = "vessel_name", length = 100)
    private String vesselName;

    @Column(name = "voyage_no", length = 20)
    private String voyageNo;

    @Column(name = "final_dest_code", length = 5)
    private String finalDestCode;

    @Column(name = "final_dest_name", length = 100)
    private String finalDestName;

    @Column(name = "final_eta", length = 8)
    private String finalEta;

    @Column(name = "volume_divisor", length = 10)
    @Enumerated(EnumType.STRING)
    private VolumeDivisor volumeDivisor;

    public void setHouseBl(HouseBlJpaEntity v)                   { this.houseBl = v; }
    public void setWorkDivision(HouseBlNonBl.WorkDivision v)     { this.workDivision = v; }
    public void setOriginalBlRef(String v)                        { this.originalBlRef = v; }
    public void setRton(BigDecimal v)                             { this.rton = v; }
    public void setVolumeWtKg(BigDecimal v)                      { this.volumeWtKg = v; }
    public void setLinerCode(String v)                            { this.linerCode = v; }
    public void setLinerName(String v)                            { this.linerName = v; }
    public void setVesselName(String v)                           { this.vesselName = v; }
    public void setVoyageNo(String v)                             { this.voyageNo = v; }
    public void setFinalDestCode(String v)                        { this.finalDestCode = v; }
    public void setFinalDestName(String v)                        { this.finalDestName = v; }
    public void setFinalEta(String v)                             { this.finalEta = v; }
    public void setVolumeDivisor(VolumeDivisor v)                  { this.volumeDivisor = v; }
}
