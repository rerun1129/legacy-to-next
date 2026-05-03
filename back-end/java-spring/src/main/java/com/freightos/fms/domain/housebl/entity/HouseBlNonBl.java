package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.common.enums.Bound;
import com.freightos.fms.domain.common.vo.BlNumber;
import com.freightos.fms.domain.common.vo.Rton;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-24 House B/L Non-B/L 확장.
 * PRD §S-08: Work Division 값에 따라 확장 컴포넌트가 재조립된다.
 *   Sea → E-10 발췌 / Air → E-11 발췌 / Warehouse → E-24 고유 / Trucking → E-20 발췌
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlNonBl extends HouseBl {

    /**
     * PRD §4.5: Work Division — Sea / Air / Warehouse / Trucking.
     * 미선택 시 저장 차단. JOB_DIV(운송 모드)와 다른 계층의 개념.
     */
    private WorkDivision workDivision;
    private BlNumber originalBlRef;
    private Rton rton;
    private Weight volumeWtKg;

    // Schedule 확장 필드 (Non B/L 전용)
    private String linerCode;
    private String linerName;
    private String vesselName;
    private String voyageNo;
    private String finalDestCode;
    private String finalDestName;
    private String finalEta;   // 형식: "yyyyMMdd"

    protected HouseBlNonBl(WorkDivision workDivision, Bound bound) {
        super(JobDiv.NON_BL, bound);
        this.workDivision = workDivision;
    }

    public static HouseBlNonBl create(WorkDivision workDivision, Bound bound) {
        return new HouseBlNonBl(workDivision, bound);
    }

    public void updateNonBlFields(BlNumber originalBlRef, Rton rton, Weight volumeWtKg) {
        this.originalBlRef = originalBlRef;
        this.rton          = rton;
        this.volumeWtKg    = volumeWtKg;
    }

    public void updateScheduleFields(String linerCode, String linerName, String vesselName,
                                     String voyageNo, String finalDestCode, String finalDestName,
                                     String finalEta) {
        this.linerCode      = linerCode;
        this.linerName      = linerName;
        this.vesselName     = vesselName;
        this.voyageNo       = voyageNo;
        this.finalDestCode  = finalDestCode;
        this.finalDestName  = finalDestName;
        this.finalEta       = finalEta;
    }

    public enum WorkDivision {
        SEA, AIR, WAREHOUSE, TRUCKING
    }
}
