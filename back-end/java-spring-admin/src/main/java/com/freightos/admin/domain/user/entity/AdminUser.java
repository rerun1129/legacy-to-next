package com.freightos.admin.domain.user.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminUser extends BaseEntity {

    private final String username;
    private String email;
    private String passwordHash;
    private UserRole role;
    private boolean active;
    private LocalDateTime deletedAt;

    private AdminUser(String username, String email, String passwordHash, UserRole role, boolean active) {
        this.username     = username;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.active       = active;
        this.deletedAt    = null;
    }

    public static AdminUser create(String username, String email, String passwordHash,
                                   UserRole role, boolean active) {
        return new AdminUser(username, email, passwordHash, role, active);
    }

    /**
     * 표시·상태 필드 갱신. 식별 필드(username)는 변경 불가.
     * passwordHash가 null 또는 빈 문자열이면 기존 값 유지.
     */
    public void applyUpdate(String email, String passwordHashOrNull, UserRole role, boolean active) {
        this.email  = email;
        this.role   = role;
        this.active = active;
        if (passwordHashOrNull != null && !passwordHashOrNull.isBlank()) {
            this.passwordHash = passwordHashOrNull;
        }
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
     * 어댑터 계층이 JPA→Domain 변환 시 deletedAt 을 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
