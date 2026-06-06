package com.freightos.pms.application.pms.port.out;

import com.freightos.pms.application.pms.projection.PmsCargoRow;

import java.util.List;

/**
 * House B/L ID 목록 기반 화물 수치 일괄 조회 아웃바운드 포트.
 * 확장 테이블(sea/air/truck/non_bl) LEFT JOIN으로 fan-out 없이 조회.
 */
public interface PmsCargoQueryPort {

    List<PmsCargoRow> findCargoByHouseBlIds(List<Long> houseBlIds);
}
