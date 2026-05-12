package com.freightos.fms.domain.masterbl.entity;

import com.freightos.common.entity.BaseEntity;
import com.freightos.fms.domain.common.enums.DescClause1;
import com.freightos.fms.domain.common.enums.DescClause2;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-06 Master B/L Description (화물 표시 및 명세).
 * Master B/L 당 1건(1:1) — 순수 도메인 엔티티, JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlDesc extends BaseEntity {

    private Long masterBlDescId;
    private Long masterBlId;
    private String marks;
    private String description;
    private DescClause1 descClause1;
    private DescClause2 descClause2;

    public static MasterBlDesc create(Long masterBlId) {
        MasterBlDesc d = new MasterBlDesc();
        d.masterBlId = masterBlId;
        return d;
    }

    public void updateContent(String marks, String description,
                              DescClause1 descClause1, DescClause2 descClause2) {
        this.marks       = marks;
        this.description = description;
        this.descClause1 = descClause1;
        this.descClause2 = descClause2;
    }
}
