package com.freightos.fms.common.entity;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 도메인 공통 베이스. 순수 Java 클래스 — JPA 어노테이션 없음.
 * 감사 필드(createdAt 등)는 JPA 계층인 BaseJpaEntity 에서 관리한다.
 */
@Getter
public abstract class BaseEntity {

    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
