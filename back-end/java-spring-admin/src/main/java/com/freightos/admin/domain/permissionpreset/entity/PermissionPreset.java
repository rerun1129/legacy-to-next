package com.freightos.admin.domain.permissionpreset.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 재활용 가능한 권한 묶음 템플릿 도메인 엔티티.
 * live binding 방식 — 이 preset 의 attributeValueRefs 가 변경되면
 * 이 preset 을 보유한 모든 user 에 즉시 반영된다.
 */
@Getter
public class PermissionPreset extends BaseEntity {

    private String code;
    private String name;
    private String description;
    private boolean active;

    /**
     * preset 에 포함된 attribute_value 참조 목록.
     * attribute_value 의 PK 가 (attribute_key, value) 복합키이므로 AttributeValueRef 로 관리한다.
     */
    private final List<AttributeValueRef> attributeValueRefs;

    private PermissionPreset(String code, String name, String description, boolean active, List<AttributeValueRef> refs) {
        this.code              = code;
        this.name              = name;
        this.description       = description;
        this.active            = active;
        this.attributeValueRefs = new ArrayList<>(refs);
    }

    public static PermissionPreset create(String code, String name, String description, boolean active) {
        PermissionPresetCodeValidator.validate(code);
        return new PermissionPreset(code, name, description, active, Collections.emptyList());
    }

    /** 어댑터 매퍼가 JPA→Domain 복원 시 attributeValueRefs 포함 생성에 사용한다. */
    public static PermissionPreset restore(String code, String name, String description, boolean active, List<AttributeValueRef> refs) {
        return new PermissionPreset(code, name, description, active, refs);
    }

    /** name·description·active 를 일괄 수정한다. code 변경 불가. */
    public void applyUpdate(String name, String description, boolean active) {
        this.name        = name;
        this.description = description;
        this.active      = active;
    }

    public void toggleActive(boolean active) {
        this.active = active;
    }

    public List<AttributeValueRef> getAttributeValueRefs() {
        return Collections.unmodifiableList(attributeValueRefs);
    }
}
