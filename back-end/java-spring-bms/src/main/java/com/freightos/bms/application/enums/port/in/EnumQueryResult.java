package com.freightos.bms.application.enums.port.in;

import com.freightos.bms.application.enums.projection.EnumOption;

import java.util.List;
import java.util.Map;

public record EnumQueryResult(Map<String, List<EnumOption>> found, List<String> notFound) {}
