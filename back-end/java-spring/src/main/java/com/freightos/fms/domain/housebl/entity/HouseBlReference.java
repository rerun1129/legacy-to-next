package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-18 House B/L 참조 번호 (레퍼런스).
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlReference extends BaseEntity {

    private Long houseBlReferenceId;
    private Long houseBlId;
    private String referenceType;
    private String referenceNo;
    private int seq;

    public static HouseBlReference create(Long houseBlId, String referenceType,
                                          String referenceNo, int seq) {
        HouseBlReference r = new HouseBlReference();
        r.houseBlId     = houseBlId;
        r.referenceType = referenceType;
        r.referenceNo   = referenceNo;
        r.seq           = seq;
        return r;
    }

    public void updateDetails(String referenceType, String referenceNo) {
        this.referenceType = referenceType;
        this.referenceNo   = referenceNo;
    }
}
