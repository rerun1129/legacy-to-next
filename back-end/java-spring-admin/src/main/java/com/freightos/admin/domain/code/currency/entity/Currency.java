package com.freightos.admin.domain.code.currency.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Currency extends BaseEntity {

    private final String currencyCode;
    private String name;
    private String nameEn;
    private String symbol;
    private boolean active;
    private LocalDateTime deletedAt;

    private Currency(String currencyCode, String name, String nameEn, String symbol, boolean active) {
        this.currencyCode = currencyCode;
        this.name         = name;
        this.nameEn       = nameEn;
        this.symbol       = symbol;
        this.active       = active;
        this.deletedAt    = null;
    }

    public static Currency create(String currencyCode, String name, String nameEn, String symbol, boolean active) {
        return new Currency(currencyCode, name, nameEn, symbol, active);
    }

    /**
     * 수정 가능한 필드만 갱신. 식별 필드(currencyCode)는 변경 불가.
     */
    public void applyUpdate(String name, String nameEn, String symbol, boolean active) {
        this.name   = name;
        this.nameEn = nameEn;
        this.symbol = symbol;
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
