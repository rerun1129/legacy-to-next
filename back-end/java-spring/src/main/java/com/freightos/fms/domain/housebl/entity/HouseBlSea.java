package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.LoadType;
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

    private LoadType loadType;         // FCL / LCL / BULK
    private String linerCode;
    private String vesselName;
    private String voyageNo;
    // 비즈니스 날짜 — yyyyMMdd
    private String onboardDate;
    private String porCode;            // Place of Receipt
    private String finalDestCode;

    // 수출 전용
    private String issueDate;
    private Integer noOfBl;
    private String issuePlace;

    // 수입 전용
    private String doDate;

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
                                  String onboardDate) {
        this.linerCode   = linerCode;
        this.vesselName  = vesselName;
        this.voyageNo    = voyageNo;
        this.onboardDate = onboardDate;
    }

    public static record SeaRouteAndFlags(
            String porCode, String finalDestCode,
            String issueDate, Integer noOfBl, String issuePlace,
            String doDate, String incoterms, String payableAt,
            boolean triangle, boolean coLoad, String mblNo,
            LoadType loadType) {}

    public void updateSeaRouteAndFlags(SeaRouteAndFlags f) {
        this.porCode       = f.porCode();
        this.finalDestCode = f.finalDestCode();
        this.issueDate     = f.issueDate();
        this.noOfBl        = f.noOfBl();
        this.issuePlace    = f.issuePlace();
        this.doDate        = f.doDate();
        this.incoterms     = f.incoterms();
        this.payableAt     = f.payableAt();
        this.isTriangle    = f.triangle();
        this.isCoLoad      = f.coLoad();
        this.mblNo         = f.mblNo();
        this.loadType      = f.loadType();
    }
}
