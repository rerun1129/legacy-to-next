package com.freightos.bms.application.enums.port.out;

import com.freightos.bms.application.enums.projection.EnumOption;

import java.util.List;
import java.util.Optional;

public interface CommonCodeReadPort {

    Optional<List<EnumOption>> findByGroupCode(String groupCode);
}
