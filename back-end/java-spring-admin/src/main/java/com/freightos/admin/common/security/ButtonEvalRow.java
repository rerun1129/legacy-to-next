package com.freightos.admin.common.security;

import java.util.List;

/**
 * 버튼 ABAC 평가용 데이터 묶음.
 * buttonId/buttonCode 와 해당 버튼에 연결된 정책 목록을 함께 전달.
 */
public record ButtonEvalRow(Long buttonId, String buttonCode, String label, String labelEn, List<PolicyRow> policies) {}
