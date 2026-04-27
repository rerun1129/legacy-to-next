package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.LoadType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * E-10 House B/L 해상 확장.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlSea extends HouseBl {

    private LoadType loadType;         // FCL / LCL / BULK
    private String linerCode;
    private String vesselName;
    private String voyageNo;
    private LocalDate onboardDate;
    private String porCode;            // Place of Receipt
    private String finalDestCode;

    // 수출 전용
    private LocalDate issueDate;
    private Integer noOfBl;
    private String issuePlace;

    // 수입 전용
    private LocalDate doDate;

    // 인코텀스
    private String incoterms;
    private String payableAt;
    private boolean isTriangle = false;
    private boolean isCoLoad = false;
    private String mblNo;              // 연결된 Master B/L No (참조용)

    protected HouseBlSea(Bound bound) {
        super(JobDiv.SEA, bound);
    }

    public static HouseBlSea create(Bound bound) {
        return new HouseBlSea(bound);
    }

    public void updateSeaSchedule(String linerCode, String vesselName, String voyageNo,
                                  LocalDate onboardDate) {
        this.linerCode   = linerCode;
        this.vesselName  = vesselName;
        this.voyageNo    = voyageNo;
        this.onboardDate = onboardDate;
    }

    public void updateSeaRouteAndFlags(String porCode, String finalDestCode,
                                       LocalDate issueDate, Integer noOfBl, String issuePlace,
                                       LocalDate doDate, String incoterms, String payableAt,
                                       boolean triangle, boolean coLoad, String mblNo,
                                       LoadType loadType) {
        this.porCode       = porCode;
        this.finalDestCode = finalDestCode;
        this.issueDate     = issueDate;
        this.noOfBl        = noOfBl;
        this.issuePlace    = issuePlace;
        this.doDate        = doDate;
        this.incoterms     = incoterms;
        this.payableAt     = payableAt;
        this.triangle      = triangle;
        this.coLoad        = coLoad;
        this.mblNo         = mblNo;
        this.loadType      = loadType;
    }
}
