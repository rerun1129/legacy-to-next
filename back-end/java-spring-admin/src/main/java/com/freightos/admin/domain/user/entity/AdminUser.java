package com.freightos.admin.domain.user.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class AdminUser extends BaseEntity {

    private final String username;
    private String email;
    private String passwordHash;
    private boolean active;
    private LocalDateTime deletedAt;
    private Map<String, List<String>> attributes;

    private AdminUser(String username, String email, String passwordHash, boolean active,
                      Map<String, List<String>> attributes) {
        this.username     = username;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.active       = active;
        this.deletedAt    = null;
        this.attributes   = attributes == null ? Collections.emptyMap() : Collections.unmodifiableMap(attributes);
    }

    public static AdminUser create(String username, String email, String passwordHash,
                                   boolean active, Map<String, List<String>> attributes) {
        return new AdminUser(username, email, passwordHash, active, attributes);
    }

    /**
     * 표시·상태 필드 갱신. 식별 필드(username)는 변경 불가.
     * passwordHash가 null 또는 빈 문자열이면 기존 값 유지.
     */
    public void applyUpdate(String email, String passwordHashOrNull, boolean active,
                            Map<String, List<String>> attributes) {
        this.email      = email;
        this.active     = active;
        this.attributes = attributes == null ? Collections.emptyMap() : Collections.unmodifiableMap(attributes);
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

    /** attributes의 role 키에 해당 role 값이 포함되어 있는지 확인한다. */
    public boolean hasRole(String role) {
        return attributes.getOrDefault("role", List.of()).contains(role);
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 deletedAt 을 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 attributes를 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignAttributes(Map<String, List<String>> attrs) {
        this.attributes = attrs == null ? Collections.emptyMap() : Collections.unmodifiableMap(attrs);
    }
}
