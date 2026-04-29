package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.*;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import com.freightos.fms.domain.housebl.enums.LoadType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-03 Master B/L 해상 확장.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlSea extends MasterBl {

    private LoadType loadType;
    private LinerCode linerCode;
    private VesselVoyage vesselVoyage;
    // 비즈니스 날짜
    private BlDate onboardDate;
    private BlNumber lineBkgNo;

    // 수출 전용
    private BlDate issueDate;

    // Container 그리드는 House B/L 소속 컨테이너의 읽기 전용 집계 뷰 — 별도 테이블 없음

    protected MasterBlSea(Bound bound) {
        super(MasterBlJobDiv.SEA, bound);
    }

    public static MasterBlSea create(Bound bound) {
        return new MasterBlSea(bound);
    }

    public void updateSeaFields(LoadType loadType, LinerCode linerCode, VesselVoyage vesselVoyage,
                                BlDate onboardDate, BlNumber lineBkgNo, BlDate issueDate) {
        this.loadType     = loadType;
        this.linerCode    = linerCode;
        this.vesselVoyage = vesselVoyage;
        this.onboardDate  = onboardDate;
        this.lineBkgNo    = lineBkgNo;
        this.issueDate    = issueDate;
    }
}
