package com.freightos.bms.application.enums.port.in;

import com.freightos.bms.application.enums.projection.EnumOption;

import java.util.List;

public interface EnumQueryUseCase {

    List<EnumOption> getByName(String name);

    EnumQueryResult getByNames(List<String> names);
}
