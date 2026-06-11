package com.freightos.admin.application.commoncode.port.out;

import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;

import java.util.List;

/**
 * Redis write-through 포트.
 * 저장 실패는 warn 삼킴 — 소비자(FMS/BMS/PMS)는 DB 폴백으로 처리한다.
 */
public interface CommonCodeCachePort {
    void putGroupCodes(String groupCode, List<CommonCodeSummary> activeCodes);
}
