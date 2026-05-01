package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import com.freightos.fms.domain.common.enums.WeightUnit;
import com.freightos.fms.domain.housebl.enums.LoadType;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.fms.domain.housebl.enums.ServiceTerm;
import jakarta.persistence.*;
import java.math.BigDecimal;
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

    @Column(name = "no_of_bl", length = 10)
    @Enumerated(EnumType.STRING)
    private NoOfBl noOfBl;

    @Column(name = "issue_place", length = 50)
    private String issuePlace;

    @Column(name = "do_date", length = 8)
    private String doDate;

    @Column(name = "payable_at", length = 50)
    private String payableAt;

    @Column(name = "triangle", nullable = false)
    private boolean isTriangle = false;

    @Column(name = "service_term", length = 20)
    @Enumerated(EnumType.STRING)
    private ServiceTerm serviceTerm;

    @Column(name = "vessel_code", length = 20)
    private String vesselCode;

    @Column(name = "vessel_nationality", length = 50)
    private String vesselNationality;

    @Column(name = "weight_unit", length = 5)
    @Enumerated(EnumType.STRING)
    private WeightUnit weightUnit;

    @Column(name = "rton", columnDefinition = "NUMERIC(10,3)")
    private BigDecimal rton;

    @Column(name = "say_information", length = 500)
    private String sayInformation;

    @Column(name = "no_of_container_or_packages", length = 100)
    private String noOfContainerOrPackages;

    public void setHouseBl(HouseBlJpaEntity v) { this.houseBl = v; }
    public void setLoadType(LoadType v) { this.loadType = v; }
    public void setLinerCode(String v) { this.linerCode = v; }
    public void setVesselName(String v) { this.vesselName = v; }
    public void setVoyageNo(String v) { this.voyageNo = v; }
    public void setOnboardDate(String v) { this.onboardDate = v; }
    public void setPorCode(String v) { this.porCode = v; }
    public void setFinalDestCode(String v) { this.finalDestCode = v; }
    public void setIssueDate(String v) { this.issueDate = v; }
    public void setNoOfBl(NoOfBl v) { this.noOfBl = v; }
    public void setIssuePlace(String v) { this.issuePlace = v; }
    public void setDoDate(String v) { this.doDate = v; }
    public void setPayableAt(String v) { this.payableAt = v; }
    public void setIsTriangle(boolean v) { this.isTriangle = v; }
    public void setServiceTerm(ServiceTerm v) { this.serviceTerm = v; }
    public void setVesselCode(String v) { this.vesselCode = v; }
    public void setVesselNationality(String v) { this.vesselNationality = v; }
    public void setWeightUnit(WeightUnit v) { this.weightUnit = v; }
    public void setRton(BigDecimal v) { this.rton = v; }
    public void setSayInformation(String v) { this.sayInformation = v; }
    public void setNoOfContainerOrPackages(String v) { this.noOfContainerOrPackages = v; }
}
