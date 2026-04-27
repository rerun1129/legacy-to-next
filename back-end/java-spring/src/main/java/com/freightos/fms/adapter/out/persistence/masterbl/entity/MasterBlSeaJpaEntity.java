package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.domain.housebl.enums.LoadType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * JPA ORM 엔티티 — Master B/L 해상 확장.
 */
@Entity
@Table(name = "master_bl_sea")
@DiscriminatorValue("SEA")
@PrimaryKeyJoinColumn(name = "master_bl_id")
@Getter
@NoArgsConstructor
public class MasterBlSeaJpaEntity extends MasterBlJpaEntity {

    @Column(name = "load_type", length = 10)
    @Enumerated(EnumType.STRING)
    private LoadType loadType;

    @Column(name = "liner_code", length = 20)
    private String linerCode;

    @Column(name = "vessel_name", length = 100)
    private String vesselName;

    @Column(name = "voyage_no", length = 20)
    private String voyageNo;

    @Column(name = "onboard_date")
    private LocalDate onboardDate;

    @Column(name = "line_bkg_no", length = 50)
    private String lineBkgNo;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    public void setLoadType(LoadType v) { this.loadType = v; }
    public void setLinerCode(String v) { this.linerCode = v; }
    public void setVesselName(String v) { this.vesselName = v; }
    public void setVoyageNo(String v) { this.voyageNo = v; }
    public void setOnboardDate(LocalDate v) { this.onboardDate = v; }
    public void setLineBkgNo(String v) { this.lineBkgNo = v; }
    public void setIssueDate(LocalDate v) { this.issueDate = v; }
}
