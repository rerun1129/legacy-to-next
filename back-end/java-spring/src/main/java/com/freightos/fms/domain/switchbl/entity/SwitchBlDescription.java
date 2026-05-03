package com.freightos.fms.domain.switchbl.entity;

import com.freightos.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-22 Switch B/L Description 본체.
 * SwitchBl 1건에 1:1로 연결되는 독립 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SwitchBlDescription extends BaseEntity {

    private Long switchBlDescriptionId;
    private Long switchBlId;
    private String marks;
    private String natureQuantity;

    protected SwitchBlDescription(Long switchBlId) {
        this.switchBlId = switchBlId;
    }

    public static SwitchBlDescription create(Long switchBlId) {
        return new SwitchBlDescription(switchBlId);
    }

    /**
     * 매퍼가 JPA→Domain 변환 시 PK를 주입할 때 사용한다.
     * 어댑터 계층에서만 호출해야 한다.
     */
    public void assignSwitchBlDescriptionId(Long switchBlDescriptionId) {
        this.switchBlDescriptionId = switchBlDescriptionId;
    }

    public void updateContent(String marks, String natureQuantity) {
        this.marks         = marks;
        this.natureQuantity = natureQuantity;
    }
}
