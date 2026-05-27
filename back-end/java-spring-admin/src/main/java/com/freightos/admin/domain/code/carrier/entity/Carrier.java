package com.freightos.admin.domain.code.carrier.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Carrier extends BaseEntity {

    private final String carrierCode;
    private String name;
    private String nameEn;
    private CarrierType carrierType;
    private String carrierAddress;
    private String ediCode;
    private boolean active;
    private LocalDateTime deletedAt;

    private Carrier(String carrierCode, String name, String nameEn, CarrierType carrierType, String carrierAddress, String ediCode, boolean active) {
        this.carrierCode    = carrierCode;
        this.name           = name;
        this.nameEn         = nameEn;
        this.carrierType    = carrierType;
        this.carrierAddress = carrierAddress;
        this.ediCode        = ediCode;
        this.active         = active;
        this.deletedAt      = null;
    }

    public static Carrier create(String carrierCode, String name, String nameEn, CarrierType carrierType, String carrierAddress, String ediCode, boolean active) {
        return new Carrier(carrierCode, name, nameEn, carrierType, carrierAddress, ediCode, active);
    }

    /**
     * 수정 가능한 필드만 갱신. 식별 필드(carrierCode)는 변경 불가.
     */
    public void applyUpdate(String name, String nameEn, CarrierType carrierType, String carrierAddress, String ediCode, boolean active) {
        this.name           = name;
        this.nameEn         = nameEn;
        this.carrierType    = carrierType;
        this.carrierAddress = carrierAddress;
        this.ediCode        = ediCode;
        this.active         = active;
    }

    /** soft delete: 삭제 시각 기록 + 비활성화. */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.active    = false;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 deletedAt을 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
