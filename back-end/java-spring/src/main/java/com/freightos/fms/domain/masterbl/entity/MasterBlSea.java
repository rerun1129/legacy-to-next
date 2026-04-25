package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.LoadType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** E-03 Master B/L 해상 확장. master_bl + master_bl_sea JOIN. */
@Entity
@Table(name = "master_bl_sea")
@DiscriminatorValue("SEA")
@PrimaryKeyJoinColumn(name = "master_bl_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlSea extends MasterBl {

    @Column(name = "load_type", length = 10)
    @Enumerated(EnumType.STRING)
    private LoadType loadType;

    @Column(name = "liner_code", length = 20) private String linerCode;
    @Column(name = "vessel_name", length = 100) private String vesselName;
    @Column(name = "voyage_no", length = 20) private String voyageNo;
    @Column(name = "onboard_date") private LocalDate onboardDate;

    @Column(name = "line_bkg_no", length = 50) private String lineBkgNo;

    // 수출 전용
    @Column(name = "issue_date") private LocalDate issueDate;

    // Container 그리드는 House B/L 소속 컨테이너의 읽기 전용 집계 뷰 — 별도 테이블 없음

    public static MasterBlSea create(Bound bound) {
        MasterBlSea e = new MasterBlSea();
        return e;
    }
}
