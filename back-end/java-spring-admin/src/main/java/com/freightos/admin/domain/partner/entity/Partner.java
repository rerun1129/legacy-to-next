package com.freightos.admin.domain.partner.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Partner extends BaseEntity {

    private final String partnerCode;
    private PartnerType partnerType;
    private String name;
    private String nameEn;
    private String businessNo;
    private String representative;
    private String phone;
    private String email;
    private String address;
    private String memo;
    private boolean active;
    private LocalDateTime deletedAt;

    private Partner(String partnerCode, PartnerType partnerType, String name, String nameEn,
                    String businessNo, String representative, String phone, String email,
                    String address, String memo, boolean active) {
        this.partnerCode    = partnerCode;
        this.partnerType    = partnerType;
        this.name           = name;
        this.nameEn         = nameEn;
        this.businessNo     = businessNo;
        this.representative = representative;
        this.phone          = phone;
        this.email          = email;
        this.address        = address;
        this.memo           = memo;
        this.active         = active;
        this.deletedAt      = null;
    }

    public static Partner create(String partnerCode, PartnerType partnerType, String name, String nameEn,
                                 String businessNo, String representative, String phone, String email,
                                 String address, String memo, boolean active) {
        return new Partner(partnerCode, partnerType, name, nameEn, businessNo, representative,
                phone, email, address, memo, active);
    }

    /**
     * 수정 가능한 필드만 갱신. 식별 필드(partnerCode)는 변경 불가.
     */
    public void applyUpdate(PartnerType partnerType, String name, String nameEn,
                            String businessNo, String representative, String phone,
                            String email, String address, String memo, boolean active) {
        this.partnerType    = partnerType;
        this.name           = name;
        this.nameEn         = nameEn;
        this.businessNo     = businessNo;
        this.representative = representative;
        this.phone          = phone;
        this.email          = email;
        this.address        = address;
        this.memo           = memo;
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
