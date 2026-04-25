package com.freightos.fms.domain.housebl.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-24 House B/L Non-B/L 확장.
 * house_bl + house_bl_non_bl JOIN.
 * PRD §S-08: Work Division 값에 따라 확장 컴포넌트가 재조립된다.
 *   Sea → E-10 발췌 / Air → E-11 발췌 / Warehouse → E-24 고유 / Trucking → E-20 발췌
 */
@Entity
@Table(name = "house_bl_non_bl")
@DiscriminatorValue("NON_BL")
@PrimaryKeyJoinColumn(name = "house_bl_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlNonBl extends HouseBl {

    /**
     * PRD §4.5: Work Division — Sea / Air / Warehouse / Trucking.
     * 미선택 시 저장 차단. JOB_DIV(운송 모드)와 다른 계층의 개념.
     */
    @Column(name = "work_division", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private WorkDivision workDivision;

    @Column(name = "settle_partner_code", length = 20)
    private String settlePartnerCode;

    @Column(name = "status", length = 20)
    private String status;              // 접수 / 처리 / 완료

    @Column(name = "original_bl_ref", length = 50)
    private String originalBlRef;       // 원본 B/L 참조번호 (선택)

    public static HouseBlNonBl create(WorkDivision workDivision) {
        HouseBlNonBl entity = new HouseBlNonBl();
        entity.workDivision = workDivision;
        entity.status       = "접수";
        return entity;
    }

    public enum WorkDivision {
        SEA, AIR, WAREHOUSE, TRUCKING
    }
}
