package com.freightos.admin.application.auth.projection;

import java.util.List;
import java.util.Map;

public record MeProjection(
    Long id,
    String username,
    String email,
    Map<String, List<String>> attributes,
    List<String> accessibleMenus,
    List<String> accessibleButtons
) {}
