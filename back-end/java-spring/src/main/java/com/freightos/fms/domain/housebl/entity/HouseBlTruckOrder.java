package com.freightos.fms.domain.housebl.entity;

import com.freightos.common.entity.BaseEntity;
import com.freightos.fms.domain.common.vo.ContainerNumber;
import com.freightos.fms.domain.common.vo.SealNumber;
import com.freightos.fms.domain.common.vo.Volume;
import com.freightos.fms.domain.common.vo.Weight;
import com.freightos.fms.domain.housebl.enums.ContainerType;
import com.freightos.fms.domain.housebl.enums.TruckType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-20 House B/L 트럭 오더 행.
 * TRUCK 모드에서만 채워지는 1:N 그리드 엔티티.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlTruckOrder extends BaseEntity {

    private Long houseBlId;
    private String truckOrderNo;
    private Integer pkgQty;
    private String pkgUnit;
    private Weight grossWeightKg;
    private Volume cbm;
    private String truckNo;
    private TruckType truckType;
    private String driver;
    private String mobileNo;
    private ContainerNumber containerNo;
    private ContainerType containerType;
    private SealNumber sealNo1;
    private SealNumber sealNo2;
    private SealNumber sealNo3;

    public static HouseBlTruckOrder create(Long houseBlId) {
        HouseBlTruckOrder o = new HouseBlTruckOrder();
        o.houseBlId = houseBlId;
        return o;
    }

    public record Details(
            String truckOrderNo,
            Integer pkgQty,
            String pkgUnit,
            Weight grossWeightKg,
            Volume cbm,
            String truckNo,
            TruckType truckType,
            String driver,
            String mobileNo,
            ContainerNumber containerNo,
            ContainerType containerType,
            SealNumber sealNo1,
            SealNumber sealNo2,
            SealNumber sealNo3
    ) {}

    public void updateDetails(Details d) {
        this.truckOrderNo  = d.truckOrderNo();
        this.pkgQty        = d.pkgQty();
        this.pkgUnit       = d.pkgUnit();
        this.grossWeightKg = d.grossWeightKg();
        this.cbm           = d.cbm();
        this.truckNo       = d.truckNo();
        this.truckType     = d.truckType();
        this.driver        = d.driver();
        this.mobileNo      = d.mobileNo();
        this.containerNo   = d.containerNo();
        this.containerType = d.containerType();
        this.sealNo1       = d.sealNo1();
        this.sealNo2       = d.sealNo2();
        this.sealNo3       = d.sealNo3();
    }
}
