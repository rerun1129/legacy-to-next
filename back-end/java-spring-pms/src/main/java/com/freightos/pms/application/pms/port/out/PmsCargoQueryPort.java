package com.freightos.pms.application.pms.port.out;

import com.freightos.pms.application.pms.projection.PmsCargoRow;
import com.freightos.pms.application.pms.projection.PmsMasterDetailRow;

import java.util.List;

/**
 * House B/L ID 목록 기반 화물 수치·식별 정보 일괄 조회 아웃바운드 포트.
 * 확장 테이블(sea/air/truck/non_bl) LEFT JOIN으로 fan-out 없이 조회.
 */
public interface PmsCargoQueryPort {

    /**
     * House B/L 키 목록으로 cargo 수치 + house 식별 정보 일괄 조회.
     * 반환 행의 식별 필드(hblNo/mblNo/jobDiv 등)는 채워진 상태.
     */
    List<PmsCargoRow> findCargoByHouseBlIds(List<Long> houseBlIds);

    /**
     * Master B/L 키 목록으로 master 식별 정보 일괄 조회.
     * mblNo/jobDiv/bound/etd/eta/polCode/podCode 반환.
     */
    List<PmsMasterDetailRow> findMasterDetailByIds(List<Long> masterBlIds);
}
