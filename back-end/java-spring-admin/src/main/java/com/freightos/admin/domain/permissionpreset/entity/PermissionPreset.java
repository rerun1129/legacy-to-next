package com.freightos.admin.domain.permissionpreset.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 재활용 가능한 권한 묶음 템플릿 도메인 엔티티.
 * live binding 방식 — 이 preset 의 attributeValueIds 가 변경되면
 * 이 preset 을 보유한 모든 user 에 즉시 반영된다.
 */
@Getter
public class PermissionPreset extends BaseEntity {

    private String code;
    private String name;
    private String description;
    private boolean active;

    /**
     * preset 에 포함된 attribute_value id 목록.
     * attribute_value 의 PK 가 단일 id(BIGINT) 이므로 Long 으로 관리한다.
     */
    private final List<Long> attributeValueIds;

    private PermissionPreset(String code, String name, String description, boolean active, List<Long> ids) {
        this.code             = code;
        this.name             = name;
        this.description      = description;
        this.active           = active;
        this.attributeValueIds = new ArrayList<>(ids);
    }

    public static PermissionPreset create(String code, String name, String description, boolean active) {
        PermissionPresetCodeValidator.validate(code);
        return new PermissionPreset(code, name, description, active, Collections.emptyList());
    }

    /** 어댑터 매퍼가 JPA→Domain 복원 시 attributeValueIds 포함 생성에 사용한다. */
    public static PermissionPreset restore(String code, String name, String description, boolean active, List<Long> ids) {
        return new PermissionPreset(code, name, description, active, ids);
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

    public List<Long> getAttributeValueIds() {
        return Collections.unmodifiableList(attributeValueIds);
    }
}
