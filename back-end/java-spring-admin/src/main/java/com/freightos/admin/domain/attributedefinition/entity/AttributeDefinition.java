package com.freightos.admin.domain.attributedefinition.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * String PK 도메인 — BaseEntity 상속 불가. 자체 audit 필드 보유.
 */
@Getter
public class AttributeDefinition {

    private final String attributeKey;
    private String name;
    private String description;
    private ValueType valueType;
    private Boolean active;
    private Boolean allowMulti;

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private AttributeDefinition(String attributeKey, String name, String description, ValueType valueType, Boolean active, Boolean allowMulti) {
        this.attributeKey = attributeKey;
        this.name         = name;
        this.description  = description;
        this.valueType    = valueType;
        this.active       = active;
        this.allowMulti   = allowMulti;
    }

    public static AttributeDefinition create(String attributeKey, String name, String description, ValueType valueType, Boolean active, Boolean allowMulti) {
        return new AttributeDefinition(attributeKey, name, description, valueType, active, allowMulti);
    }

    /** 표시 필드만 갱신. 식별 필드(attributeKey)는 변경 불가. */
    public void applyUpdate(String name, String description, ValueType valueType, Boolean active, Boolean allowMulti) {
        this.name        = name;
        this.description = description;
        this.valueType   = valueType;
        this.active      = active;
        this.allowMulti  = allowMulti;
    }

    /** 어댑터 계층이 JPA→Domain 변환 시 감사 필드를 주입할 때 사용한다. */
    public void assignAudit(Long id, LocalDateTime createdAt, LocalDateTime updatedAt,
                            String createdBy, String updatedBy) {
        this.id        = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
