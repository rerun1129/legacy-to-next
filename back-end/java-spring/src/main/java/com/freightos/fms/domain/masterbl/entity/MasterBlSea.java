package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.housebl.enums.LoadType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * E-03 Master B/L 해상 확장.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlSea extends MasterBl {

    private LoadType loadType;
    private String linerCode;
    private String vesselName;
    private String voyageNo;
    private LocalDate onboardDate;
    private String lineBkgNo;

    // 수출 전용
    private LocalDate issueDate;

    // Container 그리드는 House B/L 소속 컨테이너의 읽기 전용 집계 뷰 — 별도 테이블 없음

    protected MasterBlSea(Bound bound) {
        super(bound);
    }

    public static MasterBlSea create(Bound bound) {
        return new MasterBlSea(bound);
    }

    public void updateSeaFields(LoadType loadType, String linerCode, String vesselName,
                                String voyageNo, LocalDate onboardDate,
                                String lineBkgNo, LocalDate issueDate) {
        this.loadType    = loadType;
        this.linerCode   = linerCode;
        this.vesselName  = vesselName;
        this.voyageNo    = voyageNo;
        this.onboardDate = onboardDate;
        this.lineBkgNo   = lineBkgNo;
        this.issueDate   = issueDate;
    }
}
