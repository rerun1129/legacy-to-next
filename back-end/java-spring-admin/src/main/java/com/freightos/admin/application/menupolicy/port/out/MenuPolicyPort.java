package com.freightos.admin.application.menupolicy.port.out;

import com.freightos.admin.common.security.MenuEvalRow;

import java.util.List;

public interface MenuPolicyPort {
    /** MenuService: 메뉴 삭제 전 정책 참조 여부 확인. */
    boolean existsByMenuId(Long menuId);

    /** AttributeDefinitionService: 속성키 삭제 전 정책 참조 여부 확인. */
    boolean existsByAttributeKey(String attributeKey);

    /** ABAC 평가용 — active 메뉴와 그 정책을 한번에 조회한다. */
    List<MenuEvalRow> findAllActiveForEvaluation();

    /** 특정 모듈의 active 메뉴에 설정된 attributeKey 목록을 중복 없이 반환한다 (module 키 제외). */
    List<String> findDistinctAttributeKeysByModuleCode(String moduleCode);
}
