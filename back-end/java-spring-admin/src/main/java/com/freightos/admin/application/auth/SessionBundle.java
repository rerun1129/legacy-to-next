package com.freightos.admin.application.auth;

import java.util.List;
import java.util.Map;

/**
 * Redis 세션 번들 — 게이트웨이의 SessionBundle record와 필드명이 정확히 일치해야 한다.
 * {"username": "...", "authorities": "<CSV>", "attr": { Map<String, List<String>> }}
 */
public record SessionBundle(
        String username,
        String authorities,
        Map<String, List<String>> attr
) {}
