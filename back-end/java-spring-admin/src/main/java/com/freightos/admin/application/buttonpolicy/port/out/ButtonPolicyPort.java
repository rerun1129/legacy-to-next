package com.freightos.admin.application.buttonpolicy.port.out;

import com.freightos.admin.common.security.ButtonEvalRow;

import java.util.List;

public interface ButtonPolicyPort {
    /** ButtonService: 버튼 삭제 전 정책 참조 여부 확인. */
    boolean existsByButtonId(Long buttonId);

    /** AttributeDefinitionService: 속성키 삭제 전 정책 참조 여부 확인. */
    boolean existsByAttributeKey(String attributeKey);

    /** ABAC 평가용 — active 버튼과 그 정책을 한번에 조회한다. */
    List<ButtonEvalRow> findAllActiveForEvaluation();
}
