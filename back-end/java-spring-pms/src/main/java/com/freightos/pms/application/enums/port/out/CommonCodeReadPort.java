package com.freightos.pms.application.enums.port.out;

import com.freightos.pms.application.enums.projection.EnumOption;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 DB(admin.common_code) 조회 아웃바운드 포트.
 */
public interface CommonCodeReadPort {

    Optional<List<EnumOption>> findByGroupCode(String groupCode);
}
