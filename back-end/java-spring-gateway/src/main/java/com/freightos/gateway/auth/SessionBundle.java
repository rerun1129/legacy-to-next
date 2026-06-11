package com.freightos.gateway.auth;

import java.util.List;
import java.util.Map;

/**
 * Redis 세션 번들 — admin 로그인 시 기록되는 JSON 구조와 정확히 일치해야 한다.
 * {"username": "...", "authorities": "<CSV>", "attr": { Map<String,List<String>> }}
 */
public record SessionBundle(
        String username,
        String authorities,
        Map<String, List<String>> attr
) {}
