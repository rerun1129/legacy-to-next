package com.freightos.bms.application.port.out;

import java.util.Collection;
import java.util.Map;

/**
 * admin 마스터 테이블의 code → name 일괄 조회 아웃바운드 포트.
 * 빈 입력은 빈 맵 반환. 미존재·삭제 코드는 맵에 포함되지 않음(예외 아님).
 */
public interface CodeNameResolver {

    /**
     * customer_code → name 일괄 조회. deleted_at IS NULL 활성 거래처만 포함.
     */
    Map<String, String> findCustomerNames(Collection<String> codes);
}
