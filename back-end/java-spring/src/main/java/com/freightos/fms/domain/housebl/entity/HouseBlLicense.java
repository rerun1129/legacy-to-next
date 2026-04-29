package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * E-17 House B/L 라이선스 / 패킹 명세.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlLicense extends BaseEntity {

    private Long houseBlLicenseId;
    private Long houseBlId;
    private String licenseNo;
    private Integer pkgQty;
    private String pkgUnit;
    private BigDecimal grossWeightKg;
    private String combinedPackingMark;
    private Integer combinedPackingQty;
    private String combinedPackingUnit;
    private boolean partialShipment;
    private Integer partialShipmentSeq;
    private String hsnNo;
    private int seq;

    public static HouseBlLicense create(Long houseBlId, int seq) {
        HouseBlLicense l = new HouseBlLicense();
        l.houseBlId = houseBlId;
        l.seq       = seq;
        return l;
    }

    public void updateDetails(String licenseNo, Integer pkgQty, String pkgUnit,
                              BigDecimal grossWeightKg, String combinedPackingMark,
                              Integer combinedPackingQty, String combinedPackingUnit,
                              boolean partialShipment, Integer partialShipmentSeq, String hsnNo) {
        this.licenseNo            = licenseNo;
        this.pkgQty               = pkgQty;
        this.pkgUnit              = pkgUnit;
        this.grossWeightKg        = grossWeightKg;
        this.combinedPackingMark  = combinedPackingMark;
        this.combinedPackingQty   = combinedPackingQty;
        this.combinedPackingUnit  = combinedPackingUnit;
        this.partialShipment      = partialShipment;
        this.partialShipmentSeq   = partialShipmentSeq;
        this.hsnNo                = hsnNo;
    }
}
