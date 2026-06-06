package com.freightos.pms.application.pms.port.out;

import java.util.Collection;
import java.util.Map;

/**
 * admin 마스터 테이블의 code → name 일괄 조회 아웃바운드 포트.
 * 빈 입력은 빈 맵 반환. 미존재·삭제 코드는 맵에 포함되지 않음(예외 아님).
 */
public interface PmsCodeNameResolver {

    /** customer_code → name. deleted_at IS NULL 활성 거래처만. */
    Map<String, String> findCustomerNames(Collection<String> codes);

    /** carrier_code → name. 운송사(liner) 코드 → 이름. */
    Map<String, String> findCarrierNames(Collection<String> codes);

    /** team_code → name. */
    Map<String, String> findTeamNames(Collection<String> codes);

    /** username → user_eng_name. */
    Map<String, String> findOperatorNames(Collection<String> usernames);
}
