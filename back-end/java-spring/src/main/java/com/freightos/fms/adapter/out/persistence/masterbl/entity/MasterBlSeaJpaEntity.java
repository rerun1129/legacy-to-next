package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import com.freightos.fms.domain.common.enums.WeightUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — Master B/L 해상 확장.
 * MasterBlJpaEntity 와 @OneToOne(FK: master_bl_id) 관계.
 */
@Entity
@Table(schema = "fms", name = "master_bl_sea")
@Getter
@NoArgsConstructor
public class MasterBlSeaJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_sea_id", updatable = false, nullable = false)
    private Long masterBlSeaId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_bl_id", nullable = false, unique = true)
    private MasterBlJpaEntity masterBl;

    @Column(name = "load_type", length = 10)
    @Enumerated(EnumType.STRING)
    private LoadType loadType;

    @Column(name = "liner_code", length = 20)
    private String linerCode;

    @Column(name = "vessel_name", length = 100)
    private String vesselName;

    @Column(name = "voyage_no", length = 20)
    private String voyageNo;

    @Column(name = "onboard_date", length = 8)
    private String onboardDate;

    @Column(name = "line_bkg_no", length = 50)
    private String lineBkgNo;

    @Column(name = "issue_date", length = 8)
    private String issueDate;

    @Column(name = "vessel_nationality", length = 50)
    private String vesselNationality;

    @Column(name = "weight_unit", length = 5)
    @Enumerated(EnumType.STRING)
    private WeightUnit weightUnit;

    @Column(name = "service_term", length = 20)
    @Enumerated(EnumType.STRING)
    private ServiceTerm serviceTerm;

    @Column(name = "bl_type", length = 15)
    @Enumerated(EnumType.STRING)
    private BlType blType;

    @Column(name = "vessel_code", length = 20)
    private String vesselCode;

    @Column(name = "por_code", length = 10)
    private String porCode;

    @Column(name = "final_dest_code", length = 10)
    private String finalDestCode;

    @Column(name = "rton", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal rton;

    public void setMasterBlSeaId(Long v) { this.masterBlSeaId = v; }
    public void setMasterBl(MasterBlJpaEntity v) { this.masterBl = v; }
    public void setLoadType(LoadType v) { this.loadType = v; }
    public void setLinerCode(String v) { this.linerCode = v; }
    public void setVesselName(String v) { this.vesselName = v; }
    public void setVoyageNo(String v) { this.voyageNo = v; }
    public void setOnboardDate(String v) { this.onboardDate = v; }
    public void setLineBkgNo(String v) { this.lineBkgNo = v; }
    public void setIssueDate(String v) { this.issueDate = v; }
    public void setVesselNationality(String v) { this.vesselNationality = v; }
    public void setWeightUnit(WeightUnit v) { this.weightUnit = v; }
    public void setServiceTerm(ServiceTerm v) { this.serviceTerm = v; }
    public void setBlType(BlType v) { this.blType = v; }
    public void setVesselCode(String v) { this.vesselCode = v; }
    public void setPorCode(String v) { this.porCode = v; }
    public void setFinalDestCode(String v) { this.finalDestCode = v; }
    public void setRton(BigDecimal v) { this.rton = v; }
}
