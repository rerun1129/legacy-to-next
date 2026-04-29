package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-13 House B/L 설명 (Marks / Description / Clause / Remark).
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlDesc extends BaseEntity {

    private Long houseBlId;
    private String marks;
    private String description;
    private String descClause1;
    private String remark;

    public static HouseBlDesc create(Long houseBlId) {
        HouseBlDesc desc = new HouseBlDesc();
        desc.houseBlId = houseBlId;
        return desc;
    }

    public void updateContent(String marks, String description, String descClause1, String remark) {
        this.marks       = marks;
        this.description = description;
        this.descClause1 = descClause1;
        this.remark      = remark;
    }
}
