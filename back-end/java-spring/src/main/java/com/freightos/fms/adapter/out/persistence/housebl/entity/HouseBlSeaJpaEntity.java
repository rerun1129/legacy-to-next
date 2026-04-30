package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import com.freightos.fms.domain.housebl.enums.LoadType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — House B/L 해상 확장.
 * HouseBlJpaEntity 와 @OneToOne(FK: house_bl_id) 관계.
 */
@Entity
@Table(name = "house_bl_sea")
@Getter
@NoArgsConstructor
public class HouseBlSeaJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_sea_id", updatable = false, nullable = false)
    private Long houseBlSeaId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_bl_id", nullable = false, unique = true)
    private HouseBlJpaEntity houseBl;

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

    @Column(name = "por_code", length = 10)
    private String porCode;

    @Column(name = "final_dest_code", length = 10)
    private String finalDestCode;

    @Column(name = "issue_date", length = 8)
    private String issueDate;

    @Column(name = "no_of_bl")
    private Integer noOfBl;

    @Column(name = "issue_place", length = 50)
    private String issuePlace;

    @Column(name = "do_date", length = 8)
    private String doDate;

    @Column(name = "incoterms", length = 10)
    private String incoterms;

    @Column(name = "payable_at", length = 50)
    private String payableAt;

    @Column(name = "triangle", nullable = false)
    private boolean isTriangle = false;

    @Column(name = "co_load", nullable = false)
    private boolean isCoLoad = false;

    @Column(name = "mbl_no", length = 50)
    private String mblNo;

    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setLoadType(LoadType v) { this.loadType = v; }
    public void setLinerCode(String v) { this.linerCode = v; }
    public void setVesselName(String v) { this.vesselName = v; }
    public void setVoyageNo(String v) { this.voyageNo = v; }
    public void setOnboardDate(String v) { this.onboardDate = v; }
    public void setPorCode(String v) { this.porCode = v; }
    public void setFinalDestCode(String v) { this.finalDestCode = v; }
    public void setIssueDate(String v) { this.issueDate = v; }
    public void setNoOfBl(Integer v) { this.noOfBl = v; }
    public void setIssuePlace(String v) { this.issuePlace = v; }
    public void setDoDate(String v) { this.doDate = v; }
    public void setIncoterms(String v) { this.incoterms = v; }
    public void setPayableAt(String v) { this.payableAt = v; }
    public void setIsTriangle(boolean v) { this.isTriangle = v; }
    public void setIsCoLoad(boolean v) { this.isCoLoad = v; }
    public void setMblNo(String v) { this.mblNo = v; }
}
