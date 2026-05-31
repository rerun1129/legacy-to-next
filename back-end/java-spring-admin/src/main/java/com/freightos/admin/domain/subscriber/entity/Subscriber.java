package com.freightos.admin.domain.subscriber.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class Subscriber extends BaseEntity {

    private final String subscriberCode;
    private String name;
    private String nameEn;
    private String businessNo;
    private String representative;
    private String phone;
    private String email;
    private String memo;
    private boolean active;
    private OffsetDateTime deletedAt;

    private Subscriber(String subscriberCode, String name, String nameEn, String businessNo,
                       String representative, String phone, String email, String memo, boolean active) {
        this.subscriberCode = subscriberCode;
        this.name           = name;
        this.nameEn         = nameEn;
        this.businessNo     = businessNo;
        this.representative = representative;
        this.phone          = phone;
        this.email          = email;
        this.memo           = memo;
        this.active         = active;
        this.deletedAt      = null;
    }

    public static Subscriber create(String subscriberCode, String name, String nameEn, String businessNo,
                                    String representative, String phone, String email, String memo, boolean active) {
        return new Subscriber(subscriberCode, name, nameEn, businessNo, representative, phone, email, memo, active);
    }

    /**
     * 수정 가능한 필드만 갱신. 식별 필드(subscriberCode)는 변경 불가.
     */
    public void applyUpdate(String name, String nameEn, String businessNo,
                            String representative, String phone, String email, String memo, boolean active) {
        this.name           = name;
        this.nameEn         = nameEn;
        this.businessNo     = businessNo;
        this.representative = representative;
        this.phone          = phone;
        this.email          = email;
        this.memo           = memo;
        this.active         = active;
    }

    /** soft delete: 삭제 시각 기록 + 비활성화. */
    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
        this.active    = false;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 deletedAt을 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
