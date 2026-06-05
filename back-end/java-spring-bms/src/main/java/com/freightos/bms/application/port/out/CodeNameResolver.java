package com.freightos.bms.application.port.out;

import java.util.Collection;
import java.util.Map;

/**
 * admin·fms 마스터 테이블의 code → name 일괄 조회 아웃바운드 포트.
 * 빈 입력은 빈 맵 반환. 미존재·삭제 코드는 맵에 포함되지 않음(예외 아님).
 */
public interface CodeNameResolver {

    /**
     * customer_code → name 일괄 조회. deleted_at IS NULL 활성 거래처만 포함.
     */
    Map<String, String> findCustomerNames(Collection<String> codes);

    /**
     * team_code → name 일괄 조회. admin.team 활성 팀만 포함.
     */
    Map<String, String> findTeamNames(Collection<String> codes);

    /**
     * username(operator) → 표시명(user_eng_name) 일괄 조회. admin.admin_user 활성 사용자만 포함.
     */
    Map<String, String> findOperatorNames(Collection<String> usernames);

    /**
     * freight_code → name 일괄 조회.
     * admin.freight deleted_at IS NULL 활성 항목만 포함.
     * 미존재·삭제 코드는 맵에 포함되지 않음(예외 아님). 빈 입력은 빈 맵.
     */
    Map<String, String> findFreightNames(Collection<String> codes);

    /**
     * blType·blId → B/L 파생 정보(jobDiv·bound·blNo·etd·eta) 일괄 조회.
     * blType이 HOUSE이면 fms.house_bl, MASTER이면 fms.master_bl 에서 조회.
     * 반환 맵 키 = bl_id 문자열.
     */
    Map<String, BlDerived> findBlDerived(String blType, Collection<String> blIds);
}
