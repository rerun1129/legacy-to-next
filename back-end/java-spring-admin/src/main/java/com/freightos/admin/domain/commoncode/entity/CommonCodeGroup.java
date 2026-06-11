package com.freightos.admin.domain.commoncode.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공통코드 그룹 도메인 엔티티.
 * group_code = enum 단순클래스명 (예: 'JobDiv', 'Bound', 'housebl.JobDiv').
 * source_module = 원천 모듈 ('FMS' | 'BMS' | 'PMS').
 */
@Getter
public class CommonCodeGroup {

    private Long id;
    private final String groupCode;
    private final String sourceModule;
    private String description;
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private CommonCodeGroup(String groupCode, String sourceModule, String description, Boolean active) {
        this.groupCode    = groupCode;
        this.sourceModule = sourceModule;
        this.description  = description;
        this.active       = active;
    }

    public static CommonCodeGroup create(String groupCode, String sourceModule, String description, Boolean active) {
        return new CommonCodeGroup(groupCode, sourceModule, description, active);
    }

    /** 어댑터 계층이 JPA→Domain 변환 시 identity 및 감사 필드를 주입한다. */
    public void assignIdentity(Long id, LocalDateTime createdAt, LocalDateTime updatedAt,
                               String createdBy, String updatedBy) {
        this.id        = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }
}
