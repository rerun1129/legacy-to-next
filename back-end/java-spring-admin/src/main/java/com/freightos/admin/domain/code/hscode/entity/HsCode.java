package com.freightos.admin.domain.code.hscode.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class HsCode extends BaseEntity {

    private final String hsCode;
    private String name;
    private String nameEn;
    private boolean active;
    private LocalDateTime deletedAt;

    private HsCode(String hsCode, String name, String nameEn, boolean active) {
        this.hsCode    = hsCode;
        this.name      = name;
        this.nameEn    = nameEn;
        this.active    = active;
        this.deletedAt = null;
    }

    public static HsCode create(String hsCode, String name, String nameEn, boolean active) {
        return new HsCode(hsCode, name, nameEn, active);
    }

    /**
     * 수정 가능한 필드만 갱신. 식별 필드(hsCode)는 변경 불가.
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
