package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-17 House B/L 수출면장 (EXP 전용).
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlLicense extends BaseEntity {

    private Long houseBlId;
    private String licenseNo;
    private Integer pkgQty;
    private String pkgUnit;
    private Double grossWeightKg;
    private boolean partialShipment;
    private Integer partialShipmentSeq;
    private String hsnNo;
    private int seq;

    public static HouseBlLicense create(Long houseBlId, String licenseNo, Integer pkgQty,
                                        String pkgUnit, Double grossWeightKg,
                                        boolean partialShipment, Integer partialShipmentSeq,
                                        String hsnNo, int seq) {
        HouseBlLicense license = new HouseBlLicense();
        license.houseBlId           = houseBlId;
        license.licenseNo           = licenseNo;
        license.pkgQty              = pkgQty;
        license.pkgUnit             = pkgUnit;
        license.grossWeightKg       = grossWeightKg;
        license.partialShipment     = partialShipment;
        license.partialShipmentSeq  = partialShipmentSeq;
        license.hsnNo               = hsnNo;
        license.seq                 = seq;
        return license;
    }
}
