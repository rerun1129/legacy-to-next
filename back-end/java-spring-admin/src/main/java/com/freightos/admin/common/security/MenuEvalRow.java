package com.freightos.admin.common.security;

import java.util.List;

/**
 * 메뉴 ABAC 평가용 데이터 묶음.
 * menuId/menuCode 와 해당 메뉴에 연결된 정책 목록을 함께 전달.
 */
public record MenuEvalRow(Long menuId, String menuCode, List<PolicyRow> policies) {}
