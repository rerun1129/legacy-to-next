package com.freightos.admin.domain.code.port.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Port extends BaseEntity {

    private final String portCode;
    private String name;
    private String nameEn;
    private String countryCode;
    private PortType portType;
    private boolean active;
    private LocalDateTime deletedAt;

    private Port(String portCode, String name, String nameEn, String countryCode, PortType portType, boolean active) {
        this.portCode    = portCode;
        this.name        = name;
        this.nameEn      = nameEn;
        this.countryCode = countryCode;
        this.portType    = portType;
        this.active      = active;
        this.deletedAt   = null;
    }

    public static Port create(String portCode, String name, String nameEn, String countryCode, PortType portType, boolean active) {
        return new Port(portCode, name, nameEn, countryCode, portType, active);
    }

    /**
     * 수정 가능한 필드만 갱신. 식별 필드(portCode)는 변경 불가.
     */
    public void applyUpdate(String name, String nameEn, String countryCode, PortType portType, boolean active) {
        this.name        = name;
        this.nameEn      = nameEn;
        this.countryCode = countryCode;
        this.portType    = portType;
        this.active      = active;
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
