package com.freightos.admin.domain.subscription.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Subscription extends BaseEntity {

    private final Long subscriberId;
    private final String moduleCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    private Subscription(Long subscriberId, String moduleCode, LocalDate startDate, LocalDate endDate, boolean active) {
        this.subscriberId = subscriberId;
        this.moduleCode   = moduleCode;
        this.startDate    = startDate;
        this.endDate      = endDate;
        this.active       = active;
    }

    public static Subscription create(Long subscriberId, String moduleCode, LocalDate startDate, LocalDate endDate, boolean active) {
        return new Subscription(subscriberId, moduleCode, startDate, endDate, active);
    }

    /**
     * 수정 가능한 필드만 갱신. 식별 필드(subscriberId, moduleCode)는 변경 불가.
     */
    public void applyUpdate(LocalDate startDate, LocalDate endDate, boolean active) {
        this.startDate = startDate;
        this.endDate   = endDate;
        this.active    = active;
    }
}
