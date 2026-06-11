package com.freightos.bms.application.enums.port.out;

import com.freightos.bms.application.enums.projection.EnumOption;

import java.util.List;
import java.util.Optional;

public interface CommonCodeCachePort {

    Optional<List<EnumOption>> get(String groupCode);

    void put(String groupCode, List<EnumOption> options);
}
