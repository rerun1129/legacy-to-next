package com.freightos.fms.domain.enums.port.in;

import com.freightos.fms.domain.enums.EnumOption;

import java.util.List;

public interface EnumQueryUseCase {

    /**
     * ENUM 단일 조회. 등록되지 않은 name이면 ResourceNotFoundException을 throw한다.
     */
    List<EnumOption> getByName(String name);

    /**
     * ENUM 일괄 조회. 등록되지 않은 key는 notFound 목록에 포함되고 found에서는 제외된다.
     */
    EnumQueryResult getByNames(List<String> names);
}
