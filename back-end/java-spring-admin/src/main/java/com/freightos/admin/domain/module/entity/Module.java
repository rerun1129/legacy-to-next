package com.freightos.admin.domain.module.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * String PK 도메인 — BaseEntity 상속 불가. 자체 audit 필드 보유.
 */
@Getter
public class Module {

    private final String moduleCode;
    private String name;
    private String description;
    private Integer sortOrder;
    private Boolean active;

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private Module(String moduleCode, String name, String description, Integer sortOrder, Boolean active) {
        this.moduleCode  = moduleCode;
        this.name        = name;
        this.description = description;
        this.sortOrder   = sortOrder;
        this.active      = active;
    }

    public static Module create(String moduleCode, String name, String description, Integer sortOrder, Boolean active) {
        return new Module(moduleCode, name, description, sortOrder, active);
    }

    /** 표시 필드만 갱신. 식별 필드(moduleCode)는 변경 불가. */
    public void applyUpdate(String name, String description, Integer sortOrder, Boolean active) {
        this.name        = name;
        this.description = description;
        this.sortOrder   = sortOrder;
        this.active      = active;
    }

    /** 어댑터 계층이 JPA→Domain 변환 시 식별자 및 감사 필드를 주입할 때 사용한다. */
    public void assignAudit(Long id, LocalDateTime createdAt, LocalDateTime updatedAt,
                            String createdBy, String updatedBy) {
        this.id        = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
