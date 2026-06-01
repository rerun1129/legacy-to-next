package com.freightos.admin.domain.userlayout.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 유저별 UI 레이아웃 영속화 도메인.
 * payload는 불투명 JSON 문자열로만 다룬다 — 도메인 계층은 내용을 해석하지 않는다.
 * BaseEntity를 상속하지 않는다: 감사 컬럼(createdBy/updatedBy) 없는 최소 컬럼 테이블.
 */
@Getter
public class UserUiLayout {

    private Long id;
    private final Long userId;
    private final String storageKey;
    private String payload;
    private Instant createdAt;
    private Instant updatedAt;

    private UserUiLayout(Long id, Long userId, String storageKey, String payload,
                         Instant createdAt, Instant updatedAt) {
        this.id         = id;
        this.userId     = userId;
        this.storageKey = storageKey;
        this.payload    = payload;
        this.createdAt  = createdAt;
        this.updatedAt  = updatedAt;
    }

    /** 신규 레이아웃 생성. id/createdAt/updatedAt은 어댑터 계층(JPA)이 채운다. */
    public static UserUiLayout create(Long userId, String storageKey, String payload) {
        return new UserUiLayout(null, userId, storageKey, payload, null, null);
    }

    /** payload 갱신. */
    public void updatePayload(String payload) {
        this.payload = payload;
    }

    /**
     * 어댑터 계층이 JPA→Domain 변환 시 식별자 및 타임스탬프를 주입할 때 사용한다.
     * 도메인 외부(어댑터)에서만 호출해야 한다.
     */
    public void assignIdentity(Long id, Instant createdAt, Instant updatedAt) {
        this.id        = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
