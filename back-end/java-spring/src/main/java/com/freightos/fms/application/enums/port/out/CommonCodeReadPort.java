package com.freightos.fms.application.enums.port.out;

import com.freightos.fms.application.enums.projection.EnumOption;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 DB(admin.common_code) 조회 아웃바운드 포트.
 * groupCode = enum 단순 클래스명(또는 레지스트리 등록명).
 */
public interface CommonCodeReadPort {

    /**
     * admin 스키마 common_code 테이블에서 그룹 코드 목록을 조회한다.
     * 그룹이 존재하지 않거나 active 코드가 없으면 빈 Optional을 반환한다.
     */
    Optional<List<EnumOption>> findByGroupCode(String groupCode);
}
