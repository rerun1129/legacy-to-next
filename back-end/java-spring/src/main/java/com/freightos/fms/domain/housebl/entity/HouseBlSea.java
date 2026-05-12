package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.enums.BlType;
import com.freightos.fms.domain.housebl.enums.NoOfBl;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.common.enums.LoadType;
import com.freightos.fms.domain.common.enums.ServiceTerm;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-10 House B/L 해상 확장.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlSea extends HouseBl {

    private LoadType loadType;          // FCL / LCL / BULK
    private LinerCode linerCode;
    private VesselVoyage vesselVoyage;
    // 비즈니스 날짜
    private BlDate onboardDate;
    private PortCode porCode;           // Place of Receipt
    private PortCode finalDestCode;

    // 수출 전용
    private BlDate issueDate;
    private NoOfBl noOfBl;
    private PortCode issuePlace;

    // 수입 전용
    private BlDate doDate;

    private PortCode payableAt;
    private boolean isTriangle = false;

    private ServiceTerm serviceTerm;
    private BlType blType;              // SEA 수출만 적용
    private String vesselNationality;
    private Rton rton;
    private String sayInformation;
    private String noOfContainerOrPackages;
    private String remark;

    protected HouseBlSea(Bound bound) {
        super(JobDiv.SEA, bound);
    }

    public static HouseBlSea create(Bound bound) {
        return new HouseBlSea(bound);
    }

    public void updateSeaSchedule(LinerCode linerCode, VesselVoyage vesselVoyage, BlDate onboardDate) {
        this.linerCode    = linerCode;
        this.vesselVoyage = vesselVoyage;
        this.onboardDate  = onboardDate;
    }

    public static record SeaRouteAndFlags(
            PortCode porCode, PortCode finalDestCode,
            BlDate issueDate, NoOfBl noOfBl, PortCode issuePlace,
            BlDate doDate, PortCode payableAt,
            boolean triangle,
            LoadType loadType) {}

    public void updateSeaCargoTerms(ServiceTerm serviceTerm, Rton rton,
                                    String sayInformation, String noOfContainerOrPackages) {
        this.serviceTerm = serviceTerm;
        this.rton = rton;
        this.sayInformation = sayInformation;
        this.noOfContainerOrPackages = noOfContainerOrPackages;
    }

    public void updateBlType(BlType blType) { this.blType = blType; }

    public void updateVesselNationality(String vesselNationality) {
        this.vesselNationality = vesselNationality;
    }

    public void updateRemark(String remark) { this.remark = remark; }

    public void updateSeaRouteAndFlags(SeaRouteAndFlags f) {
        this.porCode       = f.porCode();
        this.finalDestCode = f.finalDestCode();
        this.issueDate     = f.issueDate();
        this.noOfBl        = f.noOfBl();
        this.issuePlace    = f.issuePlace();
        this.doDate        = f.doDate();
        this.payableAt     = f.payableAt();
        this.isTriangle    = f.triangle();
        this.loadType      = f.loadType();
    }
}
