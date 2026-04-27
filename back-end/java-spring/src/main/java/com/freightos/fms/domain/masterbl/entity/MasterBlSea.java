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

    public static MasterBlSea create(Bound bound) {
        MasterBlSea e = new MasterBlSea();
        return e;
    }
}
