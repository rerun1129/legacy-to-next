package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import com.freightos.fms.domain.housebl.enums.LoadType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — Master B/L 해상 확장.
 * MasterBlJpaEntity 와 @OneToOne(FK: master_bl_id) 관계.
 */
@Entity
@Table(name = "master_bl_sea")
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

    public void setMasterBl(MasterBlJpaEntity v) { this.masterBl = v; }
    public void setLoadType(LoadType v) { this.loadType = v; }
    public void setLinerCode(String v) { this.linerCode = v; }
    public void setVesselName(String v) { this.vesselName = v; }
    public void setVoyageNo(String v) { this.voyageNo = v; }
    public void setOnboardDate(String v) { this.onboardDate = v; }
    public void setLineBkgNo(String v) { this.lineBkgNo = v; }
    public void setIssueDate(String v) { this.issueDate = v; }
}
