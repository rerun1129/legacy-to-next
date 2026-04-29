package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import com.freightos.fms.domain.common.vo.AirlineCode;
import com.freightos.fms.domain.common.vo.BlDate;
import com.freightos.fms.domain.common.vo.PortCode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-07 Master B/L Schedule Leg (경유지).
 * 항공 Master B/L 전용 — 항공 Schedule 그리드의 각 행이 하나의 Leg 레코드.
 * 직항 1행 / 1회 환승 2행 / 2회 환승 3행 구조.
 * 첫 행 onBoardDate → ETD 파생, 마지막 행 toCode/arrivalDate → POD/ETA 파생.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlScheduleLeg extends BaseEntity {

    private Long masterBlId;

    private PortCode toCode;          // 도착지 공항 코드 (필수)
    private AirlineCode byAirline;    // 항공사 코드
    private String flightNo;          // 편명
    private BlDate onBoardDate;       // 본선적재(탑재)일 (필수)
    private String onBoardTime;       // 탑재 시각 (HHmm)
    private BlDate arrivalDate;       // 도착일 (필수)
    private String arrivalTime;       // 도착 시각 (HHmm)

    // 그리드 내 표시 순서 — ETD/POD/ETA 파생 기준이 되는 첫/마지막 행 판정에 사용
    private Integer seq;

    private MasterBlScheduleLeg(Long masterBlId) {
        this.masterBlId = masterBlId;
    }

    public static MasterBlScheduleLeg create(Long masterBlId) {
        return new MasterBlScheduleLeg(masterBlId);
    }

    public void updateFields(PortCode toCode, AirlineCode byAirline, String flightNo,
                             BlDate onBoardDate, String onBoardTime,
                             BlDate arrivalDate, String arrivalTime,
                             Integer seq) {
        this.toCode      = toCode;
        this.byAirline   = byAirline;
        this.flightNo    = flightNo;
        this.onBoardDate = onBoardDate;
        this.onBoardTime = onBoardTime;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.seq         = seq;
    }
}
