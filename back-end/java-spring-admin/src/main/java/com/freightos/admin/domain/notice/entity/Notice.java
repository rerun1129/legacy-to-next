package com.freightos.admin.domain.notice.entity;

import com.freightos.admin.common.entity.BaseEntity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Notice extends BaseEntity {

    private String title;
    private String content;
    private boolean pinned;
    private boolean active;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime deletedAt;

    private Notice(String title, String content, boolean pinned, boolean active,
                   LocalDateTime publishedAt, LocalDateTime expiresAt) {
        this.title       = title;
        this.content     = content;
        this.pinned      = pinned;
        this.active      = active;
        this.publishedAt = publishedAt;
        this.expiresAt   = expiresAt;
        this.deletedAt   = null;
    }

    public static Notice create(String title, String content, boolean pinned, boolean active,
                                LocalDateTime publishedAt, LocalDateTime expiresAt) {
        return new Notice(title, content, pinned, active, publishedAt, expiresAt);
    }

    /** 수정 가능한 필드 갱신. */
    public void applyUpdate(String title, String content, boolean pinned, boolean active,
                            LocalDateTime publishedAt, LocalDateTime expiresAt) {
        this.title       = title;
        this.content     = content;
        this.pinned      = pinned;
        this.active      = active;
        this.publishedAt = publishedAt;
        this.expiresAt   = expiresAt;
    }

    /** soft delete: 삭제 시각 기록 + 비활성화. */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.active    = false;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    /** 게시된 상태인지 확인. published_at이 현재 시각 이전일 때 true. */
    public boolean isPublished() {
        return publishedAt != null && !publishedAt.isAfter(LocalDateTime.now());
    }

    /** 만료된 상태인지 확인. expires_at이 현재 시각보다 과거일 때 true. */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 deletedAt을 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 publishedAt을 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 expiresAt을 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
