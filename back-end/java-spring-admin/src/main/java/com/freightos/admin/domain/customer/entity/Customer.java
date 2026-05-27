package com.freightos.admin.domain.customer.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Customer extends BaseEntity {

    private final String customerCode;
    private CustomerType customerType;
    private String name;
    private String nameEn;
    private String businessNo;
    private String representative;
    private String phone;
    private String email;
    private String customerLocalAddress;
    private String customerEnglishAddress;
    private String memo;
    private boolean active;
    private LocalDateTime deletedAt;

    private Customer(String customerCode, CustomerType customerType, String name, String nameEn,
                     String businessNo, String representative, String phone, String email,
                     String customerLocalAddress, String customerEnglishAddress, String memo, boolean active) {
        this.customerCode           = customerCode;
        this.customerType           = customerType;
        this.name                   = name;
        this.nameEn                 = nameEn;
        this.businessNo             = businessNo;
        this.representative         = representative;
        this.phone                  = phone;
        this.email                  = email;
        this.customerLocalAddress   = customerLocalAddress;
        this.customerEnglishAddress = customerEnglishAddress;
        this.memo                   = memo;
        this.active                 = active;
        this.deletedAt              = null;
    }

    public static Customer create(String customerCode, CustomerType customerType, String name, String nameEn,
                                  String businessNo, String representative, String phone, String email,
                                  String customerLocalAddress, String customerEnglishAddress, String memo, boolean active) {
        return new Customer(customerCode, customerType, name, nameEn, businessNo, representative,
                phone, email, customerLocalAddress, customerEnglishAddress, memo, active);
    }

    /**
     * 수정 가능한 필드만 갱신. 식별 필드(customerCode)는 변경 불가.
     */
    public void applyUpdate(CustomerType customerType, String name, String nameEn,
                            String businessNo, String representative, String phone,
                            String email, String customerLocalAddress, String customerEnglishAddress, String memo, boolean active) {
        this.customerType           = customerType;
        this.name                   = name;
        this.nameEn                 = nameEn;
        this.businessNo             = businessNo;
        this.representative         = representative;
        this.phone                  = phone;
        this.email                  = email;
        this.customerLocalAddress   = customerLocalAddress;
        this.customerEnglishAddress = customerEnglishAddress;
        this.memo                   = memo;
        this.active                 = active;
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
