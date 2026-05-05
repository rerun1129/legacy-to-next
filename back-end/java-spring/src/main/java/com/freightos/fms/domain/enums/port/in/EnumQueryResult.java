package com.freightos.fms.domain.enums.port.in;

import com.freightos.fms.domain.enums.EnumOption;

import java.util.List;
import java.util.Map;

public record EnumQueryResult(Map<String, List<EnumOption>> found, List<String> notFound) {}
