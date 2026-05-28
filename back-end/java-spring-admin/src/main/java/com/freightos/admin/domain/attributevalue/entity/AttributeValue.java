package com.freightos.admin.domain.attributevalue.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 단일 PK(id) + UNIQUE(attributeKey, value) 도메인 엔티티.
 * id 는 어댑터 계층이 JPA→Domain 변환 시 assignIdentity 로 주입한다.
 */
@Getter
public class AttributeValue {

    private Long id;
    private final String attributeKey;
    private final String value;
    private String label;
    private Integer sortOrder;
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private AttributeValue(String attributeKey, String value, String label, Integer sortOrder, Boolean active) {
        this.attributeKey = attributeKey;
        this.value        = value;
        this.label        = label;
        this.sortOrder    = sortOrder;
        this.active       = active;
    }

    public static AttributeValue create(String attributeKey, String value, String label, Integer sortOrder, Boolean active) {
        return new AttributeValue(attributeKey, value, label, sortOrder, active);
    }

    /** label·sortOrder·active만 갱신 가능. PK(attributeKey·value)는 변경 불가. */
    public void applyUpdate(String label, Integer sortOrder, Boolean active) {
        this.label     = label;
        this.sortOrder = sortOrder;
        this.active    = active;
    }

    /** 어댑터 계층이 JPA→Domain 변환 시 id 및 감사 필드를 주입할 때 사용한다. */
    public void assignIdentity(Long id, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, String updatedBy) {
        this.id        = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
