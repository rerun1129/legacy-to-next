package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.domain.housebl.enums.Bound;
import com.freightos.fms.domain.housebl.enums.JobDiv;
import com.freightos.fms.domain.housebl.enums.LoadType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * E-10 House B/L 해상 확장.
 * house_bl + house_bl_sea JOIN.
 */
@Entity
@Table(name = "house_bl_sea")
@DiscriminatorValue("SEA")
@PrimaryKeyJoinColumn(name = "house_bl_id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlSea extends HouseBl {

    @Column(name = "load_type", length = 10)
    @Enumerated(EnumType.STRING)
    private LoadType loadType;         // FCL / LCL / BULK

    @Column(name = "liner_code", length = 20)
    private String linerCode;

    @Column(name = "vessel_name", length = 100)
    private String vesselName;

    @Column(name = "voyage_no", length = 20)
    private String voyageNo;

    @Column(name = "onboard_date")
    private LocalDate onboardDate;

    @Column(name = "por_code", length = 10)
    private String porCode;            // Place of Receipt

    @Column(name = "final_dest_code", length = 10)
    private String finalDestCode;

    // 수출 전용
    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "no_of_bl")
    private Integer noOfBl;

    @Column(name = "issue_place", length = 50)
    private String issuePlace;

    // 수입 전용
    @Column(name = "do_date")
    private LocalDate doDate;

    // 인코텀스
    @Column(name = "incoterms", length = 10)
    private String incoterms;

    @Column(name = "payable_at", length = 50)
    private String payableAt;

    @Column(name = "triangle", nullable = false)
    private boolean triangle = false;

    @Column(name = "co_load", nullable = false)
    private boolean coLoad = false;

    @Column(name = "mbl_no", length = 50)
    private String mblNo;              // 연결된 Master B/L No (참조용)

    public static HouseBlSea create(Bound bound) {
        HouseBlSea entity = new HouseBlSea();
        // JobDiv는 discriminator에서 자동 설정되지만 명시
        return entity;
    }

    public void updateSeaSchedule(String linerCode, String vesselName, String voyageNo,
                                  LocalDate onboardDate) {
        this.linerCode  = linerCode;
        this.vesselName = vesselName;
        this.voyageNo   = voyageNo;
        this.onboardDate = onboardDate;
    }
}
