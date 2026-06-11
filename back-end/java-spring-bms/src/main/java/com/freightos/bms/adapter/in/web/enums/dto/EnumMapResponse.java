package com.freightos.bms.adapter.in.web.enums.dto;

import java.util.List;
import java.util.Map;

public record EnumMapResponse(
        Map<String, List<EnumOptionResponse>> enums,
        List<String> notFound
) {}
