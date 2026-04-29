package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-18 House B/L 참고번호 (Reference No).
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlReference extends BaseEntity {

    private Long houseBlId;
    private String referenceType;
    private String referenceNo;
    private int seq;

    public static HouseBlReference create(Long houseBlId, String referenceType,
                                          String referenceNo, int seq) {
        HouseBlReference ref = new HouseBlReference();
        ref.houseBlId      = houseBlId;
        ref.referenceType  = referenceType;
        ref.referenceNo    = referenceNo;
        ref.seq            = seq;
        return ref;
    }
}
