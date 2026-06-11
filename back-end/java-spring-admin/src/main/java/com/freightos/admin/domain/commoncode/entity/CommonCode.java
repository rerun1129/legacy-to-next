package com.freightos.admin.domain.commoncode.entity;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 공통코드 도메인 엔티티.
 * code = enum.name() (SSOT).
 * label/labelKo = DB SSOT (관리 화면에서 수정 가능).
 */
@Getter
public class CommonCode {

    private Long id;
    private final String groupCode;
    private final String code;
    private String label;
    private String labelKo;
    private Integer sortOrder;
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    private CommonCode(String groupCode, String code, String label, String labelKo,
                       Integer sortOrder, Boolean active) {
        this.groupCode = groupCode;
        this.code      = code;
        this.label     = label;
        this.labelKo   = labelKo;
        this.sortOrder = sortOrder;
        this.active    = active;
    }

    public static CommonCode create(String groupCode, String code, String label, String labelKo,
                                    Integer sortOrder, Boolean active) {
        return new CommonCode(groupCode, code, label, labelKo, sortOrder, active);
    }

    /** label·labelKo·sortOrder·active 갱신. groupCode·code(PK 구성요소)는 변경 불가. */
    public void applyUpdate(String label, String labelKo, Integer sortOrder, Boolean active) {
        this.label     = label;
        this.labelKo   = labelKo;
        this.sortOrder = sortOrder;
        this.active    = active;
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
