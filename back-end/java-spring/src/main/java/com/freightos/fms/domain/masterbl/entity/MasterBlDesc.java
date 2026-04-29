package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-06 Master B/L 설명 (Mark & Description).
 * B/L 본문 텍스트 — 해상은 "Description", 항공은 "Nature & Quantity of Goods" 라벨.
 * MasterBl과 1:1 관계.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlDesc extends BaseEntity {

    private Long masterBlId;

    // Marks and Numbers — 2단 분할 텍스트 (좌/우)
    private String marksLeft;
    private String marksRight;

    // Description / Nature & Quantity of Goods — 2단 분할 텍스트 (좌/우)
    private String descLeft;
    private String descRight;

    private MasterBlDesc(Long masterBlId) {
        this.masterBlId = masterBlId;
    }

    public static MasterBlDesc create(Long masterBlId) {
        return new MasterBlDesc(masterBlId);
    }

    public void updateFields(String marksLeft, String marksRight,
                             String descLeft, String descRight) {
        this.marksLeft  = marksLeft;
        this.marksRight = marksRight;
        this.descLeft   = descLeft;
        this.descRight  = descRight;
    }
}
