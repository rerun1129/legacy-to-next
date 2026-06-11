package com.freightos.admin.application.commoncode.port.out;

import com.freightos.admin.application.commoncode.projection.CommonCodeGroupSummary;
import com.freightos.admin.application.commoncode.projection.CommonCodeSummary;
import com.freightos.admin.domain.commoncode.entity.CommonCode;

import java.util.List;
import java.util.Optional;

public interface CommonCodePort {
    boolean existsGroupByGroupCode(String groupCode);
    List<CommonCodeGroupSummary> findAllGroupSummaries();
    List<CommonCodeSummary> findCodeSummariesByGroupCode(String groupCode);
    Optional<CommonCode> findCommonCodeById(Long id);
    boolean existsByGroupCodeAndCode(String groupCode, String code);
    void saveCommonCode(CommonCode commonCode);
    void updateCommonCodeById(Long id, CommonCode patchData);

    /** 해당 그룹의 active 코드를 sort_order 오름차순으로 조회 (Redis write-through 페이로드 생성용). */
    List<CommonCodeSummary> findActiveCodeSummariesByGroupCodeOrdered(String groupCode);
}
