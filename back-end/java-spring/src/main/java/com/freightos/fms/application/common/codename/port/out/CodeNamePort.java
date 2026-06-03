package com.freightos.fms.application.common.codename.port.out;

import java.util.Collection;
import java.util.Map;

/**
 * admin 마스터 테이블의 code → name 일괄 조회 아웃바운드 포트.
 * 빈 입력은 빈 맵 반환. 미존재·삭제 코드는 맵에 포함되지 않음(예외 아님).
 */
public interface CodeNamePort {

    Map<String, String> findCustomerNames(Collection<String> codes);

    Map<String, String> findPortNames(Collection<String> codes);

    Map<String, String> findCarrierNames(Collection<String> codes);

    /**
     * username → display name(COALESCE(user_eng_name, email)) 일괄 조회.
     * deleted_at IS NULL 활성 사용자만 포함. 두 컬럼 모두 null이면 맵에서 제외.
     */
    Map<String, String> findUserNames(Collection<String> usernames);

    /**
     * hs_code → name 일괄 조회.
     * deleted_at IS NULL 활성 코드만 포함. 미존재·삭제 코드는 맵에 포함되지 않음(예외 아님).
     */
    Map<String, String> findHsCodeNames(Collection<String> codes);

    /**
     * team_code → name 일괄 조회. active = true 활성 팀만 포함.
     * 미존재·비활성 코드는 맵에 포함되지 않음(예외 아님). 빈 입력은 빈 맵.
     */
    Map<String, String> findTeamNames(Collection<String> codes);

    /**
     * customer_code → customer_type 일괄 조회.
     * BMS 운임 라인 §6.16 FinancialDocType 자동 산정용.
     * deleted_at IS NULL 활성 고객만 포함. 미존재·삭제 코드는 맵에 포함되지 않음(예외 아님).
     */
    Map<String, String> findCustomerTypes(Collection<String> codes);
}
