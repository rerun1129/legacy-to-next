package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-13 House B/L 화물 표시 및 품목 설명.
 * HouseBl 당 1건 (1:1). 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlDesc extends BaseEntity {

    private Long houseBlDescId;
    private Long houseBlId;
    private String marks;
    private String description;
    private String descClause1;
    private String descClause2;
    private String remark;

    public static HouseBlDesc create(Long houseBlId) {
        HouseBlDesc d = new HouseBlDesc();
        d.houseBlId = houseBlId;
        return d;
    }

    public void updateContent(String marks, String description,
                              String descClause1, String descClause2, String remark) {
        this.marks       = marks;
        this.description = description;
        this.descClause1 = descClause1;
        this.descClause2 = descClause2;
        this.remark      = remark;
    }
}
