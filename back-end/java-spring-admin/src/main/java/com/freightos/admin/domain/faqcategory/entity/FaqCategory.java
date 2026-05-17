package com.freightos.admin.domain.faqcategory.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FaqCategory extends BaseEntity {

    private String name;
    private int sortOrder;
    private boolean active;
    private LocalDateTime deletedAt;

    private FaqCategory(String name, int sortOrder, boolean active) {
        this.name      = name;
        this.sortOrder = sortOrder;
        this.active    = active;
        this.deletedAt = null;
    }

    public static FaqCategory create(String name, int sortOrder, boolean active) {
        return new FaqCategory(name, sortOrder, active);
    }

    public void applyUpdate(String name, int sortOrder, boolean active) {
        this.name      = name;
        this.sortOrder = sortOrder;
        this.active    = active;
    }

    /** soft delete: 삭제 시각 기록. */
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
