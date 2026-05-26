package com.freightos.admin.domain.code.packageunit.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PackageUnit extends BaseEntity {

    private final String packageCode;
    private String name;
    private String nameEn;
    private boolean active;
    private LocalDateTime deletedAt;

    private PackageUnit(String packageCode, String name, String nameEn, boolean active) {
        this.packageCode = packageCode;
        this.name        = name;
        this.nameEn      = nameEn;
        this.active      = active;
        this.deletedAt   = null;
    }

    public static PackageUnit create(String packageCode, String name, String nameEn, boolean active) {
        return new PackageUnit(packageCode, name, nameEn, active);
    }

    /**
     * 수정 가능한 필드만 갱신. 식별 필드(packageCode)는 변경 불가.
     */
    public void applyUpdate(String name, String nameEn, boolean active) {
        this.name   = name;
        this.nameEn = nameEn;
        this.active = active;
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
